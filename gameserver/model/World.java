package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.geodata.GeoEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.CatacombSpawnManager;
import l2s.gameserver.instancemanager.DayNightSpawnManager;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Location;

public class World
{
	private static final Logger _log = LoggerFactory.getLogger(World.class);

	private static World INSTANCE = null;

	public static World getInstance() {
		if(INSTANCE == null)
			INSTANCE = new World();
		return INSTANCE;
	}

	public static final int MAP_MIN_X = Config.GEO_X_FIRST - 20 << 15;
	public static final int MAP_MAX_X = (Config.GEO_X_LAST - 19 << 15) - 1;
	public static final int MAP_MIN_Y = Config.GEO_Y_FIRST - 18 << 15;
	public static final int MAP_MAX_Y = (Config.GEO_Y_LAST - 17 << 15) - 1;
	public static final int MAP_MIN_Z = -32768;
	public static final int MAP_MAX_Z = 32767;
	public static final int WORLD_SIZE_X = Config.GEO_X_LAST - Config.GEO_X_FIRST + 1;
	public static final int WORLD_SIZE_Y = Config.GEO_Y_LAST - Config.GEO_Y_FIRST + 1;
	public static final int DIV_BY = Config.DIV_BY;
	public static final int DIV_BY_FOR_Z = Config.DIV_BY_FOR_Z;
	private static final int SMALL_DIV_BY = Config.DIV_BY / 2;
	public static final int OFFSET_X = Math.abs(World.MAP_MIN_X / World.DIV_BY);
	public static final int OFFSET_Y = Math.abs(World.MAP_MIN_Y / World.DIV_BY);
	public static final int OFFSET_Z = Math.abs(MAP_MIN_Z / World.DIV_BY_FOR_Z);
	private static final int SMALL_OFFSET_X = Math.abs(World.MAP_MIN_X / World.DIV_BY);
	private static final int SMALL_OFFSET_Y = Math.abs(World.MAP_MIN_Y / World.DIV_BY);
	private static final int REGIONS_X = World.MAP_MAX_X / World.DIV_BY + World.OFFSET_X;
	private static final int REGIONS_Y = World.MAP_MAX_Y / World.DIV_BY + World.OFFSET_Y;
	private static final int REGIONS_Z = MAP_MAX_Z / World.DIV_BY_FOR_Z + World.OFFSET_Z;
	private static final int SMALL_REGIONS_X = World.MAP_MAX_X / World.SMALL_DIV_BY + World.SMALL_OFFSET_X;
	private static final int SMALL_REGIONS_Y = World.MAP_MAX_Y / World.SMALL_DIV_BY + World.SMALL_OFFSET_Y;
	private static WorldRegion[][][] _worldRegions = new WorldRegion[World.REGIONS_X + 1][World.REGIONS_Y + 1][];
	private static WorldRegion[][] _smallWorldRegions = new WorldRegion[World.SMALL_REGIONS_X + 1][World.SMALL_REGIONS_Y + 1];

	private final int defaultGeoIndex;

	private World() {
		defaultGeoIndex = GeoEngine.createGeoIndex();
	}

	public int getDefaultGeoIndex() {
		return defaultGeoIndex;
	}

	static
	{
		final String[] split_regions = Config.VERTICAL_SPLIT_REGIONS.split(";");
		for(int x = 1; x <= World.REGIONS_X; ++x)
			for(int y = 1; y <= World.REGIONS_Y; ++y)
			{
				final int wx = (x - World.OFFSET_X) * World.DIV_BY + World.DIV_BY / 2;
				final int wy = (y - World.OFFSET_Y) * World.DIV_BY + World.DIV_BY / 2;
				if(split_regions.equals(wx + "_" + wy))
					World._worldRegions[x][y] = new WorldRegion[World.REGIONS_Z + 1];
				else
					World._worldRegions[x][y] = new WorldRegion[1];
			}
	}

