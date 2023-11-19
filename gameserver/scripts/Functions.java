package l2s.gameserver.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.World;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.ExShowTrace;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.NpcSay;
import l2s.gameserver.network.l2.s2c.Revive;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.MapRegionTable;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.HtmlUtils;

public class Functions
{
	public HardReference<Player> self;
	public HardReference<NpcInstance> npc;

	public Functions()
	{
		self = HardReferences.emptyRef();
		npc = HardReferences.emptyRef();
	}

	public static ScheduledFuture<?> executeTask(final GameObject caller, final String className, final String methodName, final Object[] args, final Map<String, Object> variables, final long delay)
	{
		return ThreadPoolManager.getInstance().schedule(() -> {
			Functions.callScripts(caller, className, methodName, args, variables);
		}, delay);
	}

	public static ScheduledFuture<?> executeTask(final String className, final String methodName, final Object[] args, final Map<String, Object> variables, final long delay)
	{
		return executeTask(null, className, methodName, args, variables, delay);
	}

	public static ScheduledFuture<?> executeTask(final GameObject object, final String className, final String methodName, final Object[] args, final long delay)
	{
		return executeTask(object, className, methodName, args, null, delay);
	}

	public static ScheduledFuture<?> executeTask(final String className, final String methodName, final Object[] args, final long delay)
	{
		return executeTask(className, methodName, args, null, delay);
	}

	public static Object callScripts(final String className, final String methodName, final Object[] args)
	{
		return callScripts(className, methodName, args, null);
	}

	public static Object callScripts(final String className, final String methodName, final Object[] args, final Map<String, Object> variables)
	{
		return callScripts(null, className, methodName, args, variables);
	}

	public static Object callScripts(final GameObject object, final String className, final String methodName, final Object[] args, final Map<String, Object> variables)
	{
		return Scripts.getInstance().callScripts(object, className, methodName, args, variables);
	}

	public static void show(final String text, final Player self)
	{
		if(text == null || self == null)
			return;
		final NpcHtmlMessage msg = new NpcHtmlMessage(self.getLastNpc() != null ? self.getLastNpc().getObjectId() : 5);
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(text);
		self.sendPacket(msg);
	}
		
	/**
	 * Статический метод, для вызова из любых мест
	 */
	public static void show(String text, Player self, NpcInstance npc, Object... arg)
	{
		if(text == null || self == null)
			return;

		NpcHtmlMessage msg = new NpcHtmlMessage(self, npc);

		// приводим нашу html-ку в нужный вид
		if(text.endsWith(".html") || text.endsWith(".htm"))
			msg.setFile(text);
		else
			msg.setHtml(HtmlUtils.bbParse(text));

		if(arg != null && arg.length % 2 == 0)
		{
			for(int i = 0; i < arg.length; i = +2)
			{
				msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
			}
		}

		self.sendPacket(msg);
	}

	public static void show(final CustomMessage message, final Player self)
	{
		show(message.toString(self), self, null);
	}

	public static void sendMessage(final String text, final Player self)
	{
		self.sendMessage(text);
	}

	public static void sendMessage(final CustomMessage message, final Player self)
	{
		self.sendMessage(message);
	}

	public static void npcSayInRange(final NpcInstance npc, final String text, final int range)
	{
		if(npc == null)
			return;
		final NpcSay cs = new NpcSay(npc, 0, text);
		for(final Player player : World.getAroundPlayers(npc, range, 200))
			if(player != null && !player.isBlockAll())
				player.sendPacket(cs);
	}

	public static void npcSay(final NpcInstance npc, final String text)
	{
		npcSayInRange(npc, text, 1500);
	}

