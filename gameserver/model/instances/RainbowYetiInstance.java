package l2s.gameserver.model.instances;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import l2s.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.ExShowScreenMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class RainbowYetiInstance extends NpcInstance
{
	private static final int ItemA = 8035;
	private static final int ItemB = 8036;
	private static final int ItemC = 8037;
	private static final int ItemD = 8038;
	private static final int ItemE = 8039;
	private static final int ItemF = 8040;
	private static final int ItemG = 8041;
	private static final int ItemH = 8042;
	private static final int ItemI = 8043;
	private static final int ItemK = 8045;
	private static final int ItemL = 8046;
	private static final int ItemN = 8047;
	private static final int ItemO = 8048;
	private static final int ItemP = 8049;
	private static final int ItemR = 8050;
	private static final int ItemS = 8051;
	private static final int ItemT = 8052;
	private static final int ItemU = 8053;
	private static final int ItemW = 8054;
	private static final int ItemY = 8055;
	private static final Word[] WORLD_LIST;
	private List<GameObject> _mobs;
	private int _generated;
	private Future<?> _task;

	public RainbowYetiInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_mobs = new ArrayList<GameObject>();
		_generated = -1;
		_task = null;
	}

	@Override
	public void onSpawn()
	{
		super.onSpawn();
		final ClanHallMiniGameEvent event = this.getEvent(ClanHallMiniGameEvent.class);
		if(event == null)
			return;
		final List<Player> around = World.getAroundPlayers(this, 750, 100);
		for(final Player player : around)
		{
			final CMGSiegeClanObject siegeClanObject = event.getSiegeClan("attackers", player.getClan());
			if(siegeClanObject == null || !siegeClanObject.getPlayers().contains(player.getObjectId()))
				player.teleToLocation(event.getResidence().getOtherRestartPoint());
		}
		_task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GenerateTask(), 10000L, 300000L);
	}

	@Override
	public void deleteMe()
	{
		if(_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
		for(final GameObject object : _mobs)
			object.deleteMe();
		_mobs.clear();
		super.deleteMe();
	}

	public void teleportFromArena()
	{
		final ClanHallMiniGameEvent event = this.getEvent(ClanHallMiniGameEvent.class);
		if(event == null)
			return;
		final List<Player> around = World.getAroundPlayers(this, 750, 100);
		for(final Player player : around)
			player.teleToLocation(event.getResidence().getOtherRestartPoint());
	}

	@Override
	public void onBypassFeedback(final Player player, final String command)
	{
		if(!NpcInstance.canBypassCheck(player, this))
			return;
		if(command.equalsIgnoreCase("get"))
		{
			final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			boolean has = true;
			if(_generated == -1)
				has = false;
			else
			{
				final Word word = RainbowYetiInstance.WORLD_LIST[_generated];
				for(final int[] itemInfo : word.getItems())
					if(player.getInventory().getCountOf(itemInfo[0]) < itemInfo[1])
						has = false;
				if(has)
				{
					for(final int[] itemInfo : word.getItems())
						if(!player.consumeItem(itemInfo[0], itemInfo[1]))
							return;
					final int rnd = Rnd.get(100);
					if(_generated >= 0 && _generated <= 5)
					{
						if(rnd < 70)
							addItem(player, 8030);
						else if(rnd < 80)
							addItem(player, 8031);
						else if(rnd < 90)
							addItem(player, 8032);
						else
							addItem(player, 8033);
					}
					else if(rnd < 10)
						addItem(player, 8030);
					else if(rnd < 40)
						addItem(player, 8031);
					else if(rnd < 70)
						addItem(player, 8032);
					else
						addItem(player, 8033);
				}
			}
			if(!has)
				msg.setFile("residence2/clanhall/watering_manager002.htm");
			else
				msg.setFile("residence2/clanhall/watering_manager004.htm");
			player.sendPacket(msg);
		}
		else if(command.equalsIgnoreCase("see"))
		{
			final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
			msg.setFile("residence2/clanhall/watering_manager005.htm");
			if(_generated == -1)
				msg.replace("%word%", "Undecided");
			else
				msg.replace("%word%", RainbowYetiInstance.WORLD_LIST[_generated].getName());
			player.sendPacket(msg);
		}
		else
			super.onBypassFeedback(player, command);
	}

	private void addItem(final Player player, final int itemId)
	{
		final ClanHallMiniGameEvent event = this.getEvent(ClanHallMiniGameEvent.class);
		if(event == null)
			return;
		final ItemInstance item = ItemTable.getInstance().createItem(itemId);
		item.addEvent(event);
		player.getInventory().addItem(item);
		player.sendPacket(SystemMessage.obtainItems(item));
	}

	@Override
	public void showChatWindow(final Player player, final int val, final Object... arg)
	{
		this.showChatWindow(player, "residence2/clanhall/watering_manager001.htm", new Object[0]);
	}

	public void addMob(final GameObject object)
	{
		_mobs.add(object);
	}

	static
	{
		(WORLD_LIST = new Word[8])[0] = new Word("BABYDUCK", new int[][] {
				{ 8036, 2 },
				{ 8035, 1 },
				{ 8055, 1 },
				{ 8038, 1 },
				{ 8053, 1 },
				{ 8037, 1 },
				{ 8045, 1 } });
		RainbowYetiInstance.WORLD_LIST[1] = new Word("ALBATROS", new int[][] {
				{ 8035, 2 },
				{ 8046, 1 },
				{ 8036, 1 },
				{ 8052, 1 },
				{ 8050, 1 },
				{ 8048, 1 },
				{ 8051, 1 } });
		RainbowYetiInstance.WORLD_LIST[2] = new Word("PELICAN", new int[][] {
				{ 8049, 1 },
				{ 8039, 1 },
				{ 8046, 1 },
				{ 8043, 1 },
				{ 8037, 1 },
				{ 8035, 1 },
				{ 8047, 1 } });
		RainbowYetiInstance.WORLD_LIST[3] = new Word("KINGFISHER", new int[][] {
				{ 8045, 1 },
				{ 8043, 1 },
				{ 8047, 1 },
				{ 8041, 1 },
				{ 8040, 1 },
				{ 8043, 1 },
				{ 8051, 1 },
				{ 8042, 1 },
				{ 8039, 1 },
				{ 8050, 1 } });
		RainbowYetiInstance.WORLD_LIST[4] = new Word("CYGNUS", new int[][] {
				{ 8037, 1 },
				{ 8055, 1 },
				{ 8041, 1 },
				{ 8047, 1 },
				{ 8053, 1 },
				{ 8051, 1 } });
		RainbowYetiInstance.WORLD_LIST[5] = new Word("TRITON", new int[][] { { 8052, 2 }, { 8050, 1 }, { 8043, 1 }, { 8047, 1 } });
		RainbowYetiInstance.WORLD_LIST[6] = new Word("RAINBOW", new int[][] {
				{ 8050, 1 },
				{ 8035, 1 },
				{ 8043, 1 },
				{ 8047, 1 },
				{ 8036, 1 },
				{ 8048, 1 },
				{ 8054, 1 } });
		RainbowYetiInstance.WORLD_LIST[7] = new Word("SPRING", new int[][] {
				{ 8051, 1 },
				{ 8049, 1 },
				{ 8050, 1 },
				{ 8043, 1 },
				{ 8047, 1 },
				{ 8041, 1 } });
	}

	private static class Word
	{
		private final String _name;
		private final int[][] _items;

		public Word(final String name, final int[]... items)
		{
			_name = name;
			_items = items;
		}

		public String getName()
		{
			return _name;
		}

		public int[][] getItems()
		{
			return _items;
		}
	}

	private class GenerateTask implements Runnable
	{
		@Override
		public void run()
		{
			_generated = Rnd.get(RainbowYetiInstance.WORLD_LIST.length);
			final Word word = RainbowYetiInstance.WORLD_LIST[_generated];
			final List<Player> around = World.getAroundPlayers(RainbowYetiInstance.this, 750, 100);
			final ExShowScreenMessage msg = new ExShowScreenMessage(word.getName(), 5000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER);
			for(final Player player : around)
				player.sendPacket(msg);
		}
	}
}
