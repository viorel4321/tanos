package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.GameServer;
//import l2s.gameserver.model.instances.FenceInstance;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;

import org.apache.commons.lang3.ArrayUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

/**
 * @author VISTALL
 */
public class GameObjectsStorage
{
	private static IntObjectMap<GameObject> _objects = new CHashIntObjectMap<GameObject>(60000 * Config.RATE_MOB_SPAWN + GameServer.getInstance().getOnlineLimit() + 1000);
	private static IntObjectMap<StaticObjectInstance> _staticObjects = new CHashIntObjectMap<StaticObjectInstance>(1000);
	private static IntObjectMap<NpcInstance> _npcs = new CHashIntObjectMap<NpcInstance>(60000 * Config.RATE_MOB_SPAWN);
	private static IntObjectMap<Player> _players = new CHashIntObjectMap<Player>(GameServer.getInstance().getOnlineLimit());
	private static IntObjectMap<ItemInstance> _items = new CHashIntObjectMap<ItemInstance>(Config.MAX_ITEMS);
	//private static IntObjectMap<FenceInstance> _fences = new CHashIntObjectMap<FenceInstance>(1000);

	public static GameObject findObject(int objId)
	{
		return _objects.get(objId);
	}

	public static Collection<GameObject> getObjects()
	{
		return _objects.valueCollection();
	}

	public static Collection<StaticObjectInstance> getStaticObjects()
	{
		return _staticObjects.valueCollection();
	}

	public static StaticObjectInstance getStaticObject(int id)
	{
		for(StaticObjectInstance object : _staticObjects.valueCollection())
		{
			if(object.getUId() == id)
				return object;
		}
		return null;
	}

	public static Collection<ItemInstance> getItems()
	{
		return _items.valueCollection();
	}

	public static ItemInstance getItem(int objectId)
	{
		for(ItemInstance item : _items.valueCollection())
		{
			if(item.getObjectId() == objectId)
				return item;
		}
		return null;
	}

	/*public static Collection<FenceInstance> getFences()
	{
		return _fences.valueCollection();
	}

	public static FenceInstance getFence(int objectId)
	{
		for(FenceInstance fence : _fences.valueCollection())
		{
			if(fence.getObjectId() == objectId)
				return fence;
		}
		return null;
	}*/

	public static Player getPlayer(String name)
	{
		for(Player player : _players.valueCollection())
		{
			if(player.getName().equalsIgnoreCase(name))
				return player;
		}
		return null;
	}

	public static Player getPlayer(int objId)
	{
		return _players.get(objId);
	}

	public static Collection<Player> getPlayers()
	{
		return _players.valueCollection();
	}

	public static NpcInstance getNpc(int objId)
	{
		return _npcs.get(objId);
	}

	public static Collection<NpcInstance> getNpcs()
	{
		return _npcs.valueCollection();
	}

	public static List<NpcInstance> getNpcs(boolean onlyAlive, int... npcIds)
	{
		return getNpcs(onlyAlive, onlyAlive, npcIds);
	}

	public static List<NpcInstance> getNpcs(boolean onlyAlive, boolean onlySpawned, int... npcIds)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance npc : getNpcs())
		{
			if((npcIds.length == 0 || ArrayUtils.contains(npcIds, npc.getNpcId())) && (!onlyAlive || !npc.isDead()) && (!onlySpawned || npc.isVisible()))
				result.add(npc);
		}
		return result;
	}

	public static List<NpcInstance> getNpcs(boolean onlyAlive, String npcName)
	{
		return getNpcs(onlyAlive, onlyAlive, npcName);
	}

	public static List<NpcInstance> getNpcs(boolean onlyAlive, boolean onlySpawned, String npcName)
	{
		List<NpcInstance> result = new ArrayList<NpcInstance>();
		for(NpcInstance npc : getNpcs())
		{
			if(npc.getName().equalsIgnoreCase(npcName) && (!onlyAlive || !npc.isDead()) && (!onlySpawned || npc.isVisible()))
				result.add(npc);
		}
		return result;
	}

	public static <T extends GameObject> void put(T o)
	{
		IntObjectMap<T> map = getMapForObject(o);
		if(map != null)
			map.put(o.getObjectId(), o);

		_objects.put(o.getObjectId(), o);
	}

	public static <T extends GameObject> void remove(T o)
	{
		IntObjectMap<T> map = getMapForObject(o);
		if(map != null)
			map.remove(o.getObjectId());

		_objects.remove(o.getObjectId());
	}

	@SuppressWarnings("unchecked")
	private static <T extends GameObject> IntObjectMap<T> getMapForObject(T o)
	{
		/*if(o.isFence())
			return (IntObjectMap<T>) _fences;*/

		if(o.isStaticObject())
			return (IntObjectMap<T>) _staticObjects;

		if(o.isNpc())
			return (IntObjectMap<T>) _npcs;

		if(o.isPlayer())
			return (IntObjectMap<T>) _players;

		return null;
	}
}