	public static void npcSayInRangeCustomMessage(final NpcInstance npc, final int range, final String address, final Object... replacements)
	{
		if(npc == null)
			return;

		CustomMessage cm = new CustomMessage(address);
		for(Object replacement : replacements)
		{
			if(replacement instanceof CustomMessage)
				cm.addCustomMessage((CustomMessage) replacement);
			else
				cm.addString(String.valueOf(replacement));
		}

		for(final Player player : World.getAroundPlayers(npc, range, 200))
		{
			if(player != null && !player.isBlockAll())
				player.sendPacket(new NpcSay(npc, 0, cm.toString(player)));
		}
	}

	public static void npcSayCustomMessage(final NpcInstance npc, final String address, final Object... replacements)
	{
		npcSayInRangeCustomMessage(npc, 1500, address, replacements);
	}

	public static void npcSayToPlayer(final NpcInstance npc, final Player player, final String text)
	{
		if(npc == null || player.isBlockAll())
			return;
		player.sendPacket(new NpcSay(npc, 2, text));
	}

	public static void npcShout(final NpcInstance npc, final String text, final int range)
	{
		if(npc == null)
			return;
		final NpcSay cs = new NpcSay(npc, 1, text);
		if(Config.SHOUT_CHAT_MODE == 1 || range > 0)
		{
			for(final Player player : World.getAroundPlayers(npc, range > 0 ? range : Config.CHAT_RANGE_FIRST_MODE, 1500))
				if(player != null && !player.isBlockAll())
					player.sendPacket(cs);
		}
		else
		{
			final int mapregion = MapRegionTable.getInstance().getMapRegion(npc.getX(), npc.getY());
			for(final Player player2 : GameObjectsStorage.getPlayers())
				if(player2 != null && MapRegionTable.getInstance().getMapRegion(player2.getX(), player2.getY()) == mapregion && !player2.isBlockAll())
					player2.sendPacket(cs);
		}
	}

	public static void npcShoutCustomMessage(final NpcInstance npc, final String address, final int range, final Object... replacements)
	{
		if(npc == null)
			return;

		CustomMessage cm = new CustomMessage(address);
		for(Object replacement : replacements)
		{
			if(replacement instanceof CustomMessage)
				cm.addCustomMessage((CustomMessage) replacement);
			else
				cm.addString(String.valueOf(replacement));
		}

		if(Config.SHOUT_CHAT_MODE == 1 || range > 0)
		{
			for(final Player player : World.getAroundPlayers(npc, range > 0 ? range : Config.CHAT_RANGE_FIRST_MODE, 1500))
			{
				if(player != null && !player.isBlockAll())
					player.sendPacket(new NpcSay(npc, 1, cm.toString(player)));
			}
		}
		else
		{
			final int mapregion = MapRegionTable.getInstance().getMapRegion(npc.getX(), npc.getY());
			for(final Player player : GameObjectsStorage.getPlayers())
			{
				if(player != null && MapRegionTable.getInstance().getMapRegion(player.getX(), player.getY()) == mapregion && !player.isBlockAll())
					player.sendPacket(new NpcSay(npc, 1, cm.toString(player)));
			}
		}
	}

	public static void addItem(final Playable playable, final int itemId, final long count)
	{
		addItem(playable, itemId, count, true);
	}

	public static void addItem(final Playable playable, final int item_id, final long count, final boolean mess)
	{
		if(playable == null || count < 1L)
			return;
		Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;
		final ItemInstance item = ItemTable.getInstance().createItem(item_id);
		if(item.isStackable())
		{
			item.setCount(count);
			player.getInventory().addItem(item);
		}
		else
		{
			player.getInventory().addItem(item);
			for(int i = 1; i < count; ++i)
				player.getInventory().addItem(ItemTable.getInstance().createItem(item_id));
		}
		if(mess)
			player.sendPacket(SystemMessage.obtainItems(item_id, count, 0));
	}

