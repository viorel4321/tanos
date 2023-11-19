package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.instancemanager.AutoAnnounce;
import l2s.gameserver.instancemanager.ServerVariables;

public class AutoAnnounces
{
	private static ScheduledFuture<?> _taskAnnounce;
	private final int _id;
	private ArrayList<String> _msg;
	private int _repeat;
	private long _nextSend;

	public AutoAnnounces(final int id)
	{
		_id = id;
	}

	public int getId()
	{
		return _id;
	}

	public void setAnnounce(final int delay, final int repeat, final ArrayList<String> msg)
	{
		_nextSend = System.currentTimeMillis() + delay * 60000L;
		_repeat = repeat;
		_msg = msg;
	}

	public void updateRepeat()
	{
		_nextSend = System.currentTimeMillis() + _repeat * 60000L;
	}

	public boolean canAnnounce()
	{
		return System.currentTimeMillis() + 10000L > _nextSend;
	}

	public ArrayList<String> getMessage()
	{
		return _msg;
	}

	public static void start()
	{
		stop();
		AutoAnnounce.reload();
		AutoAnnounces._taskAnnounce = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoAnnounce(), 60000L, 60000L);
	}

	public static void stop()
	{
		if(AutoAnnounces._taskAnnounce != null)
		{
			AutoAnnounces._taskAnnounce.cancel(false);
			AutoAnnounces._taskAnnounce = null;
		}
	}

	public static void tryStart()
	{
		if(AutoAnnounces._taskAnnounce == null && ServerVariables.getString("AutoAnnounces", "off").equalsIgnoreCase("on"))
			AutoAnnounces._taskAnnounce = ThreadPoolManager.getInstance().scheduleAtFixedRate(new AutoAnnounce(), 60000L, 60000L);
	}
}
