package l2s.gameserver.model.entity.olympiad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.TimeUtils;

public class OlympiadEndTask implements Runnable
{
	private static final Logger _log;

	@Override
	public void run()
	{
		Olympiad.endLock.lock();
		try
		{
			if(Olympiad._inCompPeriod)
			{
				if(Config.OLY_END_WAIT_COMPS)
				{
					Olympiad._scheduledOlympiadEnd = ThreadPoolManager.getInstance().schedule(new OlympiadEndTask(), 60000L);
					return;
				}
				if(Olympiad._scheduledCompEndTask != null)
					Olympiad._scheduledCompEndTask.cancel(false);
				if(Olympiad._manager != null)
				{
					Olympiad._manager.disable();
					Olympiad._manager = null;
				}
				Olympiad.doCompEnd();
			}
			Announcements.getInstance().announceToAll(new SystemMessage(1640).addNumber(Integer.valueOf(Olympiad._currentCycle)));
			Announcements.getInstance().announceToAll("Olympiad Validation Period has began");
			Olympiad.doEnd();
			try
			{
				OlympiadDatabase.save();
			}
			catch(Exception e)
			{
				OlympiadEndTask._log.error("Olympiad System: Failed to save Olympiad configuration!", e);
			}
			OlympiadEndTask._log.info("Olympiad System: Starting Validation period. Validation end " + TimeUtils.toSimpleFormat(Olympiad.getMillisToValidationEnd() + System.currentTimeMillis()));
			if(Olympiad._scheduledValdationTask != null)
				Olympiad._scheduledValdationTask.cancel(false);
			Olympiad._scheduledValdationTask = ThreadPoolManager.getInstance().schedule(new ValidationTask(), Olympiad.getMillisToValidationEnd());
		}
		catch(Exception e)
		{
			OlympiadEndTask._log.error("Olympiad System: Failed End Period!", e);
		}
		finally
		{
			Olympiad.endLock.unlock();
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(OlympiadEndTask.class);
	}
}
