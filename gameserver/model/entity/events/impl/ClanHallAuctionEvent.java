package l2s.gameserver.model.entity.events.impl;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.dao.JdbcEntityState;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.actions.StartStopAction;
import l2s.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;

public class ClanHallAuctionEvent extends SiegeEvent<ClanHall, AuctionSiegeClanObject>
{
	private Calendar _endSiegeDate;

	public ClanHallAuctionEvent(final MultiValueSet<String> set)
	{
		super(set);
		_endSiegeDate = Calendar.getInstance();
	}

	@Override
	public void reCalcNextTime(final boolean onStart)
	{
		clearActions();
		_onTimeActions.clear();
		final Clan owner = getResidence().getOwner();
		_endSiegeDate.setTimeInMillis(0L);
		if(getResidence().getAuctionLength() == 0 && owner == null)
		{
			getResidence().getSiegeDate().setTimeInMillis(System.currentTimeMillis());
			setDate();
			getResidence().setAuctionLength(7);
			getResidence().setAuctionMinBid(getResidence().getBaseMinBid());
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().update();
			_onTimeActions.clear();
			addOnTimeAction(0, new StartStopAction("event", true));
			addOnTimeAction(getResidence().getAuctionLength() * 86400, new StartStopAction("event", false));
			_endSiegeDate.setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L);
			registerActions();
		}
		else if(getResidence().getAuctionLength() != 0 || owner == null)
		{
			long endDate = getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L;
			if(!onStart && endDate <= System.currentTimeMillis())
			{
				getResidence().getSiegeDate().setTimeInMillis(System.currentTimeMillis());
				if(owner == null)
					setDate();
				getResidence().setJdbcState(JdbcEntityState.UPDATED);
				getResidence().update();
				endDate = getResidence().getSiegeDate().getTimeInMillis() + getResidence().getAuctionLength() * 86400000L;
			}
			_endSiegeDate.setTimeInMillis(endDate);
			_onTimeActions.clear();
			addOnTimeAction(0, new StartStopAction("event", true));
			addOnTimeAction(getResidence().getAuctionLength() * 86400, new StartStopAction("event", false));
			registerActions();
		}
	}

	private void setDate()
	{
		getResidence().getSiegeDate().set(7, _dayOfWeek);
		getResidence().getSiegeDate().set(11, _hourOfDay);
		getResidence().getSiegeDate().set(12, 0);
		getResidence().getSiegeDate().set(13, 10);
		getResidence().getSiegeDate().set(14, 0);
	}

	@Override
	public void stopEvent(final boolean step)
	{
		final List<AuctionSiegeClanObject> siegeClanObjects = this.removeObjects("attackers");
		final AuctionSiegeClanObject[] clans = siegeClanObjects.toArray(new AuctionSiegeClanObject[siegeClanObjects.size()]);
		Arrays.sort(clans, SiegeClanObject.SiegeClanComparatorImpl.getInstance());
		final Clan oldOwner = getResidence().getOwner();
		final AuctionSiegeClanObject winnerSiegeClan = clans.length > 0 ? clans[0] : null;
		if(winnerSiegeClan != null)
		{
			final SystemMessage msg = new SystemMessage(776).addString(winnerSiegeClan.getClan().getName());
			for(final AuctionSiegeClanObject siegeClan : siegeClanObjects)
			{
				final Player player = siegeClan.getClan().getLeader().getPlayer();
				if(player != null)
					player.sendPacket(msg);
				else
					PlayerMessageStack.getInstance().mailto(siegeClan.getClan().getLeaderId(), msg);
				if(siegeClan != winnerSiegeClan)
				{
					final long returnBid = siegeClan.getParam() - (long) (siegeClan.getParam() * 0.1);
					siegeClan.getClan().getWarehouse().addItem(Config.CH_AUCTION_BID_ID, returnBid);
				}
			}
			SiegeClanDAO.getInstance().delete(getResidence());
			if(oldOwner != null)
				oldOwner.getWarehouse().addItem(57, getResidence().getDeposit() + winnerSiegeClan.getParam());
			getResidence().setAuctionLength(0);
			getResidence().setAuctionMinBid(0L);
			getResidence().setAuctionDescription("");
			getResidence().getSiegeDate().setTimeInMillis(0L);
			getResidence().getLastSiegeDate().setTimeInMillis(0L);
			getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
			getResidence().setJdbcState(JdbcEntityState.UPDATED);
			getResidence().changeOwner(winnerSiegeClan.getClan());
			getResidence().startCycleTask();
		}
		else if(oldOwner != null)
		{
			final Player player2 = oldOwner.getLeader().getPlayer();
			if(player2 != null)
				player2.sendPacket(Msg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED);
			else
				PlayerMessageStack.getInstance().mailto(oldOwner.getLeaderId(), Msg.THE_CLAN_HALL_WHICH_HAD_BEEN_PUT_UP_FOR_AUCTION_WAS_NOT_SOLD_AND_THEREFORE_HAS_BEEN_RELISTED);
		}
		super.stopEvent(step);
	}

	@Override
	public boolean isParticle(final Player player)
	{
		return false;
	}

	@Override
	public AuctionSiegeClanObject newSiegeClan(final String type, final int clanId, final long param, final long date)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : new AuctionSiegeClanObject(type, clan, param, date);
	}

	public Calendar getEndSiegeDate()
	{
		return _endSiegeDate;
	}
}
