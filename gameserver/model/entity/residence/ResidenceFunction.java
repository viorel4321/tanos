package l2s.gameserver.model.entity.residence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.TeleportLocation;
import l2s.gameserver.tables.SkillTable;

public class ResidenceFunction
{
	private static final Logger _log;
	public static final int TELEPORT = 1;
	public static final int ITEM_CREATE = 2;
	public static final int RESTORE_HP = 3;
	public static final int RESTORE_MP = 4;
	public static final int RESTORE_EXP = 5;
	public static final int SUPPORT = 6;
	public static final int CURTAIN = 7;
	public static final int PLATFORM = 8;
	private int _id;
	private int _type;
	private int _level;
	private Calendar _endDate;
	private boolean _inDebt;
	private boolean _active;
	private Map<Integer, Integer> _leases;
	private Map<Integer, TeleportLocation[]> _teleports;
	private Map<Integer, int[]> _buylists;
	private Map<Integer, Object[][]> _buffs;
	public static final String A = "";
	public static final String W = "W";
	public static final String M = "M";
	private static final Object[][][] buffs_template;

	public ResidenceFunction(final int id, final int type)
	{
		_leases = new ConcurrentSkipListMap<Integer, Integer>();
		_teleports = new ConcurrentSkipListMap<Integer, TeleportLocation[]>();
		_buylists = new ConcurrentSkipListMap<Integer, int[]>();
		_buffs = new ConcurrentSkipListMap<Integer, Object[][]>();
		_id = id;
		_type = type;
		_endDate = Calendar.getInstance();
	}

	public int getResidenceId()
	{
		return _id;
	}

	public int getType()
	{
		return _type;
	}

	public int getLevel()
	{
		return _level;
	}

	public void setLvl(final int lvl)
	{
		_level = lvl;
	}

	public long getEndTimeInMillis()
	{
		return _endDate.getTimeInMillis();
	}

	public void setEndTimeInMillis(final long time)
	{
		_endDate.setTimeInMillis(time);
	}

	public void setInDebt(final boolean inDebt)
	{
		_inDebt = inDebt;
	}

	public boolean isInDebt()
	{
		return _inDebt;
	}

	public void setActive(final boolean active)
	{
		_active = active;
	}

	public boolean isActive()
	{
		return _active;
	}

