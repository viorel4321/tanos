package l2s.gameserver.utils;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;

public class Strings
{
	private static final Logger _log = LoggerFactory.getLogger(Strings.class);
	private static String[] tr;
	private static String[] trb;
	private static String[] trcode;
	private static String[][] timeType = new String[][] {
			{ "\u0441\u0435\u043a\u0443\u043d\u0434\u0430", "\u043c\u0438\u043d\u0443\u0442\u0430", "\u0447\u0430\u0441", "\u0434\u0435\u043d\u044c" },
			{ "\u0441\u0435\u043a\u0443\u043d\u0434\u044b", "\u043c\u0438\u043d\u0443\u0442\u044b", "\u0447\u0430\u0441\u0430", "\u0434\u043d\u044f" },
			{ "\u0441\u0435\u043a\u0443\u043d\u0434", "\u043c\u0438\u043d\u0443\u0442", "\u0447\u0430\u0441\u043e\u0432", "\u0434\u043d\u0435\u0439" }
		};

	public static String addSlashes(String s)
	{
		if(s == null)
			return "";
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		s = s.replace("@", "\\@");
		s = s.replace("'", "\\'");
		return s;
	}

	public static String stripSlashes(String s)
	{
		if(s == null)
			return "";
		s = s.replace("\\'", "'");
		s = s.replace("\\\\", "\\");
		return s;
	}

	public static Boolean parseBoolean(final Object x)
	{
		if(x instanceof Integer)
			return (int) x != 0;
		if(x == null)
			return false;
		if(x instanceof Boolean)
			return (Boolean) x;
		if(x instanceof Double)
			return Math.abs((double) x) < 1.0E-5;
		return !("" + x).equals("");
	}

	public static void reload()
	{
		try
		{
			String[] pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/translit.txt")).split("\n");
			Strings.tr = new String[pairs.length * 2];
			for(int i = 0; i < pairs.length; ++i)
			{
				final String[] ss = pairs[i].split(" +");
				Strings.tr[i * 2] = ss[0];
				Strings.tr[i * 2 + 1] = ss[1];
			}
			pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/translit_back.txt")).split("\n");
			Strings.trb = new String[pairs.length * 2];
			for(int i = 0; i < pairs.length; ++i)
			{
				final String[] ss = pairs[i].split(" +");
				Strings.trb[i * 2] = ss[0];
				Strings.trb[i * 2 + 1] = ss[1];
			}
			pairs = FileUtils.readFileToString(new File(Config.DATAPACK_ROOT, "data/transcode.txt")).split("\n");
			Strings.trcode = new String[pairs.length * 2];
			for(int i = 0; i < pairs.length; ++i)
			{
				final String[] ss = pairs[i].split(" +");
				Strings.trcode[i * 2] = ss[0];
				Strings.trcode[i * 2 + 1] = ss[1];
			}
		}
		catch(IOException e)
		{
			Strings._log.error("", e);
		}
		Strings._log.info("Loaded " + (Strings.tr.length + Strings.tr.length + Strings.trcode.length) + " translit entries.");
	}

	public static String translit(String s)
	{
		for(int i = 0; i < Strings.tr.length; i += 2)
			s = s.replace(Strings.tr[i], Strings.tr[i + 1]);
		return s;
	}

	public static String fromTranslit(String s, final int type)
	{
		if(type == 1)
			for(int i = 0; i < Strings.trb.length; i += 2)
				s = s.replace(Strings.trb[i], Strings.trb[i + 1]);
		else if(type == 2)
			for(int i = 0; i < Strings.trcode.length; i += 2)
				s = s.replace(Strings.trcode[i], Strings.trcode[i + 1]);
		return s;
	}

	public static String replace(final String str, final String regex, final int flags, final String replace)
	{
		return Pattern.compile(regex, flags).matcher(str).replaceAll(replace);
	}

	public static boolean matches(final String str, final String regex, final int flags)
	{
		return Pattern.compile(regex, flags).matcher(str).matches();
	}

	public static String joinStrings(final String glueStr, final String[] strings, int startIdx, int maxCount)
	{
		String result = "";
		if(startIdx < 0)
		{
			startIdx += strings.length;
			if(startIdx < 0)
				return result;
		}
		while(startIdx < strings.length && maxCount != 0)
		{
			if(!result.isEmpty() && glueStr != null && !glueStr.isEmpty())
				result += glueStr;
			result += strings[startIdx++];
			--maxCount;
		}
		return result;
	}

	public static String joinStrings(final String glueStr, final String[] strings, final int startIdx)
	{
		return joinStrings(glueStr, strings, startIdx, -1);
	}

	public static String joinStrings(final String glueStr, final String[] strings)
	{
		return joinStrings(glueStr, strings, 0);
	}

	public static String stripToSingleLine(String s)
	{
		if(s.isEmpty())
			return s;
		s = s.replaceAll("\\\\n", "\n");
		final int i = s.indexOf("\n");
		if(i > -1)
			s = s.substring(0, i);
		return s;
	}

	public static String timeSuffix(final long time, final Language lang)
	{
		return timeSuffix((int) (time / 1000L), lang);
	}

	public static String timeSuffix(int time, final Language lang)
	{
		int type = 0;
		if(time / 86400 > 1)
		{
			final int mul = time / 86400;
			return timeSuffix(time / 86400, 3, lang) + " " + timeSuffix(time - mul * 60 * 60 * 24, lang);
		}
		while(time >= 60 && type++ < 3)
			time /= 60;
		return timeSuffix(time, type, lang);
	}

	public static String timeSuffix(final int time, int type, final Language lang)
	{
		int num = time;
		if(num > 100)
			num %= 100;
		if(num > 20)
			num %= 10;
		if(type < 0)
			type = 0;
		else if(type > 3)
			type = 3;
		switch(num)
		{
			case 1:
			{
				num = 0;
				break;
			}
			case 2:
			case 3:
			case 4:
			{
				num = 1;
				break;
			}
			default:
			{
				num = 2;
				break;
			}
		}
		if(lang == Language.ENGLISH)
			return String.format("%d %s.", time, type == 0 ? "sec" : type == 1 ? "min" : "hour");
		String strTime = Integer.toString(time);
		try
		{
			strTime = strTime + " " + Strings.timeType[num][type];
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return strTime;
	}

	public static String convertToASCII(String in)
	{
		final StringBuilder out = new StringBuilder();
		for(int i = 0; i < in.length(); i++)
		{
			final char ch = in.charAt(i);
			if(ch <= 127)
				out.append(ch);
			else
				out.append("\\u").append(String.format("%04x", (int) ch));
		}
		return out.toString();
	}
}
