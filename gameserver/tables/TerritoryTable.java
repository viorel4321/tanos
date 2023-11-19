package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.geometry.Polygon;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.World;
import l2s.gameserver.utils.Location;

public class TerritoryTable
{
	private static Logger _log = LoggerFactory.getLogger(TerritoryTable.class);
	private static final TerritoryTable _instance = new TerritoryTable();
	private static HashMap<Integer, Territory> _locations;

	public static int locId = 70000;

	public static TerritoryTable getInstance()
	{
		return _instance;
	}

	private TerritoryTable()
	{
		reloadData();
	}

	public Territory getLocation(final int terr)
	{
		final Territory t = _locations.get(terr);
		if(t == null)
			_log.warn("TerritoryTable.getLocation: territory " + terr + " not found.");
		return t;
	}

	public Location getRandomLoc(final int terr, final int geoIndex)
	{
		final Territory t = _locations.get(terr);
		if(t == null)
		{
			_log.warn("TerritoryTable.getRandomLoc: territory " + terr + " not found.");
			return new Location();
		}
		return t.getRandomLoc(geoIndex);
	}

	public int getMinZ(final int terr)
	{
		final Territory t = _locations.get(terr);
		if(t == null)
		{
			_log.warn("TerritoryTable.getMinZ: territory " + terr + " not found.");
			return 0;
		}
		return t.getZmin();
	}

	public int getMaxZ(final int terr)
	{
		final Territory t = _locations.get(terr);
		if(t == null)
		{
			_log.warn("TerritoryTable.getMaxZ: territory " + terr + " not found.");
			return 0;
		}
		return t.getZmax();
	}

	public void reloadData()
	{
		if(_locations != null)
			for(final Territory terr : _locations.values())
				if(terr.isWorldTerritory())
					World.removeTerritory(terr);
		_locations = new HashMap<Integer, Territory>();
		final HashMap<Integer, Polygon> locs = new HashMap<Integer, Polygon>();
		final HashMap<Integer, HashMap<String, Polygon>> banned = new HashMap<Integer, HashMap<String, Polygon>>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT loc_id, name, loc_x, loc_y, loc_zmin, loc_zmax FROM `locations`");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int id = rset.getInt("loc_id");
				final String name = rset.getString("name");
				if(name.startsWith("banned"))
				{
					if(banned.get(id) == null)
					{
						final HashMap<String, Polygon> list = new HashMap<String, Polygon>();
						list.put(name, new Polygon());
						banned.put(id, list);
					}
					else if(banned.get(id).get(name) == null)
						banned.get(id).put(name, new Polygon());
					banned.get(id).get(name).add(rset.getInt("loc_x"), rset.getInt("loc_y")).setZmin(rset.getInt("loc_zmin")).setZmax(rset.getInt("loc_zmax"));
				}
				else
				{
					if(locs.get(id) == null)
						locs.put(id, new Polygon());
					locs.get(id).add(rset.getInt("loc_x"), rset.getInt("loc_y")).setZmin(rset.getInt("loc_zmin")).setZmax(rset.getInt("loc_zmax"));
				}
			}
		}
		catch(Exception e1)
		{
			_log.error("locations couldn't be initialized: ", e1);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		for(final int id2 : locs.keySet())
		{
			final Territory t = new Territory(id2);
			final Polygon temp = locs.get(id2);
			if(!temp.validate())
				_log.warn("Invalid territory: " + id2);
			t.add(temp);
			_locations.put(id2, t);
		}
		_log.info("TerritoryTable: Loaded " + _locations.size() + " locations");
		for(final int id2 : banned.keySet())
			if(_locations.containsKey(id2))
				for(final Polygon temp : banned.get(id2).values())
				{
					if(!temp.validate())
						_log.warn("Invalid banned territory: " + id2);
					_locations.get(id2).addBanned(temp);
				}
	}

	public void registerZones()
	{
		int registered = 0;
		for(final Territory terr : _locations.values())
			if(terr.isWorldTerritory())
			{
				World.addTerritory(terr);
				++registered;
			}
		_log.info("TerritoryTable: Added " + registered + " locations to L2World");
	}

	public HashMap<Integer, Territory> getLocations()
	{
		return _locations;
	}

	public static void unload()
	{
		_locations.clear();
	}
}