	public void updateRentTime(final boolean inDebt)
	{
		setEndTimeInMillis(System.currentTimeMillis() + 86400000L);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE residence_functions SET endTime=?, inDebt=? WHERE type=? AND id=?");
			statement.setInt(1, (int) (getEndTimeInMillis() / 1000L));
			statement.setInt(2, inDebt ? 1 : 0);
			statement.setInt(3, getType());
			statement.setInt(4, getResidenceId());
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			ResidenceFunction._log.error("", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public TeleportLocation[] getTeleports()
	{
		return this.getTeleports(_level);
	}

	public TeleportLocation[] getTeleports(final int level)
	{
		return _teleports.get(level);
	}

	public void addTeleports(final int level, final TeleportLocation[] teleports)
	{
		_teleports.put(level, teleports);
	}

	public int getLease()
	{
		if(_level == 0)
			return 0;
		return this.getLease(_level);
	}

	public int getLease(final int level)
	{
		return _leases.get(level);
	}

	public void addLease(final int level, final int lease)
	{
		_leases.put(level, lease);
	}

	public int[] getBuylist()
	{
		return this.getBuylist(_level);
	}

	public int[] getBuylist(final int level)
	{
		return _buylists.get(level);
	}

	public void addBuylist(final int level, final int[] buylist)
	{
		_buylists.put(level, buylist);
	}

	public Object[][] getBuffs()
	{
		return this.getBuffs(_level);
	}

	public Object[][] getBuffs(final int level)
	{
		return _buffs.get(level);
	}

	public void addBuffs(final int level)
	{
		_buffs.put(level, ResidenceFunction.buffs_template[level]);
	}

	public Set<Integer> getLevels()
	{
		return _leases.keySet();
	}

	static
	{
		_log = LoggerFactory.getLogger(ResidenceFunction.class);
		buffs_template = new Object[][][] {
				new Object[0][],
				{
						{ SkillTable.getInstance().getInfo(4342, 1), "" },
						{ SkillTable.getInstance().getInfo(4343, 1), "" },
						{ SkillTable.getInstance().getInfo(4344, 1), "" },
						{ SkillTable.getInstance().getInfo(4346, 1), "" },
						{ SkillTable.getInstance().getInfo(4345, 1), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 2), "" },
						{ SkillTable.getInstance().getInfo(4343, 3), "" },
						{ SkillTable.getInstance().getInfo(4344, 3), "" },
						{ SkillTable.getInstance().getInfo(4346, 4), "" },
						{ SkillTable.getInstance().getInfo(4345, 3), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 2), "" },
						{ SkillTable.getInstance().getInfo(4343, 3), "" },
						{ SkillTable.getInstance().getInfo(4344, 3), "" },
						{ SkillTable.getInstance().getInfo(4346, 4), "" },
						{ SkillTable.getInstance().getInfo(4345, 3), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 2), "" },
						{ SkillTable.getInstance().getInfo(4343, 3), "" },
						{ SkillTable.getInstance().getInfo(4344, 3), "" },
						{ SkillTable.getInstance().getInfo(4346, 4), "" },
						{ SkillTable.getInstance().getInfo(4345, 3), "W" },
						{ SkillTable.getInstance().getInfo(4347, 2), "" },
						{ SkillTable.getInstance().getInfo(4349, 1), "" },
						{ SkillTable.getInstance().getInfo(4350, 1), "W" },
						{ SkillTable.getInstance().getInfo(4348, 2), "" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 2), "" },
						{ SkillTable.getInstance().getInfo(4343, 3), "" },
						{ SkillTable.getInstance().getInfo(4344, 3), "" },
						{ SkillTable.getInstance().getInfo(4346, 4), "" },
						{ SkillTable.getInstance().getInfo(4345, 3), "W" },
						{ SkillTable.getInstance().getInfo(4347, 2), "" },
						{ SkillTable.getInstance().getInfo(4349, 1), "" },
						{ SkillTable.getInstance().getInfo(4350, 1), "W" },
						{ SkillTable.getInstance().getInfo(4348, 2), "" },
						{ SkillTable.getInstance().getInfo(4351, 2), "M" },
						{ SkillTable.getInstance().getInfo(4352, 1), "" },
						{ SkillTable.getInstance().getInfo(4353, 2), "W" },
						{ SkillTable.getInstance().getInfo(4358, 1), "W" },
						{ SkillTable.getInstance().getInfo(4354, 1), "W" } },
				new Object[0][],
				{
						{ SkillTable.getInstance().getInfo(4342, 2), "" },
						{ SkillTable.getInstance().getInfo(4343, 3), "" },
						{ SkillTable.getInstance().getInfo(4344, 3), "" },
						{ SkillTable.getInstance().getInfo(4346, 4), "" },
						{ SkillTable.getInstance().getInfo(4345, 3), "W" },
						{ SkillTable.getInstance().getInfo(4347, 6), "" },
						{ SkillTable.getInstance().getInfo(4349, 2), "" },
						{ SkillTable.getInstance().getInfo(4350, 4), "W" },
						{ SkillTable.getInstance().getInfo(4348, 6), "" },
						{ SkillTable.getInstance().getInfo(4351, 6), "M" },
						{ SkillTable.getInstance().getInfo(4352, 2), "" },
						{ SkillTable.getInstance().getInfo(4353, 6), "W" },
						{ SkillTable.getInstance().getInfo(4358, 3), "W" },
						{ SkillTable.getInstance().getInfo(4354, 4), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 2), "" },
						{ SkillTable.getInstance().getInfo(4343, 3), "" },
						{ SkillTable.getInstance().getInfo(4344, 3), "" },
						{ SkillTable.getInstance().getInfo(4346, 4), "" },
						{ SkillTable.getInstance().getInfo(4345, 3), "W" },
						{ SkillTable.getInstance().getInfo(4347, 6), "" },
						{ SkillTable.getInstance().getInfo(4349, 2), "" },
						{ SkillTable.getInstance().getInfo(4350, 4), "W" },
						{ SkillTable.getInstance().getInfo(4348, 6), "" },
						{ SkillTable.getInstance().getInfo(4351, 6), "M" },
						{ SkillTable.getInstance().getInfo(4352, 2), "" },
						{ SkillTable.getInstance().getInfo(4353, 6), "W" },
						{ SkillTable.getInstance().getInfo(4358, 3), "W" },
						{ SkillTable.getInstance().getInfo(4354, 4), "W" },
						{ SkillTable.getInstance().getInfo(4355, 1), "M" },
						{ SkillTable.getInstance().getInfo(4356, 1), "M" },
						{ SkillTable.getInstance().getInfo(4357, 1), "W" },
						{ SkillTable.getInstance().getInfo(4359, 1), "W" },
						{ SkillTable.getInstance().getInfo(4360, 1), "W" } },
				new Object[0][],
				new Object[0][],
				{
						{ SkillTable.getInstance().getInfo(4342, 3), "" },
						{ SkillTable.getInstance().getInfo(4343, 4), "" },
						{ SkillTable.getInstance().getInfo(4344, 4), "" },
						{ SkillTable.getInstance().getInfo(4346, 5), "" },
						{ SkillTable.getInstance().getInfo(4345, 4), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 4), "" },
						{ SkillTable.getInstance().getInfo(4343, 6), "" },
						{ SkillTable.getInstance().getInfo(4344, 6), "" },
						{ SkillTable.getInstance().getInfo(4346, 8), "" },
						{ SkillTable.getInstance().getInfo(4345, 6), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 4), "" },
						{ SkillTable.getInstance().getInfo(4343, 6), "" },
						{ SkillTable.getInstance().getInfo(4344, 6), "" },
						{ SkillTable.getInstance().getInfo(4346, 8), "" },
						{ SkillTable.getInstance().getInfo(4345, 6), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 4), "" },
						{ SkillTable.getInstance().getInfo(4343, 6), "" },
						{ SkillTable.getInstance().getInfo(4344, 6), "" },
						{ SkillTable.getInstance().getInfo(4346, 8), "" },
						{ SkillTable.getInstance().getInfo(4345, 6), "W" },
						{ SkillTable.getInstance().getInfo(4347, 8), "" },
						{ SkillTable.getInstance().getInfo(4349, 3), "" },
						{ SkillTable.getInstance().getInfo(4350, 5), "W" },
						{ SkillTable.getInstance().getInfo(4348, 8), "" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 4), "" },
						{ SkillTable.getInstance().getInfo(4343, 6), "" },
						{ SkillTable.getInstance().getInfo(4344, 6), "" },
						{ SkillTable.getInstance().getInfo(4346, 8), "" },
						{ SkillTable.getInstance().getInfo(4345, 6), "W" },
						{ SkillTable.getInstance().getInfo(4347, 8), "" },
						{ SkillTable.getInstance().getInfo(4349, 3), "" },
						{ SkillTable.getInstance().getInfo(4350, 5), "W" },
						{ SkillTable.getInstance().getInfo(4348, 8), "" },
						{ SkillTable.getInstance().getInfo(4351, 8), "M" },
						{ SkillTable.getInstance().getInfo(4352, 3), "" },
						{ SkillTable.getInstance().getInfo(4353, 8), "W" },
						{ SkillTable.getInstance().getInfo(4358, 4), "W" },
						{ SkillTable.getInstance().getInfo(4354, 5), "W" } },
				new Object[0][],
				{
						{ SkillTable.getInstance().getInfo(4342, 4), "" },
						{ SkillTable.getInstance().getInfo(4343, 6), "" },
						{ SkillTable.getInstance().getInfo(4344, 6), "" },
						{ SkillTable.getInstance().getInfo(4346, 8), "" },
						{ SkillTable.getInstance().getInfo(4345, 6), "W" },
						{ SkillTable.getInstance().getInfo(4347, 12), "" },
						{ SkillTable.getInstance().getInfo(4349, 4), "" },
						{ SkillTable.getInstance().getInfo(4350, 8), "W" },
						{ SkillTable.getInstance().getInfo(4348, 12), "" },
						{ SkillTable.getInstance().getInfo(4351, 12), "M" },
						{ SkillTable.getInstance().getInfo(4352, 4), "" },
						{ SkillTable.getInstance().getInfo(4353, 12), "W" },
						{ SkillTable.getInstance().getInfo(4358, 6), "W" },
						{ SkillTable.getInstance().getInfo(4354, 8), "W" } },
				{
						{ SkillTable.getInstance().getInfo(4342, 4), "" },
						{ SkillTable.getInstance().getInfo(4343, 6), "" },
						{ SkillTable.getInstance().getInfo(4344, 6), "" },
						{ SkillTable.getInstance().getInfo(4346, 8), "" },
						{ SkillTable.getInstance().getInfo(4345, 6), "W" },
						{ SkillTable.getInstance().getInfo(4347, 12), "" },
						{ SkillTable.getInstance().getInfo(4349, 4), "" },
						{ SkillTable.getInstance().getInfo(4350, 8), "W" },
						{ SkillTable.getInstance().getInfo(4348, 12), "" },
						{ SkillTable.getInstance().getInfo(4351, 12), "M" },
						{ SkillTable.getInstance().getInfo(4352, 4), "" },
						{ SkillTable.getInstance().getInfo(4353, 12), "W" },
						{ SkillTable.getInstance().getInfo(4358, 6), "W" },
						{ SkillTable.getInstance().getInfo(4354, 8), "W" },
						{ SkillTable.getInstance().getInfo(4355, 4), "M" },
						{ SkillTable.getInstance().getInfo(4356, 4), "M" },
						{ SkillTable.getInstance().getInfo(4357, 3), "W" },
						{ SkillTable.getInstance().getInfo(4359, 4), "W" },
						{ SkillTable.getInstance().getInfo(4360, 4), "W" } } };
	}
}
