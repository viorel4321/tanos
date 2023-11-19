package l2s.gameserver;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;

import l2s.gameserver.instancemanager.*;
import l2s.gameserver.model.*;
import l2s.gameserver.utils.NpcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.lang.StatsUtils;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.net.HostInfo;
import l2s.commons.net.nio.impl.SelectorStats;
import l2s.commons.net.nio.impl.SelectorThread;
import l2s.commons.versioning.Version;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.config.xml.ConfigParsers;
import l2s.gameserver.config.xml.holder.HostsConfigHolder;
import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.data.xml.Parsers;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.UpdatesInstaller;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.handler.AdminCommandHandler;
import l2s.gameserver.handler.UserCommandHandler;
import l2s.gameserver.handler.VoicedCommandHandler;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.listener.GameListener;
import l2s.gameserver.listener.game.OnShutdownListener;
import l2s.gameserver.listener.game.OnStartListener;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.MonsterRace;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.model.instances.HitmanInstance;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.GamePacketHandler;
import l2s.gameserver.network.l2.PacketFloodProtector;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.tables.ArmorSetsTable;
import l2s.gameserver.tables.CharTemplateTable;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.DoorTable;
import l2s.gameserver.tables.HennaTable;
import l2s.gameserver.tables.HennaTreeTable;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.tables.MapRegionTable;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.tables.SpawnTable;
import l2s.gameserver.tables.Spellbook;
import l2s.gameserver.tables.StaticObjectsTable;
import l2s.gameserver.tables.TeleportLocTable;
import l2s.gameserver.tables.TeleportTable;
import l2s.gameserver.taskmanager.ItemsAutoDestroy;
import l2s.gameserver.taskmanager.TaskManager;
import l2s.gameserver.taskmanager.tasks.RestoreOfflineTraders;
import l2s.gameserver.utils.SpamFilter;
import l2s.gameserver.utils.Stat;
import l2s.gameserver.utils.Strings;
import l2s.gameserver.utils.velocity.VelocityUtils;

public class GameServer
{
	public static boolean DEVELOP = false;

	public static final String PROJECT_REVISION = "L2s [1924]";
	public static final String UPDATE_NAME = "Interlude";

	public static final int AUTH_SERVER_PROTOCOL = 5;

	private static final Logger _log = LoggerFactory.getLogger(GameServer.class);

	public class GameServerListenerList extends ListenerList<GameServer>
	{
		public void onStart()
		{
			for(final Listener<GameServer> listener : getListeners())
				if(OnStartListener.class.isInstance(listener))
					((OnStartListener) listener).onStart();
		}

		public void onShutdown()
		{
			for(final Listener<GameServer> listener : getListeners())
				if(OnShutdownListener.class.isInstance(listener))
					((OnShutdownListener) listener).onShutdown();
		}
	}

	private final GameServerListenerList _listeners = new GameServerListenerList();
	private final List<SelectorThread<GameClient>> _selectorThreads = new ArrayList<SelectorThread<GameClient>>();
	private final SelectorStats _selectorStats = new SelectorStats();
	private Version version;
	private final ItemTable _itemTable;
	private static GameServer _instance;
	public static Events events;

	private long _serverStartTimeMillis;

	private final String _licenseHost;
	private final int _onlineLimit;

	public List<SelectorThread<GameClient>> getSelectorThreads()
	{
		return _selectorThreads;
	}

	public SelectorStats getSelectorStats()
	{
		return _selectorStats;
	}

	public long getServerStartTime()
	{
		return _serverStartTimeMillis;
	}

	public String getLicenseHost()
	{
		return _licenseHost;
	}

	public int getOnlineLimit()
	{
		return _onlineLimit;
	}