	public static long getItemCount(Playable playable, int item_id)
	{
		long count = 0L;
		Playable player;
		if(playable != null && playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;
		final Inventory inv = player.getInventory();
		if(inv == null)
			return 0L;
		final ItemInstance[] items2;
		final ItemInstance[] items = items2 = inv.getItems();
		for(final ItemInstance item : items2)
			if(item.getItemId() == item_id)
				count += item.getCount();
		return count;
	}

	/**
	 * @param playable Владелец инвентаря
	 * @param itemId   ID предмета
	 * @param count	количество
	 * @return true,  если у персонажа есть необходимое количество предметов
	 */
	public static boolean haveItem(Playable playable, int itemId, long count)
	{
		return getItemCount(playable, itemId) >= count;
	}

	public static boolean deleteItem(Playable playable, int item_id, long count)
	{
		return removeItem(playable, item_id, count) >= count;
	}

	public static long removeItem(Playable playable, int item_id, long count)
	{
		if(playable == null || count < 1L)
			return 0L;
		Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;
		final Inventory inv = player.getInventory();
		if(inv == null)
			return 0L;
		long removed = count;
		final ItemInstance[] items = inv.getItems();
		for(final ItemInstance item : items)
		{
			if(item.getItemId() == item_id && count > 0L)
			{
				final long item_count = item.getCount();
				final long rem = count <= item_count ? count : item_count;
				player.getInventory().destroyItemByItemId(item_id, rem, true);
				count -= rem;
			}
		}
		removed -= count;
		if(removed > 0L)
			player.sendPacket(SystemMessage.removeItems(item_id, removed));
		return removed;
	}

	public static boolean deleteItemByObjId(Playable playable, int item_obj_id, long count)
	{
		return removeItemByObjId(playable, item_obj_id, count) >= count;
	}

	public static long removeItemByObjId(Playable playable, int item_obj_id, long count)
	{
		if(playable == null || count < 1)
			return 0L;
		Playable player;
		if(playable.isSummon())
			player = playable.getPlayer();
		else
			player = playable;
		final Inventory inv = player.getInventory();
		if(inv == null)
			return 0L;
		final ItemInstance[] items = inv.getItems();
		for(final ItemInstance item : items)
		{
			if(item.getObjectId() == item_obj_id && count > 0)
			{
				final long item_count = item.getCount();
				final int item_id = item.getItemId();
				final long removed = count <= item_count ? count : item_count;
				player.getInventory().destroyItem(item, removed, true);
				if(removed > 0L)
					player.sendPacket(SystemMessage.removeItems(item_id, removed));
				return removed;
			}
		}
		return 0L;
	}

	public static boolean ride(final Player player, final int pet)
	{
		if(player.isMounted())
			player.setMount(0, 0, 0);
		if(player.getServitor() != null)
		{
			player.sendPacket(new SystemMessage(543));
			return false;
		}
		player.setMount(pet, 0, 0);
		return true;
	}

	public static void unRide(final Player player)
	{
		if(player.isMounted())
			player.setMount(0, 0, 0);
	}

	public static void unSummonPet(final Player player, final boolean onlyPets)
	{
		if(player.getTrainedBeast() != null)
			player.getTrainedBeast().doDespawn();
		final Servitor pet = player.getServitor();
		if(pet == null)
			return;
		if(pet.isPet() || !onlyPets)
			pet.unSummon();
	}

	public static NpcInstance spawn(final Location loc, final int npcId)
	{
		return spawn(loc, npcId, 0);
	}

	public static NpcInstance spawn(final Location loc, final int npcId, final int instanceId)
	{
		try
		{
			final NpcInstance npc = NpcTable.getTemplate(npcId).getNewInstance();
			npc.setSpawnedLoc(loc.correctGeoZ(npc.getGeoIndex()));
			npc.setReflectionId(instanceId);
			npc.spawnMe(npc.getSpawnedLoc());
			return npc;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	public Player getSelf()
	{
		return self.get();
	}

	public NpcInstance getNpc()
	{
		return npc.get();
	}

	public static ExShowTrace Points2Trace(final List<int[]> points, final int step, final boolean auto_compleate, final boolean maxz)
	{
		final ExShowTrace result = new ExShowTrace();
		int[] prev = null;
		int[] first = null;
		for(final int[] p : points)
		{
			if(first == null)
				first = p;
			if(prev != null)
				result.addLine(prev[0], prev[1], maxz ? prev[3] : prev[2], p[0], p[1], maxz ? p[3] : p[2], step, 60000);
			prev = p;
		}
		if(prev == null || first == null)
			return result;
		if(auto_compleate)
			result.addLine(prev[0], prev[1], maxz ? prev[3] : prev[2], first[0], first[1], maxz ? first[3] : first[2], step, 60000);
		return result;
	}

	public static void SpawnNPCs(final int npcId, final int[][] locations, final List<Spawn> list)
	{
		final NpcTemplate template = NpcTable.getTemplate(npcId);
		if(template == null)
		{
			System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
			Thread.dumpStack();
			return;
		}
		for(final int[] location : locations)
			try
			{
				final Spawn sp = new Spawn(template);
				sp.setLoc(new Location(location));
				sp.setAmount(1);
				sp.setRespawnDelay(0);
				sp.init();
				if(list != null)
					list.add(sp);
			}
			catch(ClassNotFoundException e)
			{
				e.printStackTrace();
			}
	}

	public static void deSpawnNPCs(final List<Spawn> list)
	{
		for(final Spawn sp : list)
		{
			sp.stopRespawn();
			sp.getLastSpawn().deleteMe();
		}
		list.clear();
	}

	public static boolean IsActive(final String name)
	{
		return ServerVariables.getString(name, "off").equalsIgnoreCase("on");
	}

	public static boolean SetActive(final String name, final boolean active)
	{
		if(active == IsActive(name))
			return false;
		if(active)
			ServerVariables.set(name, "on");
		else
			ServerVariables.unset(name);
		return true;
	}

	public static boolean SimpleCheckDrop(final Creature mob, final Creature killer)
	{
		return mob != null && mob.isMonster() && !mob.isRaid() && killer != null && killer.getPlayer() != null && killer.getLevel() - mob.getLevel() < 9;
	}

	public static boolean isEventStarted(final String event)
	{
		return (boolean) callScripts(event, "isRunned", new Object[0]);
	}

	public static boolean isEventStarted(final String event, final String name)
	{
		return (boolean) callScripts(event, "isRunned", new Object[] { name });
	}

	public static void healPlayer(final Player player)
	{
		if(player == null)
			return;
		if(player.isDead())
		{
			player.restoreExp();
			player.setCurrentHp(player.getMaxHp(), true);
			player.setCurrentMp(player.getMaxMp());
			player.setCurrentCp(player.getMaxCp());
			player.broadcastPacket(new Revive(player));
		}
		else
		{
			player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
			player.setCurrentCp(player.getMaxCp());
		}
	}

	public static void stopEffects(final Player player, final int[] ids)
	{
		if(!player.getAbnormalList().isEmpty())
			for(final Abnormal e : player.getAbnormalList().values())
				if(ArrayUtils.contains(ids, e.getSkill().getId()))
					e.exit();
	}

	public static List<Player> getPlayers(final List<Integer> list)
	{
		final List<Player> result = new ArrayList<Player>();
		for(final Integer id : list)
		{
			final Player player = GameObjectsStorage.getPlayer(id);
			if(player != null)
				result.add(player);
		}
		return result;
	}

	public static List<Player> getPlayers(final List<Integer> list1, final List<Integer> list2)
	{
		final List<Player> result = new ArrayList<Player>();
		for(final Integer id : list1)
		{
			final Player player = GameObjectsStorage.getPlayer(id);
			if(player != null)
				result.add(player);
		}
		for(final Integer id : list2)
		{
			final Player player = GameObjectsStorage.getPlayer(id);
			if(player != null)
				result.add(player);
		}
		return result;
	}
}