	public static List<WorldRegion> getNeighbors(final int x, final int y, final int z, final boolean small)
	{
		final List<WorldRegion> neighbors = new ArrayList<WorldRegion>();
		if(small)
		{
			for(int a = -Config.VIEW_OFFSET; a <= Config.VIEW_OFFSET; ++a)
				for(int b = -Config.VIEW_OFFSET; b <= Config.VIEW_OFFSET; ++b)
					if(validRegion(x + a, y + b, 0, true) && World._smallWorldRegions[x + a][y + b] != null)
						neighbors.add(World._smallWorldRegions[x + a][y + b]);
		}
		else
			for(int a = -Config.VIEW_OFFSET; a <= Config.VIEW_OFFSET; ++a)
				for(int b = -Config.VIEW_OFFSET; b <= Config.VIEW_OFFSET; ++b)
					if(validRegion(x + a, y + b, 0, false))
						if(World._worldRegions[x + a][y + b].length > 1)
						{
							for(int c = -Config.VIEW_OFFSET; c <= Config.VIEW_OFFSET; ++c)
								if(validRegion(x + a, y + b, z + c, false) && World._worldRegions[x + a][y + b][z + c] != null)
									neighbors.add(World._worldRegions[x + a][y + b][z + c]);
						}
						else if(World._worldRegions[x + a][y + b][0] != null)
							neighbors.add(World._worldRegions[x + a][y + b][0]);
		return neighbors;
	}

	public static List<WorldRegion> getNeighborsZ(final int x, final int y, final int z1, final int z2)
	{
		final List<WorldRegion> neighbors = new ArrayList<WorldRegion>();
		final int _x = x / World.DIV_BY + World.OFFSET_X;
		final int _y = y / World.DIV_BY + World.OFFSET_Y;
		final int _z1 = z1 / World.DIV_BY_FOR_Z + World.OFFSET_Z;
		final int _z2 = z2 / World.DIV_BY_FOR_Z + World.OFFSET_Z;
		for(int a = -1; a <= 1; ++a)
			for(int b = -1; b <= 1; ++b)
				if(validRegion(_x + a, _y + b, 0, false))
					if(World._worldRegions[_x + a][_y + b].length > 1)
					{
						for(int c = _z1; c <= _z2; ++c)
							if(validRegion(_x + a, _y + b, c, false) && World._worldRegions[_x + a][_y + b][c] != null)
								neighbors.add(World._worldRegions[_x + a][_y + b][c]);
					}
					else if(World._worldRegions[_x + a][_y + b][0] != null)
						neighbors.add(World._worldRegions[_x + a][_y + b][0]);
		return neighbors;
	}

	public static boolean validRegion(final int x, final int y, final int z, final boolean small)
	{
		if(small)
			return x >= 1 && x <= World.SMALL_REGIONS_X && y >= 1 && y <= World.SMALL_REGIONS_Y;
		return x >= 1 && x <= World.REGIONS_X && y >= 1 && y <= World.REGIONS_Y && z >= 0 && z < World._worldRegions[x][y].length;
	}

	public static WorldRegion getRegion(final Location loc)
	{
		return getRegion(loc.x, loc.y, loc.z, false);
	}

	public static WorldRegion getRegion(final GameObject obj)
	{
		return getRegion(obj.getX(), obj.getY(), obj.getZ(), false);
	}

	public static WorldRegion getRegion(final int x, final int y, final int z, final boolean small)
	{
		if(small)
		{
			final int _x = x / World.SMALL_DIV_BY + World.SMALL_OFFSET_X;
			final int _y = y / World.SMALL_DIV_BY + World.SMALL_OFFSET_Y;
			if(validRegion(_x, _y, 0, true))
			{
				if(World._smallWorldRegions[_x][_y] == null)
					World._smallWorldRegions[_x][_y] = new WorldRegion(_x, _y, 0, true);
				return World._smallWorldRegions[_x][_y];
			}
		}
		else
		{
			final int _x = x / World.DIV_BY + World.OFFSET_X;
			final int _y = y / World.DIV_BY + World.OFFSET_Y;
			if(validRegion(_x, _y, 0, false))
			{
				int _z = 0;
				if(World._worldRegions[_x][_y].length > 1)
				{
					_z = z / World.DIV_BY_FOR_Z + World.OFFSET_Z;
					if(!validRegion(_x, _y, _z, false))
						return null;
				}
				if(World._worldRegions[_x][_y][_z] == null)
					World._worldRegions[_x][_y][_z] = new WorldRegion(_x, _y, _z, false);
				return World._worldRegions[_x][_y][_z];
			}
		}
		return null;
	}

