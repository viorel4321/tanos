package l2s.gameserver.idfactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.list.array.TIntArrayList;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;

public abstract class IdFactory
{
	private static final Logger _log;
	public static final String[][] EXTRACT_OBJ_ID_TABLES;
	public static final int FIRST_OID = 268435456;
	public static final int LAST_OID = Integer.MAX_VALUE;
	public static final int FREE_OBJECT_ID_SIZE = 1879048191;
	protected static final IdFactory _instance;
	protected boolean initialized;
	protected long releasedCount;

	public static final IdFactory getInstance()
	{
		return IdFactory._instance;
	}

	protected IdFactory()
	{
		releasedCount = 0L;
		resetOnlineStatus();
	}

	private void resetOnlineStatus()
	{
		Connection con = null;
		Statement st = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.createStatement();
			st.executeUpdate("UPDATE characters SET online = 0");
			IdFactory._log.info("IdFactory: Clear characters online status.");
		}
		catch(SQLException e)
		{
			IdFactory._log.warn("" + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, st);
		}
	}

	protected int[] extractUsedObjectIDTable() throws SQLException
	{
		final TIntArrayList objectIds = new TIntArrayList();
		Connection con = null;
		Statement st = null;
		ResultSet rs = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			st = con.createStatement();
			for(final String[] table : IdFactory.EXTRACT_OBJ_ID_TABLES)
			{
				rs = st.executeQuery("SELECT " + table[1] + " FROM " + table[0]);
				int size = objectIds.size();
				while(rs.next())
					objectIds.add(rs.getInt(1));
				DbUtils.close(rs);
				size = objectIds.size() - size;
				if(size > 0)
					IdFactory._log.info("IdFactory: Extracted " + size + " used id's from " + table[0]);
			}
		}
		finally
		{
			DbUtils.closeQuietly(con, st, rs);
		}
		final int[] extracted = objectIds.toArray();
		Arrays.sort(extracted);
		IdFactory._log.info("IdFactory: Extracted total " + extracted.length + " used id's.");
		return extracted;
	}

	public boolean isInitialized()
	{
		return initialized;
	}

	public abstract int getNextId();

	public void releaseId(final int id)
	{
		++releasedCount;
	}

	public long getReleasedCount()
	{
		return releasedCount;
	}

	public abstract int size();

	static
	{
		_log = LoggerFactory.getLogger(IdFactory.class);
		EXTRACT_OBJ_ID_TABLES = new String[][] {
				{ "characters", "obj_id" },
				{ "items", "object_id" },
				{ "clan_data", "clan_id" },
				{ "ally_data", "ally_id" },
				{ "pets", "objId" },
				{ "couples", "id" } };
		_instance = new BitSetIDFactory();
	}
}
