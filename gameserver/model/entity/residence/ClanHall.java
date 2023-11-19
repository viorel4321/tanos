package l2s.gameserver.model.entity.residence;

import java.sql.Connection;
import java.sql.PreparedStatement;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.ClanDataDAO;
import l2s.gameserver.dao.ClanHallDAO;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.PlayerMessageStack;
import l2s.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.templates.StatsSet;

public class ClanHall extends Residence
{
	private static final Logger _log;
	private static final int REWARD_CYCLE = 168;
	private int _auctionLength;
	private long _auctionMinBid;
	private long _auctionLastBid;
	private String _location;
	private String _auctionDescription;
	private final int _grade;
	private final long _rentalFee;
	private final long _minBid;
	private final long _deposit;

	public ClanHall(final StatsSet set)
	{
		super(set);
		_auctionDescription = "";
		_grade = set.getInteger("grade", 0);
		_rentalFee = (long) (set.getInteger("rental_fee", 0) * Config.RESIDENCE_LEASE_MULTIPLIER);
		_minBid = set.getInteger("min_bid", 0);
		_deposit = set.getInteger("deposit", 0);
	}

	@Override
	public void init()
	{
		initZone();
		initEvent();
		loadData();
		loadFunctions();
		rewardSkills();

		SiegeEvent<?, ?> siegeEvent = getSiegeEvent();
		if(siegeEvent != null && siegeEvent.getClass() == ClanHallAuctionEvent.class && _owner != null && getAuctionLength() == 0)
			startCycleTask();
	}

	@Override
	public void changeOwner(final Clan clan)
	{
		final Clan oldOwner = getOwner();
		if(oldOwner != null && (clan == null || clan.getClanId() != oldOwner.getClanId()))
		{
			removeSkills();
			oldOwner.setHasHideout(0);
			cancelCycleTask();
		}
		updateOwnerInDB(clan);
		rewardSkills();
		update();

		SiegeEvent<?, ?> siegeEvent = getSiegeEvent();
		if(clan == null && siegeEvent != null && siegeEvent.getClass() == ClanHallAuctionEvent.class)
			siegeEvent.reCalcNextTime(false);
	}

	@Override
	public ResidenceType getType()
	{
		return ResidenceType.ClanHall;
	}

	@Override
	protected void loadData()
	{
		_owner = ClanDataDAO.getInstance().getOwner(this);
		ClanHallDAO.getInstance().select(this);
	}

	private void updateOwnerInDB(final Clan clan)
	{
		_owner = clan;
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET hasHideout=0 WHERE hasHideout=?");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE clan_data SET hasHideout=? WHERE clan_id=?");
			statement.setInt(1, getId());
			statement.setInt(2, getOwnerId());
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("DELETE FROM residence_functions WHERE id=?");
			statement.setInt(1, getId());
			statement.execute();
			DbUtils.close(statement);
			if(clan != null)
			{
				clan.setHasHideout(getId());
				clan.broadcastClanStatus(false, true, false);
			}
		}
		catch(Exception e)
		{
			ClanHall._log.warn("Exception: updateOwnerInDB(L2Clan clan): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int getGrade()
	{
		return _grade;
	}

	@Override
	public void update()
	{
		ClanHallDAO.getInstance().update(this);
	}

	public int getAuctionLength()
	{
		return _auctionLength;
	}

	public void setAuctionLength(final int auctionLength)
	{
		_auctionLength = auctionLength;
	}

	public String getAuctionDescription()
	{
		return _auctionDescription;
	}

	public void setAuctionDescription(final String auctionDescription)
	{
		_auctionDescription = auctionDescription == null ? "" : auctionDescription;
	}

	public long getAuctionMinBid()
	{
		return _auctionMinBid;
	}

	public void setAuctionMinBid(final long auctionMinBid)
	{
		_auctionMinBid = auctionMinBid;
	}

	public long getAuctionLastBid()
	{
		return _auctionLastBid;
	}

	public void setAuctionLastBid(final long auctionLastBid)
	{
		_auctionLastBid = auctionLastBid;
	}

	public String getLocation()
	{
		return _location;
	}

	public void setLocation(final String name)
	{
		_location = name;
	}

	public long getRentalFee()
	{
		return _rentalFee;
	}

	public long getBaseMinBid()
	{
		return _minBid;
	}

	public long getDeposit()
	{
		return _deposit;
	}

	@Override
	public void chanceCycle()
	{
		super.chanceCycle();
		setPaidCycle(getPaidCycle() + 1);
		if(getPaidCycle() >= 168)
			if(_owner.getWarehouse().getAdenaCount() > _rentalFee)
			{
				_owner.getWarehouse().destroyItem(57, _rentalFee);
				setPaidCycle(0);
			}
			else
			{
				final ClanMember member = _owner.getLeader();
				if(member.isOnline())
					member.getPlayer().sendPacket(Msg.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED);
				else
					PlayerMessageStack.getInstance().mailto(member.getObjectId(), Msg.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED.packet(null));
				changeOwner(null);
			}
	}

	static
	{
		_log = LoggerFactory.getLogger(ClanHall.class);
	}
}