	public static void removeObject(final GameObject object)
	{
		if(object != null)
			object.clearTerritories();
	}

	public static Player getPlayer(final String name)
	{
		return GameObjectsStorage.getPlayer(name);
	}

	public static void addVisibleObject(final GameObject object, final Creature dropper)
	{
		if(object == null || !object.isVisible() || object.inObserverMode() && object.getOlympiadObserveId() == -1)
			return;
		final WorldRegion region = getRegion(object);
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(region == null || currentRegion != null && currentRegion.equals(region))
			return;
		if(currentRegion == null)
		{
			for(final WorldRegion neighbor : region.getNeighbors())
				neighbor.addToPlayers(object, dropper);
			region.addObject(object);
			object.setCurrentRegion(region);
		}
		else
		{
			currentRegion.removeObject(object, true);
			region.addObject(object);
			object.setCurrentRegion(region);
			final List<WorldRegion> oldNeighbors = currentRegion.getNeighbors();
			final List<WorldRegion> newNeighbors = region.getNeighbors();
			for(final WorldRegion neighbor2 : oldNeighbors)
			{
				boolean flag = true;
				for(final WorldRegion newneighbor : newNeighbors)
					if(newneighbor != null && newneighbor.equals(neighbor2))
					{
						flag = false;
						break;
					}
				if(flag)
					neighbor2.removeFromPlayers(object, true);
			}
			for(final WorldRegion neighbor2 : newNeighbors)
			{
				boolean flag = true;
				for(final WorldRegion oldneighbor : oldNeighbors)
					if(oldneighbor != null && oldneighbor.equals(neighbor2))
					{
						flag = false;
						break;
					}
				if(flag)
					neighbor2.addToPlayers(object, dropper);
			}
		}
	}

	public static void removeVisibleObject(final GameObject object)
	{
		if(object == null || object.isVisible() || object.inObserverMode() && object.getOlympiadObserveId() == -1)
			return;
		if(object.getCurrentRegion() != null)
		{
			object.getCurrentRegion().removeObject(object, false);
			if(object.getCurrentRegion() != null)
				for(final WorldRegion neighbor : object.getCurrentRegion().getNeighbors())
					neighbor.removeFromPlayers(object, false);
			object.setCurrentRegion(null);
		}
	}

	public static boolean validCoords(final int x, final int y)
	{
		return x > World.MAP_MIN_X && x < World.MAP_MAX_X && y > World.MAP_MIN_Y && y < World.MAP_MAX_Y;
	}

	public static int validCoordX(int x)
	{
		if(x < World.MAP_MIN_X)
			x = World.MAP_MIN_X + 1;
		else if(x > World.MAP_MAX_X)
			x = World.MAP_MAX_X - 1;
		return x;
	}

	public static int validCoordY(int y)
	{
		if(y < World.MAP_MIN_Y)
			y = World.MAP_MIN_Y + 1;
		else if(y > World.MAP_MAX_Y)
			y = World.MAP_MAX_Y - 1;
		return y;
	}

	public static int validCoordZ(int z)
	{
		if(z < MAP_MIN_Z)
			z = -MAP_MAX_Z;
		else if(z > MAP_MAX_Z)
			z = 32766;
		return z;
	}

	public static synchronized void deleteVisibleNpcSpawns()
	{
		RaidBossSpawnManager.getInstance().cleanUp();
		DayNightSpawnManager.getInstance().cleanUp();
		CatacombSpawnManager.getInstance().cleanUp();
		World._log.info("Deleting all visible NPC's...");
		for(int i = 1; i <= World.REGIONS_X; ++i)
			for(int j = 1; j <= World.REGIONS_Y; ++j)
				if(World._worldRegions[i][j].length > 1)
				{
					for(int k = 0; k < World.REGIONS_Z; ++k)
						if(World._worldRegions[i][j][k] != null)
							World._worldRegions[i][j][k].deleteVisibleNpcSpawns();
				}
				else if(World._worldRegions[i][j][0] != null)
					World._worldRegions[i][j][0].deleteVisibleNpcSpawns();
		for(int i = 1; i <= World.SMALL_REGIONS_X; ++i)
			for(int j = 1; j <= World.SMALL_REGIONS_Y; ++j)
				if(World._smallWorldRegions[i][j] != null)
					World._smallWorldRegions[i][j].deleteVisibleNpcSpawns();
		World._log.info("All visible NPC's deleted.");
	}

