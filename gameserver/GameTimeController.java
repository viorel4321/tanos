package l2s.gameserver;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.gameserver.instancemanager.DayNightSpawnManager;
import l2s.gameserver.listener.GameListener;
import l2s.gameserver.listener.game.OnDayNightChangeListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ClientSetTime;

public class GameTimeController
{
	public class CheckSunState implements Runnable
	{
		@Override
		public void run()
		{
			if(isNowNight())
				getInstance().getListenerEngine().onNight();
			else
				getInstance().getListenerEngine().onDay();

			DayNightSpawnManager.getInstance().notifyChangeMode();

			for(final Player player : GameObjectsStorage.getPlayers())
			{
				player.checkDayNightMessages();
				player.sendPacket(new ClientSetTime());
			}
		}
	}

	protected class GameTimeListenerList extends ListenerList<GameServer>
	{
		public void onDay()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnDayNightChangeListener.class.isInstance(listener))
					((OnDayNightChangeListener) listener).onDay();
		}

		public void onNight()
		{
			for(Listener<GameServer> listener : getListeners())
				if(OnDayNightChangeListener.class.isInstance(listener))
					((OnDayNightChangeListener) listener).onNight();
		}
	}

	private class FirstCheck implements Runnable
	{
		@Override
		public void run()
		{
			if(isNowNight())
				getInstance().getListenerEngine().onNight();
			else
				getInstance().getListenerEngine().onDay();

			DayNightSpawnManager.getInstance().notifyChangeMode();
		}
	}

	private static final Logger _log = LoggerFactory.getLogger(GameTimeController.class);

	public static final int TICKS_PER_SECOND = 10;
	public static final int MILLIS_IN_TICK = 100;

	private static final GameTimeController _instance = new GameTimeController();

	private long _gameStartTime;
	private GameTimeListenerList listenerEngine = new GameTimeListenerList();
	private Runnable _dayChangeNotify;

	public static final GameTimeController getInstance()
	{
		return _instance;
	}

	private GameTimeController()
	{
		_dayChangeNotify = new CheckSunState();
		_gameStartTime = getDayStartTime();
		ThreadPoolManager.getInstance().execute(new FirstCheck());
		final StringBuilder msg = new StringBuilder();
		msg.append("GameTimeController: ");
		msg.append("current time is ");
		msg.append(getGameHour()).append(":");
		if(getGameMin() < 10)
			msg.append("0");
		msg.append(getGameMin());
		msg.append(" in the ");
		if(isNowNight())
			msg.append("night");
		else
			msg.append("day");
		msg.append(".");
		_log.info(msg.toString());
		long nightStart = 0L;
		long dayStart = 3600000L;
		while(_gameStartTime + nightStart < System.currentTimeMillis())
			nightStart += 14400000L;
		while(_gameStartTime + dayStart < System.currentTimeMillis())
			dayStart += 14400000L;
		dayStart -= System.currentTimeMillis() - _gameStartTime;
		nightStart -= System.currentTimeMillis() - _gameStartTime;
		ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, nightStart, 14400000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(_dayChangeNotify, dayStart, 14400000L);
	}

	private long getDayStartTime()
	{
		final Calendar dayStart = Calendar.getInstance();
		final int HOUR_OF_DAY = dayStart.get(11);
		dayStart.add(11, -(HOUR_OF_DAY + 1) % 4);
		dayStart.set(12, 0);
		dayStart.set(13, 0);
		dayStart.set(14, 0);
		return dayStart.getTimeInMillis();
	}

	public boolean isNowNight()
	{
		return getGameHour() < 6;
	}

	public int getGameTime()
	{
		return getGameTicks() / 100;
	}

	public int getGameHour()
	{
		return getGameTime() / 60 % 24;
	}

	public int getGameMin()
	{
		return getGameTime() % 60;
	}

	public int getGameTicks()
	{
		return (int) ((System.currentTimeMillis() - _gameStartTime) / 100L);
	}

	public GameTimeListenerList getListenerEngine()
	{
		return listenerEngine;
	}

	public <T extends GameListener> boolean addListener(T listener)
	{
		return listenerEngine.add(listener);
	}

	public <T extends GameListener> boolean removeListener(T listener)
	{
		return listenerEngine.remove(listener);
	}
}
