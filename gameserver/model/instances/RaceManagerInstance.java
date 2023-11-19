package l2s.gameserver.model.instances;

import java.util.ArrayList;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.ServerVariables;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.MonsterRace;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.DeleteObject;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MonRaceInfo;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class RaceManagerInstance extends NpcInstance
{
	public static final int LANES = 8;
	public static final int WINDOW_START = 0;
	private static ArrayList<Race> history;
	private static ArrayList<RaceManagerInstance> managers;
	private static int _raceNumber;
	private static final long SECOND = 1000L;
	private static final long MINUTE = 60000L;
	private static int minutes;
	private static final int ACCEPTING_BETS = 0;
	private static final int WAITING = 1;
	private static final int STARTING_RACE = 2;
	private static final int RACE_END = 3;
	private static int state;
	protected static final int[][] codes;
	private static boolean notInitialized;
	protected static MonRaceInfo packet;
	protected static int[] cost;

	public RaceManagerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		if(RaceManagerInstance.notInitialized)
		{
			RaceManagerInstance.notInitialized = false;
			RaceManagerInstance._raceNumber = ServerVariables.getInt("monster_race", 1);
			RaceManagerInstance.history = new ArrayList<Race>();
			RaceManagerInstance.managers = new ArrayList<RaceManagerInstance>();
			final ThreadPoolManager s = ThreadPoolManager.getInstance();
			s.scheduleAtFixedRate(new Announcement(816), 0L, 600000L);
			s.scheduleAtFixedRate(new Announcement(817), 30000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(816), 60000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(817), 90000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(818), 120000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(818), 180000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(818), 240000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(818), 300000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(819), 360000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(819), 420000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(820), 420000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(820), 480000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(821), 510000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(822), 530000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(823), 535000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(823), 536000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(823), 537000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(823), 538000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(823), 539000L, 600000L);
			s.scheduleAtFixedRate(new Announcement(824), 540000L, 600000L);
		}
		RaceManagerInstance.managers.add(this);
	}

	public void removeKnownPlayer(final Player player)
	{
		for(int i = 0; i < 8; ++i)
		{
			final DeleteObject obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
			player.sendPacket(obj);
		}
	}

	public void makeAnnouncement(final int type)
	{
		final SystemMessage sm = new SystemMessage(type);
		switch(type)
		{
			case 816:
			case 817:
			{
				if(RaceManagerInstance.state != 0)
				{
					RaceManagerInstance.state = 0;
					startRace();
				}
				sm.addNumber(Integer.valueOf(RaceManagerInstance._raceNumber));
				break;
			}
			case 818:
			case 820:
			case 823:
			{
				sm.addNumber(Integer.valueOf(RaceManagerInstance.minutes));
				sm.addNumber(Integer.valueOf(RaceManagerInstance._raceNumber));
				--RaceManagerInstance.minutes;
				break;
			}
			case 819:
			{
				sm.addNumber(Integer.valueOf(RaceManagerInstance._raceNumber));
				RaceManagerInstance.state = 1;
				RaceManagerInstance.minutes = 2;
				break;
			}
			case 822:
			case 825:
			{
				sm.addNumber(Integer.valueOf(RaceManagerInstance._raceNumber));
				RaceManagerInstance.minutes = 5;
				break;
			}
			case 826:
			{
				RaceManagerInstance.state = 3;
				sm.addNumber(Integer.valueOf(MonsterRace.getInstance().getFirstPlace()));
				sm.addNumber(Integer.valueOf(MonsterRace.getInstance().getSecondPlace()));
				break;
			}
		}
		broadcast(sm);
		if(type == 824)
		{
			RaceManagerInstance.state = 2;
			startRace();
			RaceManagerInstance.minutes = 5;
		}
	}

	protected void broadcast(final L2GameServerPacket pkt)
	{
		for(final RaceManagerInstance manager : RaceManagerInstance.managers)
			if(!manager.isDead())
				manager.broadcastPacketToOthers(pkt);
	}

	public void sendMonsterInfo()
	{
		broadcast(RaceManagerInstance.packet);
	}

	private void startRace()
	{
		final MonsterRace race = MonsterRace.getInstance();
		if(RaceManagerInstance.state == 2)
		{
			final PlaySound SRace = new PlaySound("S_Race");
			broadcast(SRace);
			final PlaySound SRace2 = new PlaySound(0, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559));
			broadcast(SRace2);
			RaceManagerInstance.packet = new MonRaceInfo(RaceManagerInstance.codes[1][0], RaceManagerInstance.codes[1][1], race.getMonsters(), race.getSpeeds());
			sendMonsterInfo();
			ThreadPoolManager.getInstance().schedule(new RunRace(), 5000L);
		}
		else
		{
			race.newRace();
			race.newSpeeds();
			RaceManagerInstance.packet = new MonRaceInfo(RaceManagerInstance.codes[0][0], RaceManagerInstance.codes[0][1], race.getMonsters(), race.getSpeeds());
			sendMonsterInfo();
		}
	}

	@Override
	public void onBypassFeedback(final Player player, String command)
	{
		if(Config.DEBUG)
			System.out.println("Command: " + command);
		if(command.startsWith("BuyTicket") && RaceManagerInstance.state != 0)
		{
			player.sendPacket(new SystemMessage(1046));
			command = "Chat 0";
		}
		if(command.startsWith("ShowOdds") && RaceManagerInstance.state == 0)
		{
			player.sendPacket(new SystemMessage(1044));
			command = "Chat 0";
		}
		if(command.startsWith("BuyTicket"))
		{
			int val = Integer.parseInt(command.substring(10));
			if(val == 0)
			{
				player.setRace(0, 0);
				player.setRace(1, 0);
			}
			if(val == 10 && player.getRace(0) == 0 || val == 20 && player.getRace(0) == 0 && player.getRace(1) == 0)
				val = 0;
			showBuyTicket(player, val);
		}
		else if(command.equals("ShowOdds"))
			showOdds(player);
		else if(command.equals("ShowInfo"))
			showMonsterInfo(player);
		else if(!command.equals("calculateWin"))
			if(!command.equals("viewHistory"))
				super.onBypassFeedback(player, command);
	}

	public void showOdds(final Player player)
	{
		if(RaceManagerInstance.state == 0)
			return;
		final int npcId = getTemplate().npcId;
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		final String filename = getHtmlPath(npcId, 5, player);
		html.setFile(filename);
		for(int i = 0; i < 8; ++i)
		{
			final int n = i + 1;
			final String search = "Mob" + n;
			html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
		}
		html.replace("1race", String.valueOf(RaceManagerInstance._raceNumber));
		player.sendPacket(html);
		player.sendActionFailed();
	}

	public void showMonsterInfo(final Player player)
	{
		final int npcId = getTemplate().npcId;
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		final String filename = getHtmlPath(npcId, 6, player);
		html.setFile(filename);
		for(int i = 0; i < 8; ++i)
		{
			final int n = i + 1;
			final String search = "Mob" + n;
			html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
		}
		player.sendPacket(html);
		player.sendActionFailed();
	}

	public void showBuyTicket(final Player player, final int val)
	{
		if(RaceManagerInstance.state != 0)
			return;
		final int npcId = getTemplate().npcId;
		final NpcHtmlMessage html = new NpcHtmlMessage(_objectId);
		if(val < 10)
		{
			final String filename = getHtmlPath(npcId, 2, player);
			html.setFile(filename);
			for(int i = 0; i < 8; ++i)
			{
				final int n = i + 1;
				final String search = "Mob" + n;
				html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
			}
			final String search = "No1";
			if(val == 0)
				html.replace(search, "");
			else
			{
				html.replace(search, "" + val);
				player.setRace(0, val);
			}
		}
		else if(val < 20)
		{
			if(player.getRace(0) == 0)
				return;
			final String filename = getHtmlPath(npcId, 3, player);
			html.setFile(filename);
			html.replace("0place", "" + player.getRace(0));
			String search = "Mob1";
			final String replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
			html.replace(search, replace);
			search = "0adena";
			if(val == 10)
				html.replace(search, "");
			else
			{
				html.replace(search, "" + RaceManagerInstance.cost[val - 11]);
				player.setRace(1, val - 10);
			}
		}
		else if(val == 20)
		{
			if(player.getRace(0) == 0 || player.getRace(1) == 0)
				return;
			final String filename = getHtmlPath(npcId, 4, player);
			html.setFile(filename);
			html.replace("0place", "" + player.getRace(0));
			String search = "Mob1";
			final String replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
			html.replace(search, replace);
			search = "0adena";
			final int price = RaceManagerInstance.cost[player.getRace(1) - 1];
			html.replace(search, "" + price);
			search = "0tax";
			final int tax = 0;
			html.replace(search, "" + tax);
			search = "0total";
			final int total = price + tax;
			html.replace(search, "" + total);
		}
		else
		{
			if(player.getRace(0) == 0 || player.getRace(1) == 0)
				return;
			if(player.getAdena() < RaceManagerInstance.cost[player.getRace(1) - 1])
			{
				player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				return;
			}
			final int ticket = player.getRace(0);
			final int priceId = player.getRace(1);
			player.setRace(0, 0);
			player.setRace(1, 0);
			player.reduceAdena(RaceManagerInstance.cost[priceId - 1], true);
			final SystemMessage sm = new SystemMessage(371);
			sm.addNumber(Integer.valueOf(RaceManagerInstance._raceNumber));
			sm.addItemName(Integer.valueOf(4443));
			player.sendPacket(sm);
			final ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), 4443);
			item.setEnchantLevel(RaceManagerInstance._raceNumber);
			item.setCustomType1(ticket);
			item.setCustomType2(RaceManagerInstance.cost[priceId - 1] / 100);
			player.getInventory().addItem(item);
			return;
		}
		html.replace("1race", String.valueOf(RaceManagerInstance._raceNumber));
		player.sendPacket(html);
		player.sendActionFailed();
	}

	public MonRaceInfo getPacket()
	{
		return RaceManagerInstance.packet;
	}

	static
	{
		RaceManagerInstance._raceNumber = 1;
		RaceManagerInstance.minutes = 5;
		RaceManagerInstance.state = 3;
		codes = new int[][] { { -1, 0 }, { 0, 15322 }, { 13765, -1 } };
		RaceManagerInstance.notInitialized = true;
		RaceManagerInstance.cost = new int[] { 100, 500, 1000, 5000, 10000, 20000, 50000, 100000 };
	}

	class Announcement implements Runnable
	{
		private int type;

		public Announcement(final int type)
		{
			this.type = type;
		}

		@Override
		public void run()
		{
			makeAnnouncement(type);
		}
	}

	public class Race
	{
		private Info[] info;

		public Race(final Info[] info)
		{
			this.info = info;
		}

		public Info getLaneInfo(final int lane)
		{
			return info[lane];
		}

		public class Info
		{
			private int id;
			private int place;
			private int odds;
			private int payout;

			public Info(final int id, final int place, final int odds, final int payout)
			{
				this.id = id;
				this.place = place;
				this.odds = odds;
				this.payout = payout;
			}

			public int getId()
			{
				return id;
			}

			public int getOdds()
			{
				return odds;
			}

			public int getPayout()
			{
				return payout;
			}

			public int getPlace()
			{
				return place;
			}
		}
	}

	class RunRace implements Runnable
	{
		@Override
		public void run()
		{
			RaceManagerInstance.packet = new MonRaceInfo(RaceManagerInstance.codes[2][0], RaceManagerInstance.codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
			sendMonsterInfo();
			ThreadPoolManager.getInstance().schedule(new RunEnd(), 30000L);
		}
	}

	class RunEnd implements Runnable
	{
		@Override
		public void run()
		{
			makeAnnouncement(826);
			makeAnnouncement(825);
			RaceManagerInstance._raceNumber++;
			ServerVariables.set("monster_race", RaceManagerInstance._raceNumber);
			for(int i = 0; i < 8; ++i)
				broadcast(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
		}
	}
}