	public static GameObject getAroundObjectById(final GameObject object, final Integer id)
	{
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return null;
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getObjectsSize();
		for(final WorldRegion region : neighbors)
			for(final GameObject o : region.getObjectsList(new ArrayList<GameObject>(size), object.getObjectId(), object.getReflectionId()))
				if(o != null && o.getObjectId() == id)
					return o;
		return null;
	}

	public static List<GameObject> getAroundObjects(final GameObject object)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getObjectsSize();
		final List<GameObject> result = new ArrayList<GameObject>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getObjectsList(result, oid, object.getReflectionId());
		return result;
	}

	public static List<GameObject> getAroundObjects(final GameObject object, final int radius, final int height)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getObjectsSize();
		final List<GameObject> result = new ArrayList<GameObject>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getObjectsList(result, oid, object.getReflectionId(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static List<Creature> getAroundCharacters(final GameObject object)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getObjectsSize();
		final List<Creature> result = new ArrayList<Creature>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getCharactersList(result, oid, object.getReflectionId());
		return result;
	}

	public static List<Creature> getAroundCharacters(final GameObject object, final int radius, final int height)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getObjectsSize();
		final List<Creature> result = new ArrayList<Creature>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getCharactersList(result, oid, object.getReflectionId(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static List<NpcInstance> getAroundNpc(final GameObject object)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getObjectsSize();
		final List<NpcInstance> result = new ArrayList<NpcInstance>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getNpcsList(result, oid, object.getReflectionId());
		return result;
	}

	public static List<NpcInstance> getAroundNpc(final GameObject object, final int radius, final int height)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getObjectsSize();
		final List<NpcInstance> result = new ArrayList<NpcInstance>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getNpcsList(result, oid, object.getReflectionId(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static List<Playable> getAroundPlayables(final GameObject object)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getPlayersSize();
		final List<Playable> result = new ArrayList<Playable>(size * 2);
		for(final WorldRegion region2 : neighbors)
			region2.getPlayablesList(result, oid, object.getReflectionId());
		return result;
	}

	public static List<Playable> getAroundPlayables(final GameObject object, final int radius, final int height)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 0;
		for(final WorldRegion region : neighbors)
			size += region.getPlayersSize();
		final List<Playable> result = new ArrayList<Playable>(size * 2);
		for(final WorldRegion region2 : neighbors)
			region2.getPlayablesList(result, oid, object.getReflectionId(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static List<Player> getAroundPlayers(final GameObject object)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 10;
		for(final WorldRegion region : neighbors)
			size += region.getPlayersSize();
		final List<Player> result = new ArrayList<Player>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getPlayersList(result, oid, object.getReflectionId());
		return result;
	}

	public static List<Player> getAroundPlayers(final GameObject object, final int radius, final int height)
	{
		final int oid = object.getObjectId();
		final WorldRegion currentRegion = object.getCurrentRegion();
		if(currentRegion == null)
			return Collections.emptyList();
		final List<WorldRegion> neighbors = currentRegion.getNeighbors();
		int size = 10;
		for(final WorldRegion region : neighbors)
			size += region.getPlayersSize();
		final List<Player> result = new ArrayList<Player>(size);
		for(final WorldRegion region2 : neighbors)
			region2.getPlayersList(result, oid, object.getReflectionId(), object.getX(), object.getY(), object.getZ(), radius * radius, height);
		return result;
	}

	public static boolean isAroundPlayers(final GameObject object)
	{
		return object.getCurrentRegion() != null && !object.getCurrentRegion().areNeighborsEmpty();
	}

	public static void addTerritory(final Territory territory)
	{
		final int xmin = territory.getXmin() / World.DIV_BY + World.OFFSET_X;
		final int ymin = territory.getYmin() / World.DIV_BY + World.OFFSET_Y;
		final int zmin = territory.getZmin() / World.DIV_BY_FOR_Z + World.OFFSET_Z;
		final int xmax = territory.getXmax() / World.DIV_BY + World.OFFSET_X;
		final int ymax = territory.getYmax() / World.DIV_BY + World.OFFSET_Y;
		final int zmax = territory.getZmax() / World.DIV_BY_FOR_Z + World.OFFSET_Z;
		for(int x = xmin; x <= xmax; ++x)
			for(int y = ymin; y <= ymax; ++y)
				if(validRegion(x, y, 0, false))
					if(World._worldRegions[x][y].length > 1)
					{
						for(int z = zmin; z <= zmax; ++z)
							if(validRegion(x, y, z, false))
							{
								if(World._worldRegions[x][y][z] == null)
									World._worldRegions[x][y][z] = new WorldRegion(x, y, z, false);
								World._worldRegions[x][y][z].addTerritory(territory);
							}
					}
					else
					{
						if(World._worldRegions[x][y][0] == null)
							World._worldRegions[x][y][0] = new WorldRegion(x, y, 0, false);
						World._worldRegions[x][y][0].addTerritory(territory);
					}
	}

	public static void removeTerritory(final Territory territory)
	{
		final int xmin = territory.getXmin() / World.DIV_BY + World.OFFSET_X;
		final int ymin = territory.getYmin() / World.DIV_BY + World.OFFSET_Y;
		final int zmin = territory.getZmin() / World.DIV_BY_FOR_Z + World.OFFSET_Z;
		final int xmax = territory.getXmax() / World.DIV_BY + World.OFFSET_X;
		final int ymax = territory.getYmax() / World.DIV_BY + World.OFFSET_Y;
		final int zmax = territory.getZmax() / World.DIV_BY_FOR_Z + World.OFFSET_Z;
		for(int x = xmin; x <= xmax; ++x)
			for(int y = ymin; y <= ymax; ++y)
				if(validRegion(x, y, 0, false))
					if(World._worldRegions[x][y].length > 1)
					{
						for(int z = zmin; z <= zmax; ++z)
							if(validRegion(x, y, z, false) && World._worldRegions[x][y][z] != null)
								World._worldRegions[x][y][z].removeTerritory(territory);
					}
					else if(World._worldRegions[x][y][0] != null)
						World._worldRegions[x][y][0].removeTerritory(territory);
	}

	public static List<Territory> getTerritories(final int x, final int y, final int z)
	{
		final WorldRegion region = getRegion(x, y, z, false);
		return region == null ? null : region.getTerritories(x, y, z);
	}

	public static void getZones(final List<Zone> inside, final Location loc)
	{
		final List<Territory> territories = getTerritories(loc.x, loc.y, loc.z);
		if(territories != null)
			for(final Territory terr : territories)
				if(terr != null && terr.getZone() != null && terr.getZone().checkIfInZone(loc.x, loc.y, loc.z))
					inside.add(terr.getZone());
	}

	public static Territory getWater(final Location loc)
	{
		final List<Territory> territories = getTerritories(loc.x, loc.y, loc.z);
		if(territories != null)
			for(final Territory terr : territories)
				if(terr != null && terr.getZone() != null && terr.getZone().getType() == Zone.ZoneType.water)
					return terr;
		return null;
	}

	public static boolean isWater(final Location loc)
	{
		return getWater(loc) != null;
	}

	public static WorldRegion[][][] getRegions()
	{
		return World._worldRegions;
	}

	public static int getRegionsCount(final Boolean active)
	{
		int ret = 0;
		for(final WorldRegion[][] wr0 : World._worldRegions)
			if(wr0 != null)
				for(final WorldRegion[] wr2 : wr0)
					if(wr2 != null)
						for(final WorldRegion wr3 : wr2)
							if(wr3 != null)
								if(active == null || wr3.areNeighborsEmpty() != active)
									++ret;
		return ret;
	}
}
