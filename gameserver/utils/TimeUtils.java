package l2s.gameserver.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils
{
	private static final SimpleDateFormat SIMPLE_FORMAT;
	private static final SimpleDateFormat _sdf;

	public static String toSimpleFormat(final Calendar cal)
	{
		return TimeUtils.SIMPLE_FORMAT.format(cal.getTime());
	}

	public static String toSimpleFormat(final long cal)
	{
		return TimeUtils.SIMPLE_FORMAT.format(cal);
	}

	public static String getTime()
	{
		return TimeUtils._sdf.format(new Date()) + "#";
	}

	static
	{
		SIMPLE_FORMAT = new SimpleDateFormat("HH:mm dd.MM.yyyy");
		_sdf = new SimpleDateFormat("dd (HH:mm:ss)");
	}
}
