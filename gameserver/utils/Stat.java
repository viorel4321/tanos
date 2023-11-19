package l2s.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.ServerVariables;

public class Stat
{
	private static long _insertItemCounter;
	private static long _deleteItemCounter;
	private static long _updateItemCounter;
	private static long _lazyUpdateItem;
	private static long _updatePlayerBase;
	private static long _taxSum;
	private static long _taxLastUpdate;
	private static long _rouletteSum;
	private static long _rouletteLastUpdate;
	private static long _adenaSum;

	public static void init()
	{
		Stat._taxSum = ServerVariables.getLong("taxsum", 0L);
		Stat._rouletteSum = ServerVariables.getLong("rouletteSum", 0L);
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT (SELECT SUM(count) FROM items WHERE item_id=57) + (SELECT SUM(treasury) FROM castle) AS `count`");
			rset = statement.executeQuery();
			if(rset.next())
				Stat._adenaSum = rset.getLong("count");
			DbUtils.closeQuietly(statement, rset);
		}
		catch(Exception e)
		{
			System.out.println("Unable to load extended RRD stats");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public static void increaseInsertItemCount()
	{
		++Stat._insertItemCounter;
	}

	public static long getInsertItemCount()
	{
		return Stat._insertItemCounter;
	}

	public static void increaseDeleteItemCount()
	{
		++Stat._deleteItemCounter;
	}

	public static long getDeleteItemCount()
	{
		return Stat._deleteItemCounter;
	}

	public static void increaseUpdateItemCount()
	{
		++Stat._updateItemCounter;
	}

	public static long getUpdateItemCount()
	{
		return Stat._updateItemCounter;
	}

	public static void increaseLazyUpdateItem()
	{
		++Stat._lazyUpdateItem;
	}

	public static long getLazyUpdateItem()
	{
		return Stat._lazyUpdateItem;
	}

	public static void increaseUpdatePlayerBase()
	{
		++Stat._updatePlayerBase;
	}

	public static long getUpdatePlayerBase()
	{
		return Stat._updatePlayerBase;
	}

	public static void addTax(final long sum)
	{
		Stat._taxSum += sum;
		if(System.currentTimeMillis() - Stat._taxLastUpdate < 10000L)
			return;
		Stat._taxLastUpdate = System.currentTimeMillis();
		ServerVariables.set("taxsum", Stat._taxSum);
	}

	public static void addRoulette(final long sum)
	{
		Stat._rouletteSum += sum;
		if(System.currentTimeMillis() - Stat._rouletteLastUpdate < 10000L)
			return;
		Stat._rouletteLastUpdate = System.currentTimeMillis();
		ServerVariables.set("rouletteSum", Stat._rouletteSum);
	}

	public static long getTaxSum()
	{
		return Stat._taxSum;
	}

	public static long getRouletteSum()
	{
		return Stat._rouletteSum;
	}

	public static void addAdena(final long sum)
	{
		Stat._adenaSum += sum;
	}

	public static long getAdena()
	{
		return Stat._adenaSum;
	}

	static
	{
		Stat._insertItemCounter = 0L;
		Stat._deleteItemCounter = 0L;
		Stat._updateItemCounter = 0L;
		Stat._lazyUpdateItem = 0L;
		Stat._updatePlayerBase = 0L;
	}
}
