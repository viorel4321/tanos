package l2s.gameserver.model.entity.residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dao.JdbcEntity;
import l2s.commons.dao.JdbcEntityState;
import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public abstract class Residence implements JdbcEntity
{
	private static final long serialVersionUID = 1L;
	private static final Logger _log;
	public static final long CYCLE_TIME = 3600000L;
	protected final int _id;
	protected final String _name;
	protected Clan _owner;
	protected Zone _zone;
	protected List<ResidenceFunction> _functions;
	protected List<Skill> _skills;
	protected SiegeEvent<?, ?> _siegeEvent;
	protected Calendar _siegeDate;
	protected Calendar _lastSiegeDate;
	protected Calendar _ownDate;
	protected ScheduledFuture<?> _cycleTask;
	private int _cycle;
	private int _rewardCount;
	private int _paidCycle;
	private int _reputation;
	private int _reputationOwner;
	private int _reputationLoser;
	protected JdbcEntityState _jdbcEntityState;
	protected List<Location> _banishPoints;
	protected List<Location> _ownerRestartPoints;
	protected List<Location> _otherRestartPoints;
	protected List<Location> _chaosRestartPoints;

	public Residence(final StatsSet set)
	{
		_functions = new ArrayList<ResidenceFunction>();
		_skills = new ArrayList<Skill>();
		_siegeDate = Calendar.getInstance();
		_lastSiegeDate = Calendar.getInstance();
		_ownDate = Calendar.getInstance();
		_jdbcEntityState = JdbcEntityState.CREATED;
		_banishPoints = new ArrayList<Location>();
		_ownerRestartPoints = new ArrayList<Location>();
		_otherRestartPoints = new ArrayList<Location>();
		_chaosRestartPoints = new ArrayList<Location>();
		_id = set.getInteger("id");
		_name = set.getString("name");
	}

	public abstract ResidenceType getType();

	public void init()
	{
		initZone();
		initEvent();
		loadData();
		loadFunctions();
		rewardSkills();
		startCycleTask();
	}

	protected void initZone()
	{
		_zone = ZoneManager.getInstance().getZone("residence_" + _id);
	}

	protected void initEvent()
	{
		_siegeEvent = EventHolder.getInstance().getEvent(EventType.SIEGE_EVENT, _id);
	}

	@SuppressWarnings("unchecked")
	public <E extends SiegeEvent<?, ?>> E getSiegeEvent()
	{
		return (E) _siegeEvent;
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public int getOwnerId()
	{
		return _owner == null ? 0 : _owner.getClanId();
	}

	public Clan getOwner()
	{
		return _owner;
	}

	public Zone getZone()
	{
		return _zone;
	}

	protected abstract void loadData();

	public abstract void changeOwner(final Clan p0);

	public Calendar getOwnDate()
	{
		return _ownDate;
	}

	public Calendar getSiegeDate()
	{
		return _siegeDate;
	}

	public Calendar getLastSiegeDate()
	{
		return _lastSiegeDate;
	}

	public void addSkill(final Skill skill)
	{
		_skills.add(skill);
	}

	public void addFunction(final ResidenceFunction function)
	{
		_functions.add(function);
	}

	public boolean checkIfInZone(final Location loc)
	{
		return this.checkIfInZone(loc.x, loc.y, loc.z);
	}

	public boolean checkIfInZone(final int x, final int y, final int z)
	{
		return getZone() != null && getZone().checkIfInZone(x, y, z);
	}

	public void banishForeigner()
	{
		for(final Player player : _zone.getInsidePlayers())
		{
			if(player.getClanId() == getOwnerId())
				continue;
			player.teleToLocation(getBanishPoint());
		}
	}

	public void rewardSkills()
	{
		final Clan owner = getOwner();
		if(owner != null)
			for(final Skill skill : _skills)
			{
				owner.addNewSkill(skill, false);
				owner.broadcastToOnlineMembers(new SystemMessage(1788).addSkillName(skill.getId(), skill.getLevel()));
			}
	}

	public void removeSkills()
	{
		final Clan owner = getOwner();
		if(owner != null)
			for(final Skill skill : _skills)
				owner.removeSkill(skill);
	}

	protected void loadFunctions()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM residence_functions WHERE id = ?");
			statement.setInt(1, getId());
			rs = statement.executeQuery();
			while(rs.next())
			{
				final ResidenceFunction function = getFunction(rs.getInt("type"));
				function.setLvl(rs.getInt("lvl"));
				function.setEndTimeInMillis(rs.getInt("endTime") * 1000L);
				function.setInDebt(rs.getBoolean("inDebt"));
				function.setActive(true);
				startAutoTaskForFunction(function);
			}
		}
		catch(Exception e)
		{
			Residence._log.warn("Residence: loadFunctions(): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rs);
		}
	}

	public boolean isFunctionActive(final int type)
	{
		final ResidenceFunction function = getFunction(type);
		return function != null && function.isActive() && function.getLevel() > 0;
	}

	public ResidenceFunction getFunction(final int type)
	{
		for(int i = 0; i < _functions.size(); ++i)
			if(_functions.get(i).getType() == type)
				return _functions.get(i);
		return null;
	}

	public boolean updateFunctions(final int type, final int level)
	{
		final Clan clan = getOwner();
		if(clan == null)
			return false;
		final long count = clan.getAdenaCount();
		final ResidenceFunction function = getFunction(type);
		if(function == null)
			return false;
		if(function.isActive() && function.getLevel() == level)
			return true;
		final int lease = level == 0 ? 0 : getFunction(type).getLease(level);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(!function.isActive())
			{
				if(count < lease)
					return false;
				clan.getWarehouse().destroyItem(57, lease);
				final long time = Calendar.getInstance().getTimeInMillis() + 86400000L;
				statement = con.prepareStatement("REPLACE residence_functions SET id=?, type=?, lvl=?, endTime=?");
				statement.setInt(1, getId());
				statement.setInt(2, type);
				statement.setInt(3, level);
				statement.setInt(4, (int) (time / 1000L));
				statement.execute();
				function.setLvl(level);
				function.setEndTimeInMillis(time);
				function.setActive(true);
				startAutoTaskForFunction(function);
			}
			else
			{
				if(count < lease - getFunction(type).getLease())
					return false;
				if(lease > getFunction(type).getLease())
					clan.getWarehouse().destroyItem(57, lease - getFunction(type).getLease());
				statement = con.prepareStatement("REPLACE residence_functions SET id=?, type=?, lvl=?");
				statement.setInt(1, getId());
				statement.setInt(2, type);
				statement.setInt(3, level);
				statement.execute();
				function.setLvl(level);
			}
		}
		catch(Exception e)
		{
			Residence._log.warn("Exception: SiegeUnit.updateFunctions(int type, int lvl, int lease, long rate, long time, boolean addNew): " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	public void removeFunction(final int type)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM residence_functions WHERE id=? AND type=?");
			statement.setInt(1, getId());
			statement.setInt(2, type);
			statement.execute();
		}
		catch(Exception e)
		{
			Residence._log.warn("Exception: removeFunctions(int type): " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void startAutoTaskForFunction(final ResidenceFunction function)
	{
		if(getOwnerId() == 0)
			return;
		final Clan clan = getOwner();
		if(clan == null)
			return;
		if(function.getEndTimeInMillis() > System.currentTimeMillis())
			ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
		else if(function.isInDebt() && clan.getAdenaCount() >= function.getLease())
		{
			clan.getWarehouse().destroyItem(57, function.getLease());
			function.updateRentTime(false);
			ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
		}
		else if(!function.isInDebt())
		{
			function.setInDebt(true);
			function.updateRentTime(true);
			ThreadPoolManager.getInstance().schedule(new AutoTaskForFunctions(function), function.getEndTimeInMillis() - System.currentTimeMillis());
		}
		else
		{
			function.setLvl(0);
			function.setActive(false);
			removeFunction(function.getType());
		}
	}

	@Override
	public void setJdbcState(final JdbcEntityState state)
	{
		_jdbcEntityState = state;
	}

	@Override
	public JdbcEntityState getJdbcState()
	{
		return _jdbcEntityState;
	}

	@Override
	public void save()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void delete()
	{
		throw new UnsupportedOperationException();
	}

	public void cancelCycleTask()
	{
		_cycle = 0;
		_paidCycle = 0;
		_rewardCount = 0;
		if(_cycleTask != null)
		{
			_cycleTask.cancel(false);
			_cycleTask = null;
		}
		setJdbcState(JdbcEntityState.UPDATED);
	}

	public void startCycleTask()
	{
		if(_owner == null)
			return;
		final long ownedTime = getOwnDate().getTimeInMillis();
		if(ownedTime == 0L)
			return;
		long diff;
		for(diff = System.currentTimeMillis() - ownedTime; diff >= 3600000L; diff -= 3600000L)
		{}
		_cycleTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ResidenceCycleTask(), diff, 3600000L);
	}

	public void chanceCycle()
	{
		setCycle(getCycle() + 1);
		setJdbcState(JdbcEntityState.UPDATED);
	}

	public List<Skill> getSkills()
	{
		return _skills;
	}

	public void addBanishPoint(final Location loc)
	{
		_banishPoints.add(loc);
	}

	public void addOwnerRestartPoint(final Location loc)
	{
		_ownerRestartPoints.add(loc);
	}

	public void addOtherRestartPoint(final Location loc)
	{
		_otherRestartPoints.add(loc);
	}

	public void addChaosRestartPoint(final Location loc)
	{
		_chaosRestartPoints.add(loc);
	}

	public Location getBanishPoint()
	{
		if(_banishPoints.isEmpty())
			return null;
		return _banishPoints.get(Rnd.get(_banishPoints.size()));
	}

	public Location getOwnerRestartPoint()
	{
		if(_ownerRestartPoints.isEmpty())
			return null;
		return _ownerRestartPoints.get(Rnd.get(_ownerRestartPoints.size()));
	}

	public Location getOtherRestartPoint()
	{
		if(_otherRestartPoints.isEmpty())
			return null;
		return _otherRestartPoints.get(Rnd.get(_otherRestartPoints.size()));
	}

	public Location getChaosRestartPoint()
	{
		if(_chaosRestartPoints.isEmpty())
			return null;
		return _chaosRestartPoints.get(Rnd.get(_chaosRestartPoints.size()));
	}

	public Location getNotOwnerRestartPoint(final Player player)
	{
		return player.getKarma() > 0 ? getChaosRestartPoint() : getOtherRestartPoint();
	}

	public int getCycle()
	{
		return _cycle;
	}

	public long getCycleDelay()
	{
		if(_cycleTask == null)
			return 0L;
		return _cycleTask.getDelay(TimeUnit.SECONDS);
	}

	public void setCycle(final int cycle)
	{
		_cycle = cycle;
	}

	public int getPaidCycle()
	{
		return _paidCycle;
	}

	public void setPaidCycle(final int paidCycle)
	{
		_paidCycle = paidCycle;
	}

	public int getRewardCount()
	{
		return _rewardCount;
	}

	public void setRewardCount(final int rewardCount)
	{
		_rewardCount = rewardCount;
	}

	public int getReputation()
	{
		return _reputation;
	}

	public void setReputation(final int rep)
	{
		_reputation = rep;
	}

	public int getReputationOwner()
	{
		return _reputationOwner;
	}

	public void setReputationOwner(final int rep)
	{
		_reputationOwner = rep;
	}

	public int getReputationLoser()
	{
		return _reputationLoser;
	}

	public void setReputationLoser(final int rep)
	{
		_reputationLoser = rep;
	}

	static
	{
		_log = LoggerFactory.getLogger(Residence.class);
	}

	public class ResidenceCycleTask implements Runnable
	{
		@Override
		public void run()
		{
			chanceCycle();
			update();
		}
	}

	private class AutoTaskForFunctions implements Runnable
	{
		ResidenceFunction _function;

		public AutoTaskForFunctions(final ResidenceFunction function)
		{
			_function = function;
		}

		@Override
		public void run()
		{
			startAutoTaskForFunction(_function);
		}
	}
}
