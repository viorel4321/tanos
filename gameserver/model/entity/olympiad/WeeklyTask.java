package l2s.gameserver.model.entity.olympiad;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;

public class WeeklyTask implements Runnable
{
	private static final Logger _log;

	@Override
	public void run()
	{
		Olympiad.addWeeklyPoints();
		WeeklyTask._log.info("Olympiad System: Added weekly points to nobles.");
		final Calendar nextChange = Calendar.getInstance();
		Olympiad._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
	}

	static
	{
		_log = LoggerFactory.getLogger(WeeklyTask.class);
	}
}