	public GameServer() throws Exception
	{
		_instance = this;
		_serverStartTimeMillis = System.currentTimeMillis();

		new File("./log/").mkdir();

		version = new Version(GameServer.class);

		_log.info("=================================================");
		_log.info("Project Revision: ........ " + PROJECT_REVISION);
		_log.info("Build Revision: .......... " + version.getRevisionNumber());
		_log.info("Update: .................. " + UPDATE_NAME);
		_log.info("Build date: .............. " + version.getBuildDate());
		_log.info("Compiler version: ........ " + version.getBuildJdk());
		_log.info("=================================================");

		// Initialize config
		ConfigParsers.parseAll();
		Config.load(false);
		VelocityUtils.init();

		final HostInfo[] hosts = HostsConfigHolder.getInstance().getGameServerHosts();
		if(hosts.length == 0)
		{
			throw new Exception("Server hosts list is empty!");
		}

		final TIntSet ports = new TIntHashSet();
		for(HostInfo host : hosts)
		{
			if(host.getAddress() != null)
			{
				while(!checkFreePort(host.getAddress(), host.getPort()))
				{
					_log.warn("Port '" + host.getPort() + "' on host '" + host.getAddress() + "' is allready binded. Please free it and restart server.");
					try
					{
						Thread.sleep(1000L);
					}
					catch(InterruptedException e2)
					{
						//
					}
				}
				ports.add(host.getPort());
			}
		}

		final int[] portsArray = ports.toArray();

		if(portsArray.length == 0)
		{
			throw new Exception("Server ports list is empty!");
		}

		String licenseHost = "";
		int onlineLimit = 0;

		_licenseHost = Config.EXTERNAL_HOSTNAME;
		_onlineLimit = Config.MAXIMUM_ONLINE_USERS;

		if(_onlineLimit == 0)
		{
			throw new Exception("Server online limit is zero!");
		}

		DatabaseFactory.getInstance();
		Strings.reload();

		UpdatesInstaller.checkAndInstall();

		final IdFactory _idFactory = IdFactory.getInstance();
		if(!_idFactory.isInitialized())
		{
			_log.error("Could not read object IDs from DB. Please Check Your Data.");
			throw new Exception("Could not initialize the ID factory");
		}
		ThreadPoolManager.getInstance();
		Scripts.getInstance();
		GeoEngine.load();
		GameTimeController.getInstance();
		SpamFilter.getInstance().load();
		CrestCache.getInstance();
		SkillTree.getInstance();
		SkillTable.getInstance();
		Spellbook.getInstance();
		_itemTable = ItemTable.getInstance();
		if(!_itemTable.isInitialized())
		{
			_log.error("Could not find the >Items files. Please Check Your Data.");
			throw new Exception("Could not initialize the item table");
		}
		final ArmorSetsTable _armorSetsTable = ArmorSetsTable.getInstance();
		if(!_armorSetsTable.isInitialized())
		{
			_log.error("Could not find the ArmorSets files. Please Check Your Data.");
			throw new Exception("Could not initialize the armorSets table");
		}
		GameServer.events = new Events();
		TradeController.getInstance();
		RecipeController.getInstance();
		if(Config.PACKET_FLOOD_PROTECTOR)
			PacketFloodProtector.getInstance();
		CharTemplateTable.getInstance();
		NpcTable.getInstance();
		if(!NpcTable.isInitialized())
		{
			_log.error("Could not find the extraced files. Please Check Your Data.");
			throw new Exception("Could not initialize the npc table");
		}
		Parsers.parseAll();
		final HennaTable _hennaTable = HennaTable.getInstance();
		if(!_hennaTable.isInitialized())
			throw new Exception("Could not initialize the Henna Table");
		HennaTreeTable.getInstance();
		if(!_hennaTable.isInitialized())
			throw new Exception("Could not initialize the Henna Tree Table");
		MapRegionTable.getInstance();
		DoorTable.getInstance().fillDoors();
		TownManager.getInstance();
		Scripts.getInstance().init();
		SpawnTable.getInstance();
		DimensionalRiftManager.getInstance();
		Announcements.getInstance();
		ClanTable.getInstance();
		PlayerMessageStack.getInstance();
		if(Config.AUTODESTROY_ITEM_AFTER > 0)
			ItemsAutoDestroy.getInstance();
		MonsterRace.getInstance();
		StaticObjectsTable.getInstance();
		final SevenSigns _sevenSignsEngine = SevenSigns.getInstance();
		SevenSignsFestival.getInstance();
		final AutoChatHandler _autoChatHandler = AutoChatHandler.getInstance();
		_log.info("AutoChatHandler: Loaded " + _autoChatHandler.size() + " handlers in total.");
		_sevenSignsEngine.spawnSevenSignsNPC();
		Olympiad.load();
		Hero.getInstance();
		PetitionManager.getInstance();
		CursedWeaponsManager.getInstance();
		if(!Config.ALLOW_WEDDING)
		{
			CoupleManager.getInstance();
			_log.info("CoupleManager initialized");
		}
		final AdminCommandHandler _adminCommandHandler = AdminCommandHandler.getInstance();
		_log.info("AdminCommandHandler: Loaded " + _adminCommandHandler.size() + " handlers.");
		final UserCommandHandler _userCommandHandler = UserCommandHandler.getInstance();
		_log.info("UserCommandHandler: Loaded " + _userCommandHandler.size() + " handlers.");
		final VoicedCommandHandler _voicedCommandHandler = VoicedCommandHandler.getInstance();
		_log.info("VoicedCommandHandler: Loaded " + _voicedCommandHandler.size() + " handlers.");
		TaskManager.getInstance();
		ResidenceHolder.getInstance().callInit();
		EventHolder.getInstance().callInit();
		Manor.getInstance();
		CastleManorManager.getInstance();
		BoatHolder.getInstance().spawnAll();
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		if(Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionShipManager.getInstance();
		_log.info("IdFactory: Free ObjectID's remaining: " + IdFactory.getInstance().size());
		TeleportTable.getInstance();
		TeleportLocTable.getInstance();
		L2TopManager.getInstance();
		MMOTopManager.getInstance();
		if(Config.HITMAN_ENABLE)
		{
			HitmanInstance.updateOrderPlayer();
			if(Config.HITMAN_ANNOUNCE_ENABLE)
				HitmanInstance.AnnounceStart();
		}
		Player.loadBots();
		int restartTime = 0;
		int restartAt = 0;
		if(Config.RESTART_AT_TIME > -1)
		{
			final Calendar calendarRestartAt = Calendar.getInstance();
			calendarRestartAt.set(11, Config.RESTART_AT_TIME);
			calendarRestartAt.set(12, Config.RESTART_AT_MINS);
			calendarRestartAt.set(13, 0);
			if(calendarRestartAt.getTimeInMillis() < System.currentTimeMillis())
				calendarRestartAt.add(11, 24);
			restartAt = (int) (calendarRestartAt.getTimeInMillis() - System.currentTimeMillis()) / 1000;
		}
		restartTime = Config.RESTART_TIME * 60 * 60;
		if(restartTime < restartAt && restartTime > 0 || restartTime > restartAt && restartAt == 0)
			Shutdown.getInstance().schedule(restartTime, Shutdown.RESTART);
		else if(restartAt > 0)
			Shutdown.getInstance().schedule(restartAt, Shutdown.RESTART);
		_log.info("GameServer Started");
		_log.info("Maximum Numbers of Connected Players: " + Config.MAXIMUM_ONLINE_USERS);
		Stat.init();

		GameBanManager.getInstance().init();

		registerSelectorThreads(ports);

		getListeners().onStart();

		AuthServerCommunication.getInstance().start();

		Toolkit.getDefaultToolkit().beep();

		if(Config.SERVICES_OFFLINE_TRADE_RESTORE_AFTER_RESTART)
			ThreadPoolManager.getInstance().schedule(new RestoreOfflineTraders(), 30000L);

		AutoAnnounces.tryStart();

		_log.info("Server Loaded in " + (System.currentTimeMillis() - _serverStartTimeMillis) / 1000L + " seconds");
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				if(!Config.DONTLOADSPAWN)
					SpawnTable.getInstance().loadSpawn();
				if(Config.SPAWN_FROM_CONFIG)
				{
					for(final int[] npcInfo : Config.SPAWN_NPC_FROM_CONFIG){
						NpcUtils.spawnSingle(npcInfo[0], npcInfo[1],npcInfo[2],npcInfo[3],npcInfo[4] , 0);
					}
				}
				_log.info("=================================================");
				final String memUsage = new StringBuilder().append(StatsUtils.getMemUsage()).toString();
				for(final String line : memUsage.split("\n"))
					_log.info(line);
				_log.info("=================================================");
			}
		}, 10L);
	}

	public GameServerListenerList getListeners()
	{
		return _listeners;
	}

	public static GameServer getInstance()
	{
		return GameServer._instance;
	}

	public <T extends GameListener> boolean addListener(final T listener)
	{
		return _listeners.add(listener);
	}

	public <T extends GameListener> boolean removeListener(final T listener)
	{
		return _listeners.remove(listener);
	}

	private static boolean checkFreePort(String hostname, int port)
	{
		ServerSocket ss = null;
		try
		{
			if(hostname.equalsIgnoreCase("*"))
				ss = new ServerSocket(port);
			else
				ss = new ServerSocket(port, 50, InetAddress.getByName(hostname));
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				ss.close();
			}
			catch(Exception e)
			{
				//
			}
		}
		return true;
	}

	private static boolean checkOpenPort(String ip, int port)
	{
		Socket socket = null;
		try
		{
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port), 100);
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch(Exception e)
			{
				//
			}
		}
		return true;
	}

	private void registerSelectorThreads(TIntSet ports)
	{
		final GamePacketHandler gph = new GamePacketHandler();

		for(int port : ports.toArray())
			registerSelectorThread(gph, null, port);
	}

	private void registerSelectorThread(GamePacketHandler gph, String ip, int port)
	{
		try
		{
			SelectorThread<GameClient> selectorThread = new SelectorThread<GameClient>(Config.SELECTOR_CONFIG, _selectorStats, gph, gph, gph, null);
			selectorThread.openServerSocket(ip == null ? null : InetAddress.getByName(ip), port);
			selectorThread.start();
			_selectorThreads.add(selectorThread);
		}
		catch(Exception e)
		{
			//
		}
	}

	public static void main(final String[] args) throws Exception
	{
		for(String arg : args)
		{
			if(arg.equalsIgnoreCase("-dev"))
				DEVELOP = true;
		}
		new GameServer();
	}

	public Version getVersion()
	{
		return version;
	}

	public final ItemTable getItemTable()
	{
		return _itemTable;
	}
}