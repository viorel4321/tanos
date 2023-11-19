package l2s.gameserver.instancemanager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import l2s.commons.geometry.Polygon;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.Territory;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.DimensionalRift;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.TeleportToLocation;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.tables.TerritoryTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class DimensionalRiftManager
{
	private static Logger _log;
	private static DimensionalRiftManager _instance;
	private Map<Integer, Map<Integer, DimensionalRiftRoom>> _rooms;
	private static final int DIMENSIONAL_FRAGMENT_ITEM_ID = 7079;
	public static List<Integer> insts;

	public static DimensionalRiftManager getInstance()
	{
		if(DimensionalRiftManager._instance == null)
			DimensionalRiftManager._instance = new DimensionalRiftManager();
		return DimensionalRiftManager._instance;
	}

	public DimensionalRiftManager()
	{
		_rooms = new HashMap<Integer, Map<Integer, DimensionalRiftRoom>>();
		load();
	}

	public DimensionalRiftRoom getRoom(final int type, final int room)
	{
		return _rooms.get(type).get(room);
	}

	public Map<Integer, DimensionalRiftRoom> getRooms(final int type)
	{
		return _rooms.get(type);
	}

	public void load()
	{
		int countGood = 0;
		int countBad = 0;
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			final File file = new File(Config.DATAPACK_ROOT, "data/dimensionalRift.xml");
			if(!file.exists())
				throw new IOException();
			final Document doc = factory.newDocumentBuilder().parse(file);
			Location tele = new Location();
			int xMin = 0;
			int xMax = 0;
			int yMin = 0;
			int yMax = 0;
			int zMin = 0;
			int zMax = 0;
			for(Node rift = doc.getFirstChild(); rift != null; rift = rift.getNextSibling())
				if("rift".equalsIgnoreCase(rift.getNodeName()))
					for(Node area = rift.getFirstChild(); area != null; area = area.getNextSibling())
						if("area".equalsIgnoreCase(area.getNodeName()))
						{
							NamedNodeMap attrs = area.getAttributes();
							final int type = Integer.parseInt(attrs.getNamedItem("type").getNodeValue());
							for(Node room = area.getFirstChild(); room != null; room = room.getNextSibling())
								if("room".equalsIgnoreCase(room.getNodeName()))
								{
									attrs = room.getAttributes();
									final int roomId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
									final Node boss = attrs.getNamedItem("isBossRoom");
									final boolean isBossRoom = boss != null && Boolean.parseBoolean(boss.getNodeValue());
									for(Node coord = room.getFirstChild(); coord != null; coord = coord.getNextSibling())
										if("teleport".equalsIgnoreCase(coord.getNodeName()))
										{
											attrs = coord.getAttributes();
											tele = new Location(attrs.getNamedItem("loc").getNodeValue());
										}
										else if("zone".equalsIgnoreCase(coord.getNodeName()))
										{
											attrs = coord.getAttributes();
											xMin = Integer.parseInt(attrs.getNamedItem("xMin").getNodeValue());
											xMax = Integer.parseInt(attrs.getNamedItem("xMax").getNodeValue());
											yMin = Integer.parseInt(attrs.getNamedItem("yMin").getNodeValue());
											yMax = Integer.parseInt(attrs.getNamedItem("yMax").getNodeValue());
											zMin = Integer.parseInt(attrs.getNamedItem("zMin").getNodeValue());
											zMax = Integer.parseInt(attrs.getNamedItem("zMax").getNodeValue());
										}
									final int loc_id = TerritoryTable.locId++;
									final Territory territory = new Territory(loc_id);
									final Polygon shape = new Polygon();
									shape.add(xMin, yMin).add(xMax, yMin).add(xMax, yMax).add(xMin, yMax).setZmin(zMin).setZmax(zMax);
									if(!shape.validate())
										DimensionalRiftManager._log.warn("Invalid territory in rift with coords: xMin=" + xMin + " xMax=" + xMax + " yMin=" + yMin + " yMax=" + yMax + " zMin=" + zMin + " zMax=" + zMax);
									territory.add(shape);
									TerritoryTable.getInstance().getLocations().put(loc_id, territory);
									World.addTerritory(territory);
									if(!_rooms.containsKey(type))
										_rooms.put(type, new HashMap<Integer, DimensionalRiftRoom>());
								_rooms.get(type).put(roomId, new DimensionalRiftRoom(territory, tele, isBossRoom));
									for(Node spawn = room.getFirstChild(); spawn != null; spawn = spawn.getNextSibling())
										if("spawn".equalsIgnoreCase(spawn.getNodeName()))
										{
											attrs = spawn.getAttributes();
											final int mobId = Integer.parseInt(attrs.getNamedItem("mobId").getNodeValue());
											final int delay = Integer.parseInt(attrs.getNamedItem("delay").getNodeValue());
											final int count = Integer.parseInt(attrs.getNamedItem("count").getNodeValue());
											final NpcTemplate template = NpcTable.getTemplate(mobId);
											if(template == null)
												DimensionalRiftManager._log.warn("Template " + mobId + " not found!");
											if(!_rooms.containsKey(type))
												DimensionalRiftManager._log.warn("Type " + type + " not found!");
											else if(!_rooms.get(type).containsKey(roomId))
												DimensionalRiftManager._log.warn("Room " + roomId + " in Type " + type + " not found!");
											if(template != null && _rooms.containsKey(type) && _rooms.get(type).containsKey(roomId))
											{
												final Spawn spawnDat = new Spawn(template);
												spawnDat.setLocation(loc_id);
												spawnDat.setHeading(-1);
												spawnDat.setRespawnDelay(delay);
												spawnDat.setAmount(count);
												_rooms.get(type).get(roomId).getSpawns().add(spawnDat);
												++countGood;
											}
											else
												++countBad;
										}
								}
						}
		}
		catch(Exception e)
		{
			DimensionalRiftManager._log.warn("Error on loading dimensional rift spawns:");
			e.printStackTrace();
		}
		final int typeSize = _rooms.keySet().size();
		int roomSize = 0;
		for(final int b : _rooms.keySet())
			roomSize += _rooms.get(b).keySet().size();
		DimensionalRiftManager._log.info("DimensionalRiftManager: Loaded " + typeSize + " room types with " + roomSize + " rooms.");
		if(countBad > 0)
			DimensionalRiftManager._log.info("DimensionalRiftManager: Loaded " + countGood + " dimensional rift spawns, " + countBad + " errors.");
		else
			DimensionalRiftManager._log.info("DimensionalRiftManager: Loaded " + countGood + " dimensional rift spawns.");
	}

	public void reload()
	{
		for(final int b : _rooms.keySet())
			_rooms.get(b).clear();
		_rooms.clear();
		load();
	}

	public boolean checkIfInRiftZone(final Location loc, final boolean ignorePeaceZone)
	{
		if(ignorePeaceZone)
			return _rooms.get(0).get(1).checkIfInZone(loc);
		return _rooms.get(0).get(1).checkIfInZone(loc) && !_rooms.get(0).get(0).checkIfInZone(loc);
	}

	public boolean checkIfInPeaceZone(final Location loc)
	{
		return _rooms.get(0).get(0).checkIfInZone(loc);
	}

	public void teleportToWaitingRoom(final Player player)
	{
		teleToLocation(player, getRoom(0, 0).getTeleportCoords().rnd(0, 250, false), 0);
	}

	public void start(final Player player, final int type, final NpcInstance npc)
	{
		if(!player.isInParty())
		{
			showHtmlFile(player, "rift/NoParty.htm", npc);
			return;
		}
		if(!player.isGM())
		{
			if(!player.getParty().isLeader(player))
			{
				showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
				return;
			}
			if(player.getParty().isInDimensionalRift())
			{
				showHtmlFile(player, "rift/Cheater.htm", npc);
				if(!player.isGM())
					DimensionalRiftManager._log.warn("Player " + player.getName() + "(" + player.getObjectId() + ") was cheating in dimension rift area!");
				return;
			}
			if(player.getParty().getMemberCount() < Config.RIFT_MIN_PARTY_SIZE)
			{
				showHtmlFile(player, "rift/SmallParty.htm", npc);
				return;
			}
			for(final Player p : player.getParty().getPartyMembers())
				if(!checkIfInPeaceZone(p.getLoc()))
				{
					showHtmlFile(player, "rift/NotInWaitingRoom.htm", npc);
					return;
				}
			for(final Player p2 : player.getParty().getPartyMembers())
			{
				final ItemInstance i = p2.getInventory().getItemByItemId(7079);
				if(i == null || i.getCount() < getNeededItems(type))
				{
					showHtmlFile(player, "rift/NoFragments.htm", npc);
					return;
				}
			}
			for(final Player p2 : player.getParty().getPartyMembers())
				p2.getInventory().destroyItemByItemId(7079, getNeededItems(type), true);
		}
		new DimensionalRift(player.getParty(), type, Rnd.get(1, _rooms.get(type).size() - 1));
	}

	private int getNeededItems(final int type)
	{
		switch(type)
		{
			case 1:
			{
				return Config.RIFT_ENTER_COST_RECRUIT;
			}
			case 2:
			{
				return Config.RIFT_ENTER_COST_SOLDIER;
			}
			case 3:
			{
				return Config.RIFT_ENTER_COST_OFFICER;
			}
			case 4:
			{
				return Config.RIFT_ENTER_COST_CAPTAIN;
			}
			case 5:
			{
				return Config.RIFT_ENTER_COST_COMMANDER;
			}
			case 6:
			{
				return Config.RIFT_ENTER_COST_HERO;
			}
			default:
			{
				return 999999999;
			}
		}
	}

	public void showHtmlFile(final Player player, final String file, final NpcInstance npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
		html.setFile(file);
		html.replace("%t_name%", npc.getName());
		player.sendPacket(html);
	}

	public static void teleToLocation(final Player player, final Location loc, final int id)
	{
		if(player.isTeleporting() || player.isLogoutStarted())
			return;
		if(player.recording)
			player.writeBot(false);
		player.setIsTeleporting(true);
		player.setTarget(null);
		player.stopMove();
		if(player.isInVehicle())
			player.setVehicle(null);
		player.breakFakeDeath();
		player.decayMe();
		player.setXYZInvisible(loc);
		player.setReflectionId(id);
		player.setLastClientPosition(null);
		player.setLastServerPosition(null);
		player.sendPacket(new TeleportToLocation(player, loc.x, loc.y, loc.z));
		if(player.getServitor() != null)
			player.getServitor().teleportToOwner();
	}

	static
	{
		DimensionalRiftManager._log = LoggerFactory.getLogger(DimensionalRiftManager.class);
		DimensionalRiftManager.insts = new CopyOnWriteArrayList<Integer>();
	}

	public class DimensionalRiftRoom
	{
		private final Territory _territory;
		private final Location _teleportCoords;
		private final boolean _isBossRoom;
		private final List<Spawn> _roomSpawns;

		public DimensionalRiftRoom(final Territory territory, final Location tele, final boolean isBossRoom)
		{
			_territory = territory;
			_teleportCoords = tele;
			_isBossRoom = isBossRoom;
			_roomSpawns = new ArrayList<Spawn>();
		}

		public Location getTeleportCoords()
		{
			return _teleportCoords;
		}

		public boolean checkIfInZone(final Location loc)
		{
			return this.checkIfInZone(loc.x, loc.y, loc.z);
		}

		public boolean checkIfInZone(final int x, final int y, final int z)
		{
			return _territory.isInside(x, y, z);
		}

		public boolean isBossRoom()
		{
			return _isBossRoom;
		}

		public List<Spawn> getSpawns()
		{
			return _roomSpawns;
		}
	}
}
