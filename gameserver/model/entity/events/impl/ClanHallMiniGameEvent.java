package l2s.gameserver.model.entity.events.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l2s.commons.collections.CollectionUtils;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.SiegeClanDAO;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2s.gameserver.model.entity.events.objects.SiegeClanObject;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;

public class ClanHallMiniGameEvent extends SiegeEvent<ClanHall, CMGSiegeClanObject>
{
	public static final String NEXT_STEP = "next_step";
	public static final String REFUND = "refund";
	private boolean _arenaClosed;

	public ClanHallMiniGameEvent(final MultiValueSet<String> set)
	{
		super(set);
		_arenaClosed = true;
	}

	@Override
	public void startEvent()
	{
		_oldOwner = getResidence().getOwner();
		final List<CMGSiegeClanObject> siegeClans = this.getObjects("attackers");
		if(siegeClans.size() < 2)
		{
			final CMGSiegeClanObject siegeClan = CollectionUtils.safeGet(siegeClans, 0);
			if(siegeClan != null)
			{
				final CMGSiegeClanObject oldSiegeClan = getSiegeClan("refund", siegeClan.getObjectId());
				if(oldSiegeClan != null)
				{
					SiegeClanDAO.getInstance().delete(getResidence(), siegeClan);
					oldSiegeClan.setParam(oldSiegeClan.getParam() + siegeClan.getParam());
					SiegeClanDAO.getInstance().update(getResidence(), oldSiegeClan);
				}
				else
				{
					siegeClan.setType("refund");
					siegeClans.remove(siegeClan);
					addObject("refund", siegeClan);
					SiegeClanDAO.getInstance().update(getResidence(), siegeClan);
				}
			}
			siegeClans.clear();
			this.broadcastTo(Msg.THIS_CLAN_HALL_WAR_HAS_BEEN_CANCELLED, "attackers");
			this.broadcastInZone2(new SystemMessage(856).addString(getResidence().getName()));
			reCalcNextTime(false);
			return;
		}
		final CMGSiegeClanObject[] clans = siegeClans.toArray(new CMGSiegeClanObject[siegeClans.size()]);
		Arrays.sort(clans, SiegeClanObject.SiegeClanComparatorImpl.getInstance());
		final List<CMGSiegeClanObject> temp = new ArrayList<CMGSiegeClanObject>(4);
		for(int i = 0; i < clans.length; ++i)
		{
			final CMGSiegeClanObject siegeClan2 = clans[i];
			SiegeClanDAO.getInstance().delete(getResidence(), siegeClan2);
			if(temp.size() == 4)
			{
				siegeClans.remove(siegeClan2);
				siegeClan2.broadcast(Msg.YOU_HAVE_FAILED_IN_YOUR_ATTEMPT_TO_REGISTER_FOR_THE_CLAN_HALL_WAR);
			}
			else
			{
				temp.add(siegeClan2);
				siegeClan2.broadcast(Msg.YOU_HAVE_BEEN_REGISTERED_FOR_A_CLAN_HALL_WAR);
			}
		}
		_arenaClosed = false;
		super.startEvent();
	}

	@Override
	public void stopEvent(final boolean step)
	{
		removeBanishItems();
		final Clan newOwner = getResidence().getOwner();
		if(newOwner != null)
		{
			if(_oldOwner != newOwner)
			{
				newOwner.broadcastToOnlineMembers(new PlaySound("Siege_Victory"));
				newOwner.incReputation(getResidence().getReputation(), false, toString());
			}
			this.broadcastTo(new SystemMessage(855).addString(newOwner.getName()).addString(getResidence().getName()), "attackers", "defenders");
			this.broadcastTo(new SystemMessage(843).addString(getResidence().getName()), "attackers", "defenders");
		}
		else
			this.broadcastTo(new SystemMessage(856).addString(getResidence().getName()), "attackers");
		updateParticles(false, "attackers");
		this.removeObjects("attackers");
		super.stopEvent(step);
		_oldOwner = null;
	}

	public void nextStep()
	{
		final List<CMGSiegeClanObject> siegeClans = this.getObjects("attackers");
		for(int i = 0; i < siegeClans.size(); ++i)
			spawnAction("arena_" + i, true);
		updateParticles(_arenaClosed = true, "attackers");
		this.broadcastTo(new SystemMessage(844).addString(getResidence().getName()), "attackers");
	}

	@Override
	public void removeState(final int val)
	{
		super.removeState(val);
		if(val == 2)
			this.broadcastTo(Msg.THE_REGISTRATION_PERIOD_FOR_A_CLAN_HALL_WAR_HAS_ENDED, "attackers");
	}

	@Override
	public CMGSiegeClanObject newSiegeClan(final String type, final int clanId, final long param, final long date)
	{
		final Clan clan = ClanTable.getInstance().getClan(clanId);
		return clan == null ? null : new CMGSiegeClanObject(type, clan, param, date);
	}

	@Override
	public void announce(final int val)
	{
		final int seconds = val % 60;
		final int min = val / 60;
		if(min > 0)
		{
			final SystemMessage msg = min > 10 ? Msg.IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_MUST_HURRY_AND_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALLS_ARENA : Msg.IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_PLEASE_ENTER_THE_ARENA_NOW;
			this.broadcastTo(msg.addNumber(Integer.valueOf(min)), "attackers");
		}
		else
			this.broadcastTo(new SystemMessage(1828).addNumber(Integer.valueOf(seconds)), "attackers");
	}

	@Override
	public void processStep(final Clan clan)
	{
		if(clan != null)
			getResidence().changeOwner(clan);
		this.stopEvent(true);
	}

	@Override
	public void loadSiegeClans()
	{
		addObjects("attackers", SiegeClanDAO.getInstance().load(getResidence(), "attackers"));
		addObjects("refund", SiegeClanDAO.getInstance().load(getResidence(), "refund"));
	}

	@Override
	public void action(final String name, final boolean start)
	{
		if(name.equalsIgnoreCase("next_step"))
			nextStep();
		else
			super.action(name, start);
	}

	@Override
	public int getUserRelation(final Player thisPlayer, final int result)
	{
		return result;
	}

	@Override
	public int getRelation(final Player thisPlayer, final Player targetPlayer, final int result)
	{
		return result;
	}

	public boolean isArenaClosed()
	{
		return _arenaClosed;
	}

	@Override
	public void onAddEvent(final GameObject object)
	{
		if(object.isItem())
			addBanishItem((ItemInstance) object);
	}
}
