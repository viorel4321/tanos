package l2s.gameserver.model.entity.olympiad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Announcements;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.TimeUtils;

class CompStartTask implements Runnable
{
	private static final Logger _log;

	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;
		if(Olympiad._manager != null)
			Olympiad._manager.disable();
		Olympiad._inCompPeriod = true;
		Olympiad._manager = new OlympiadManager();
		new Thread(Olympiad._manager).start();
		if(Olympiad._scheduledCompEndTask != null)
			Olympiad._scheduledCompEndTask.cancel(false);
		Olympiad._scheduledCompEndTask = ThreadPoolManager.getInstance().schedule(new Olympiad.CompEndTask(), Olympiad.getMillisToCompEnd());
		Announcements.getInstance().announceToAll(new SystemMessage(1641));
		CompStartTask._log.info("Olympiad System: Olympiad Game Started. End: " + TimeUtils.toSimpleFormat(System.currentTimeMillis() + Olympiad.getMillisToCompEnd()));
	}

	static
	{
		_log = LoggerFactory.getLogger(CompStartTask.class);
	}
}
