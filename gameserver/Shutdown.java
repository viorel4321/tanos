package l2s.gameserver;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.net.nio.impl.SelectorThread;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.CoupleManager;
import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.instancemanager.FishingChampionShipManager;
import l2s.gameserver.instancemanager.RaidBossSpawnManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.model.entity.olympiad.OlympiadDatabase;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.utils.SpamFilter;
import l2s.gameserver.utils.Util;

public class Shutdown extends Thread
{
	private static final Logger _log;
	public static final int SHUTDOWN = 0;
	public static final int RESTART = 2;
	public static final int NONE = -1;
	private static final Shutdown _instance;
	private Timer counter;
	private int shutdownMode;
	private int shutdownCounter;

	public static final Shutdown getInstance()
	{
		return Shutdown._instance;
	}

	private Shutdown()
	{
		setName(this.getClass().getSimpleName());
		setDaemon(true);
		shutdownMode = -1;
	}

	public int getSeconds()
	{
		return shutdownMode == -1 ? -1 : shutdownCounter;
	}

	public int getMode()
	{
		return shutdownMode;
	}

	public synchronized void schedule(final int seconds, final int shutdownMode)
	{
		if(seconds < 0)
			return;
		if(counter != null)
			counter.cancel();
		this.shutdownMode = shutdownMode;
		shutdownCounter = seconds;
		Shutdown._log.info("Scheduled server " + (shutdownMode == 0 ? "shutdown" : "restart") + " in " + Util.formatTime(seconds) + ".");
		(counter = new Timer("ShutdownCounter", true)).scheduleAtFixedRate(new ShutdownCounter(), 0L, 1000L);
	}

	public synchronized void cancel()
	{
		shutdownMode = -1;
		if(counter != null)
			counter.cancel();
		counter = null;
	}

	@Override
	public void run()
	{
		System.out.println("Shutting down LS/GS communication...");
		AuthServerCommunication.getInstance().shutdown();
		System.out.println("Shutting down scripts...");
		Scripts.getInstance().shutdown();
		System.out.println("Disconnecting players...");
		disconnectAllPlayers();
		System.out.println("Saving data...");
		saveData();
		try
		{
			System.out.println("Shutting down thread pool...");
			ThreadPoolManager.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Shutting down selector...");
		if(GameServer.getInstance() != null)
			for(final SelectorThread<GameClient> st : GameServer.getInstance().getSelectorThreads())
				try
				{
					st.shutdown();
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
				}
		try
		{
			System.out.println("Shutting down database communication...");
			DatabaseFactory.getInstance().shutdown();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("Shutdown finished.");
	}

	private void saveData()
	{
		if(Config.ALLOW_SEVEN_SIGNS)
		{
			try
			{
				if(!SevenSigns.getInstance().isSealValidationPeriod())
				{
					SevenSignsFestival.getInstance().saveFestivalData(false);
					System.out.println("SevenSignsFestival: Data saved.");
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			try
			{
				SevenSigns.getInstance().saveSevenSignsData(null, true);
				System.out.println("SevenSigns: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		if(Config.ENABLE_OLYMPIAD)
			try
			{
				OlympiadDatabase.save();
				System.out.println("Olympiad: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		if(Config.ALLOW_WEDDING)
			try
			{
				CoupleManager.getInstance().store();
				System.out.println("CoupleManager: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		try
		{
			FishingChampionShipManager.getInstance().shutdown();
			System.out.println("FishingChampionShipManager: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			RaidBossSpawnManager.getInstance().updateAllStatusDb(true);
			System.out.println("RaidBossSpawnManager: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			Hero.getInstance().shutdown();
			System.out.println("Hero: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		try
		{
			SpamFilter.getInstance().save();
			System.out.println("Spam: Data saved.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		if(Config.ALLOW_CURSED_WEAPONS)
			try
			{
				CursedWeaponsManager.getInstance().saveData();
				System.out.println("CursedWeaponsManager: Data saved.");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		NpcTable.storeKillsCount();
	}

	private void disconnectAllPlayers()
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			try
			{
				player.kick(true);
			}
			catch(Exception e)
			{
				System.out.println("Error while disconnect char: " + player.toString());
				e.printStackTrace();
			}
	}

	static
	{
		_log = LoggerFactory.getLogger(Shutdown.class);
		_instance = new Shutdown();
	}

	private class ShutdownCounter extends TimerTask
	{
		@Override
		public void run()
		{
			switch(shutdownCounter)
			{
				case 20:
				case 30:
				case 60:
				case 120:
				case 180:
				case 240:
				case 300:
				case 600:
				case 900:
				case 1800:
				{
					for(final Player player : GameObjectsStorage.getPlayers())
						player.sendPacket(new SystemMessage(1).addNumber(Integer.valueOf(shutdownCounter)));
					break;
				}
				case 0:
				{
					switch(shutdownMode)
					{
						case 0:
						{
							Runtime.getRuntime().exit(0);
							break;
						}
						case 2:
						{
							Runtime.getRuntime().exit(2);
							break;
						}
					}
					cancel();
					return;
				}
			}
			if(shutdownCounter <= 10)
				for(final Player player : GameObjectsStorage.getPlayers())
					player.sendPacket(new SystemMessage(1).addNumber(Integer.valueOf(shutdownCounter)));
			shutdownCounter--;
		}
	}
}
