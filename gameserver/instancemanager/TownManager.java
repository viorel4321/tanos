package l2s.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.Town;
import l2s.gameserver.tables.MapRegionTable;

public final class TownManager
{
	private static TownManager _instance;
	private List<Town> _towns;
	private static final Logger _log;

	private TownManager()
	{
		_towns = new ArrayList<Town>();
		final List<Zone> zones = ZoneManager.getInstance().getZoneByType(Zone.ZoneType.Town);
		if(zones.size() == 0)
			TownManager._log.warn("Not found zones for Towns!!!");
		else
			for(final Zone zone : zones)
				_towns.add(new Town(zone.getIndex()));
	}

	public static TownManager getInstance()
	{
		if(TownManager._instance == null)
			TownManager._instance = new TownManager();
		return TownManager._instance;
	}

	public Town getClosestTown(final int x, final int y)
	{
		return getTown(MapRegionTable.getInstance().getMapRegion(x, y));
	}

	public Town getClosestTown(final GameObject activeObject)
	{
		return getTown(MapRegionTable.getInstance().getMapRegion(activeObject.getX(), activeObject.getY()));
	}

	public int getClosestTownNumber(final Creature activeChar)
	{
		return MapRegionTable.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
	}

	public String getClosestTownName(final Creature activeChar)
	{
		return this.getClosestTown(activeChar).getName();
	}

	public Town getTown(final int townId)
	{
		for(final Town town : _towns)
			if(town.getTownId() == townId)
				return town;
		return null;
	}

	static
	{
		_log = LoggerFactory.getLogger(TownManager.class);
	}
}
