package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.ArmorSet;

public class ArmorSetsTable
{
	private static Logger _log;
	private static ArmorSetsTable _instance;
	private boolean _initialized;
	private Map<Integer, ArmorSet> _armorSets;

	public static ArmorSetsTable getInstance()
	{
		if(ArmorSetsTable._instance == null)
			ArmorSetsTable._instance = new ArmorSetsTable();
		return ArmorSetsTable._instance;
	}

	private ArmorSetsTable()
	{
		_initialized = true;
		_armorSets = new HashMap<Integer, ArmorSet>();
		loadData();
	}

	private void loadData()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill FROM armorsets");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int chest = rset.getInt("chest");
				final int legs = rset.getInt("legs");
				final int head = rset.getInt("head");
				final int gloves = rset.getInt("gloves");
				final int feet = rset.getInt("feet");
				final int skill_id = rset.getInt("skill_id");
				final int shield = rset.getInt("shield");
				final int shield_skill_id = rset.getInt("shield_skill_id");
				final int enchant6skill = rset.getInt("enchant6skill");
				_armorSets.put(chest, new ArmorSet(chest, legs, head, gloves, feet, skill_id, shield, shield_skill_id, enchant6skill));
			}
			ArmorSetsTable._log.info("ArmorSetsTable: Loaded " + _armorSets.size() + " armor sets.");
		}
		catch(Exception e)
		{
			ArmorSetsTable._log.error("ArmorSetsTable: Error reading ArmorSets table: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public boolean setExists(final int chestId)
	{
		return _armorSets.containsKey(chestId);
	}

	public ArmorSet getSet(final int chestId)
	{
		return _armorSets.get(chestId);
	}

	public boolean isInitialized()
	{
		return _initialized;
	}

	static
	{
		ArmorSetsTable._log = LoggerFactory.getLogger(ArmorSetsTable.class);
	}
}
