package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.templates.HennaTemplate;
import l2s.gameserver.templates.StatsSet;

public class HennaTable
{
	private static final Logger _log;
	private static HennaTable _instance;
	private HashMap<Integer, HennaTemplate> _henna;
	private boolean _initialized;

	public static HennaTable getInstance()
	{
		if(HennaTable._instance == null)
			HennaTable._instance = new HennaTable();
		return HennaTable._instance;
	}

	private HennaTable()
	{
		_initialized = true;
		_henna = new HashMap<Integer, HennaTemplate>();
		RestoreHennaData();
	}

	private void RestoreHennaData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet hennadata = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT symbol_id, symbol_name, dye_id, dye_amount, price, stat_INT, stat_STR, stat_CON, stat_MEM, stat_DEX, stat_WIT FROM henna");
			hennadata = statement.executeQuery();
			fillHennaTable(hennadata);
		}
		catch(Exception e)
		{
			HennaTable._log.error("error while creating henna table " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, hennadata);
		}
	}

	private void fillHennaTable(final ResultSet HennaData) throws Exception
	{
		while(HennaData.next())
		{
			final StatsSet hennaDat = new StatsSet();
			final int id = HennaData.getInt("symbol_id");
			hennaDat.set("symbol_id", id);
			hennaDat.set("dye", HennaData.getInt("dye_id"));
			hennaDat.set("price", HennaData.getInt("price"));
			hennaDat.set("amount", HennaData.getInt("dye_amount"));
			hennaDat.set("stat_INT", HennaData.getInt("stat_INT"));
			hennaDat.set("stat_STR", HennaData.getInt("stat_STR"));
			hennaDat.set("stat_CON", HennaData.getInt("stat_CON"));
			hennaDat.set("stat_MEN", HennaData.getInt("stat_MEM"));
			hennaDat.set("stat_DEX", HennaData.getInt("stat_DEX"));
			hennaDat.set("stat_WIT", HennaData.getInt("stat_WIT"));
			final HennaTemplate template = new HennaTemplate(hennaDat);
			_henna.put(id, template);
		}
		HennaTable._log.info("HennaTable: Loaded " + _henna.size() + " Templates.");
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	public HennaTemplate getTemplate(final int id)
	{
		return _henna.get(id);
	}

	static
	{
		_log = LoggerFactory.getLogger(HennaTable.class);
	}
}
