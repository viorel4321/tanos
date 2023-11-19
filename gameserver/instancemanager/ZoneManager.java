package l2s.gameserver.instancemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.geometry.Polygon;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.listener.ZoneEnterLeaveListener;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.RoundTerritory;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.tables.TerritoryTable;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.XmlUtils;

public class ZoneManager
{
	private static final Logger _log = LoggerFactory.getLogger(ZoneManager.class);

	private static ZoneManager _instance;
	private static Map<Zone.ZoneType, List<Zone>> _zonesByType;
	private static Map<String, Zone> _zones;
	private final NoLandingZoneListener _noLandingZoneListener;

	private ZoneManager()
	{
		_noLandingZoneListener = new NoLandingZoneListener();
		parse();
	}

	public static ZoneManager getInstance()
	{
		if(_instance == null)
			_instance = new ZoneManager();
		return _instance;
	}

	public boolean checkIfInZone(final Zone.ZoneType zoneType, final GameObject object)
	{
		return this.checkIfInZone(zoneType, object.getX(), object.getY(), object.getZ());
	}

	public boolean checkIfInZone(final Zone.ZoneType zoneType, final int x, final int y)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return false;
		for(final Zone zone : list)
			if(zone.isActive() && zone.getLoc() != null && zone.getLoc().isInside(x, y))
				return true;
		return false;
	}

	public boolean checkIfInZone(final Zone.ZoneType zoneType, final int x, final int y, final int z)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return false;
		for(final Zone zone : list)
			if(zone.isActive() && zone.getLoc() != null && zone.getLoc().isInside(x, y) && z >= zone.getLoc().getZmin() && z <= zone.getLoc().getZmax())
				return true;
		return false;
	}

	public boolean checkIfInZoneAndIndex(final Zone.ZoneType zoneType, final int index, final GameObject object)
	{
		return this.checkIfInZoneAndIndex(zoneType, index, object.getX(), object.getY(), object.getZ());
	}

	public boolean checkIfInZoneAndIndex(final Zone.ZoneType zoneType, final int index, final int x, final int y, final int z)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return false;
		for(final Zone zone : list)
			if(zone.isActive() && zone.getIndex() == index && zone.getLoc() != null && zone.getLoc().isInside(x, y) && z >= zone.getLoc().getZmin() && z <= zone.getLoc().getZmax())
				return true;
		return false;
	}

	public boolean checkInZoneAndIndex(final Zone.ZoneType zoneType, final int index, final int x, final int y, final int z)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return false;
		for(final Zone zone : list)
			if(zone.getIndex() == index && zone.getLoc() != null && zone.getLoc().isInside(x, y) && z >= zone.getLoc().getZmin() && z <= zone.getLoc().getZmax())
				return true;
		return false;
	}

	public Zone getZoneByType(final Zone.ZoneType zoneType, final int x, final int y, final boolean onlyActive)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;
		for(final Zone zone : list)
			if((!onlyActive || zone.isActive()) && zone.getLoc() != null && zone.getLoc().isInside(x, y))
				return zone;
		return null;
	}

	public List<Zone> getZoneByType(final Zone.ZoneType zoneType)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		final List<Zone> result = new ArrayList<Zone>();
		if(list == null)
			return result;
		for(final Zone zone : list)
			if(zone.isActive())
				result.add(zone);
		return result;
	}

	public Zone getZoneByIndex(final Zone.ZoneType zoneType, final int index, final boolean onlyActive)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;
		for(final Zone zone : list)
			if((!onlyActive || zone.isActive()) && zone.getIndex() == index)
				return zone;
		return null;
	}

	public Zone getZoneById(final Zone.ZoneType zoneType, final int id, final boolean onlyActive)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;
		for(final Zone zone : list)
			if((!onlyActive || zone.isActive()) && zone.getId() == id)
				return zone;
		return null;
	}

	public Zone getZoneByTypeAndObject(final Zone.ZoneType zoneType, final GameObject object)
	{
		return getZoneByTypeAndCoords(zoneType, object.getX(), object.getY(), object.getZ());
	}

	public Zone getZoneByTypeAndCoords(final Zone.ZoneType zoneType, final int x, final int y, final int z)
	{
		final List<Zone> list = _zonesByType.get(zoneType);
		if(list == null)
			return null;
		for(final Zone zone : list)
			if(zone.isActive() && zone.getLoc() != null && zone.getLoc().isInside(x, y, z))
				return zone;
		return null;
	}

	public Zone getZone(final String name)
	{
		return _zones.get(name);
	}

	private void parse()
	{
		_zonesByType = new HashMap<Zone.ZoneType, List<Zone>>();
		_zones = new HashMap<String, Zone>();
		Config.ZONE_EQUIP = false;
		final List<File> files = new ArrayList<File>();
		hashFiles("zone", files);
		int count = 0;
		for(final File f : files)
			count += parseFile(f);
		_log.info("ZoneManager: Loaded " + count + " zones");
		TerritoryTable.getInstance().registerZones();
	}

	private void hashFiles(final String dirname, final List<File> hash)
	{
		final File dir = new File(Config.DATAPACK_ROOT, "data/" + dirname);
		if(!dir.exists())
		{
			_log.info("Dir " + dir.getAbsolutePath() + " not exists");
			return;
		}
		final File[] listFiles;
		final File[] files = listFiles = dir.listFiles();
		for(final File f : listFiles)
			if(f.getName().endsWith(".xml"))
				hash.add(f);
			else if(f.isDirectory() && !f.getName().equals(".svn"))
				hashFiles(dirname + "/" + f.getName(), hash);
	}

	public int parseFile(final File f)
	{
		Document doc = null;
		try
		{
			doc = XmlUtils.readFile(f);
		}
		catch(Exception e)
		{
			_log.error("zones file couldnt be initialized: " + f, e);
			return 0;
		}
		try
		{
			return parseDocument(doc);
		}
		catch(Exception e)
		{
			_log.error("zones file couldnt be initialized: " + f, e);
			return 0;
		}
	}

	private int parseDocument(final Document doc)
	{
		int count = 0;
		for(final Element zone : doc.getRootElement().elements())
		{
			final Zone z = new Zone(XmlUtils.getIntValue(zone, "id", 0));
			z.setType(Zone.ZoneType.valueOf(zone.attributeValue("type")));
			z.setName(zone.attributeValue("name"));
			boolean enabled = true;
			for(final Element set : zone.elements("set"))
			{
				final String name = set.attributeValue("name");
				if("index".equalsIgnoreCase(name))
					z.setIndex(XmlUtils.getIntValue(set, "val", 0));
				else if("taxById".equalsIgnoreCase(name))
					z.setTaxById(XmlUtils.getIntValue(set, "val", 0));
				else if("entering_message_no".equalsIgnoreCase(name))
					z.setEnteringMessageId(XmlUtils.getIntValue(set, "val", 0));
				else if("leaving_message_no".equalsIgnoreCase(name))
					z.setLeavingMessageId(XmlUtils.getIntValue(set, "val", 0));
				else if("entering_message".equalsIgnoreCase(name))
					z.setEnteringMessage(set.attributeValue("val"));
				else if("leaving_message".equalsIgnoreCase(name))
					z.setLeavingMessage(set.attributeValue("val"));
				else if("target".equalsIgnoreCase(name))
					z.setTarget(set.attributeValue("val"));
				else if("skill_name".equalsIgnoreCase(name))
					z.setSkill(set.attributeValue("val"));
				else if("skill_prob".equalsIgnoreCase(name))
					z.setSkillProb(set.attributeValue("val"));
				else if("unit_tick".equalsIgnoreCase(name))
					z.setUnitTick(set.attributeValue("val"));
				else if("initial_delay".equalsIgnoreCase(name))
					z.setInitialDelay(set.attributeValue("val"));
				else if("restart_time".equalsIgnoreCase(name))
					z.setRestartTime(XmlUtils.getLongValue(set, "val", 0L));
				else if("blocked_actions".equalsIgnoreCase(name))
					z.setBlockedActions(set.attributeValue("val"));
				else if("damage_on_hp".equalsIgnoreCase(name))
					z.setDamageOnHP(set.attributeValue("val"));
				else if("damage_on_mp".equalsIgnoreCase(name))
					z.setDamageOn(set.attributeValue("val"));
				else if("message_no".equalsIgnoreCase(name))
					z.setMessageNumber(set.attributeValue("val"));
				else if("move_bonus".equalsIgnoreCase(name))
					z.setMoveBonus(set.attributeValue("val"));
				else if("hp_regen_bonus".equalsIgnoreCase(name))
					z.setRegenBonusHP(set.attributeValue("val"));
				else if("mp_regen_bonus".equalsIgnoreCase(name))
					z.setRegenBonusMP(set.attributeValue("val"));
				else if("affect_race".equalsIgnoreCase(name))
					z.setAffectRace(set.attributeValue("val"));
				else if("event".equalsIgnoreCase(name))
					z.setEvent(set.attributeValue("val"));
				else if("eventId".equalsIgnoreCase(name))
					z.setEventId(XmlUtils.getIntValue(set, "val", 0));
				else if("enabled".equalsIgnoreCase(name))
					enabled = XmlUtils.getBooleanValue(set, "val", true) || z.getType() == Zone.ZoneType.water;
				else if("restrictSkills".equalsIgnoreCase(name))
					z.setRestrictSkills(XmlUtils.getIntArray(set, "val", ",", new int[0]));
				else if("restrictEquip".equalsIgnoreCase(name))
					z.setRestrictEquip(XmlUtils.getIntArray(set, "val", ",", new int[0]));
				else if("affect_race".equalsIgnoreCase(name))
					z.setAffectRace(set.attributeValue("val"));
				else if("private_store_currecy".equalsIgnoreCase(name))
					z.setPrivateStoreCurrecy(XmlUtils.getIntValue(set, "val", 0));
				else
					z.setParam(name, set.attributeValue("val"));
			}
			final Iterator<Element> j = zone.elementIterator();
			while(j.hasNext())
			{
				final Element e = j.next();
				final boolean shape = e.getName().startsWith("shape");
				if(!shape && !e.getName().startsWith("restart_point") && !e.getName().startsWith("PKrestart_point"))
				{
					if(!Config.ALLOW_PVP_ZONES_MOD)
						continue;
					if(!e.getName().startsWith("adv_restart_point"))
						continue;
				}
				int locId2;
				if(shape)
					TerritoryTable.locId = (locId2 = TerritoryTable.locId) + 1;
				else
					locId2 = 0;
				final int locId = locId2;
				final boolean isRound = e.attributeValue("loc") != null;
				Territory territory = null;
				List<Location> points = null;
				Polygon temp = null;
				if(isRound)
				{
					final String[] coord = e.attributeValue("loc").replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim().split(" ");
					if(coord.length < 5)
						territory = new RoundTerritory(locId, Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2]), Integer.MIN_VALUE, Integer.MAX_VALUE);
					else
						territory = new RoundTerritory(locId, Integer.parseInt(coord[0]), Integer.parseInt(coord[1]), Integer.parseInt(coord[2]), Integer.parseInt(coord[3]), Integer.parseInt(coord[4]));
				}
				else
				{
					if(shape)
						temp = new Polygon();
					else
						points = new ArrayList<Location>();
					for(final Element coords : e.elements("coords"))
					{
						final String[] coord2 = coords.attributeValue("loc").replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim().split(" ");
						if(shape)
						{
							if(coord2.length < 4)
								temp.add(Integer.parseInt(coord2[0]), Integer.parseInt(coord2[1])).setZmin(Integer.MIN_VALUE).setZmax(Integer.MAX_VALUE);
							else
								temp.add(Integer.parseInt(coord2[0]), Integer.parseInt(coord2[1])).setZmin(Integer.parseInt(coord2[2])).setZmax(Integer.parseInt(coord2[3]));
						}
						else
							points.add(new Location(Integer.parseInt(coord2[0]), Integer.parseInt(coord2[1]), Integer.parseInt(coord2[2])));
					}
					if(shape)
					{
						territory = new Territory(locId);
						territory.add(temp);
					}
				}
				if("shape".equalsIgnoreCase(e.getName()))
				{
					z.setLoc(territory);
					territory.setZone(z);
					if(temp != null && !temp.validate())
						_log.warn("Invalid territory in zone: " + z.getName());
				}
				else if("restart_point".equalsIgnoreCase(e.getName()))
					z.setRestartPoints(points);
				else if("PKrestart_point".equalsIgnoreCase(e.getName()))
					z.setPKRestartPoints(points);
				else if("adv_restart_point".equalsIgnoreCase(e.getName()))
					z.setAdvRestartPoints(points);
				z.setActive(enabled);
				if(shape)
					TerritoryTable.getInstance().getLocations().put(locId, territory);
			}
			if(z.getType() == Zone.ZoneType.no_landing || z.getType() == Zone.ZoneType.Siege || z.getType() == Zone.ZoneType.OlympiadStadia)
				z.getListenerEngine().addMethodInvokedListener(_noLandingZoneListener);
			if(_zonesByType.get(z.getType()) == null)
				_zonesByType.put(z.getType(), new ArrayList<Zone>());
			_zonesByType.get(z.getType()).add(z);
			if(_zones.get(z.getName()) == null)
				_zones.put(z.getName(), z);
			++count;
		}
		return count;
	}

	public void reload()
	{
		parse();
	}

	private class NoLandingZoneListener extends ZoneEnterLeaveListener
	{
		@Override
		public void objectEntered(final Zone zone, final GameObject object)
		{
			final Player player = object.getPlayer();
			if(player != null && player.isFlying() && !player.isBlocked() && player.getMountNpcId() == 12621)
			{
				final SiegeEvent<?, ?> siege = player.getEvent(SiegeEvent.class);
				if(siege != null && siege.checkIfInZone(player))
				{
					final Residence unit = siege.getResidence();
					if(unit != null && player.isClanLeader() && (player.getClan().getHasCastle() == unit.getId() || player.getClan().getHasHideout() == unit.getId()))
						return;
				}
				player.stopMove();
				player.sendPacket(Msg.THIS_AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_ATOP_OF_A_WYVERN_YOU_WILL_BE_DISMOUNTED_FROM_YOUR_WYVERN_IF_YOU_DO_NOT_LEAVE);
				Integer enterCount = (Integer) player.getProperty("Zone.EnteredNoLandingOnWywern");
				if(enterCount == null)
					enterCount = 0;
				final Location loc = player.getLastServerPosition();
				if(loc == null || enterCount >= 5)
				{
					player.setMount(0, 0, 0);
					player.addProperty("Zone.EnteredNoLandingOnWywern", 0);
					return;
				}
				player.teleToLocation(loc);
				player.addProperty("Zone.EnteredNoLandingOnWywern", enterCount + 1);
			}
		}

		@Override
		public void objectLeaved(final Zone zone, final GameObject object)
		{}
	}
}
