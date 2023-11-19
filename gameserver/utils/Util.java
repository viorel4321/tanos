package l2s.gameserver.utils;

import java.io.File;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.htm.HtmTemplates;
import l2s.gameserver.database.mysql;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.Player;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class Util
{
	private static Logger _log;
	static final String PATTERN = "0.0000000000E00";
	static final DecimalFormat df;
	public static final String defaultDelimiter = "[\\s,;:]+";
	private static NumberFormat adenaFormatter;
	private static final int MAX_ANGLE = 360;
	private static final double FRONT_MAX_ANGLE = 100.0;
	private static final double BACK_MAX_ANGLE = 40.0;

	static boolean checkIfIpInRange(String ip, final String ipRange)
	{
		int userIp1 = -1;
		int userIp2 = -1;
		int userIp3 = -1;
		int userIp4 = -1;
		ip = ip.replace(".", ",");
		for(final String s : ip.split(","))
			if(userIp1 == -1)
				userIp1 = Integer.parseInt(s);
			else if(userIp2 == -1)
				userIp2 = Integer.parseInt(s);
			else if(userIp3 == -1)
				userIp3 = Integer.parseInt(s);
			else
				userIp4 = Integer.parseInt(s);
		int ipMin1 = -1;
		int ipMin2 = -1;
		int ipMin3 = -1;
		int ipMin4 = -1;
		int ipMax1 = -1;
		int ipMax2 = -1;
		int ipMax3 = -1;
		int ipMax4 = -1;
		final StringTokenizer st = new StringTokenizer(ipRange, "-");
		if(Config.DEBUG)
			System.out.println("Tokens in string " + ipRange + ": " + st.countTokens());
		if(st.countTokens() == 2)
		{
			String firstIp = st.nextToken();
			String lastIp = st.nextToken();
			firstIp = firstIp.replace(".", ",");
			lastIp = lastIp.replace(".", ",");
			for(final String s2 : firstIp.split(","))
				if(ipMin1 == -1)
					ipMin1 = Integer.parseInt(s2);
				else if(ipMin2 == -1)
					ipMin2 = Integer.parseInt(s2);
				else if(ipMin3 == -1)
					ipMin3 = Integer.parseInt(s2);
				else
					ipMin4 = Integer.parseInt(s2);
			for(final String s3 : lastIp.split(","))
				if(ipMax1 == -1)
					ipMax1 = Integer.parseInt(s3);
				else if(ipMax2 == -1)
					ipMax2 = Integer.parseInt(s3);
				else if(ipMax3 == -1)
					ipMax3 = Integer.parseInt(s3);
				else
					ipMax4 = Integer.parseInt(s3);
			if(userIp1 > ipMin1 && userIp1 < ipMax1)
				return true;
			if(userIp1 < ipMin1 || userIp1 > ipMax1)
				return false;
			if(userIp1 == ipMin1 && userIp1 != ipMax1)
				return userIp2 > ipMin2 || userIp2 >= ipMin2 && (userIp3 > ipMin3 || userIp3 >= ipMin3 && userIp4 >= ipMin4);
			if(userIp1 != ipMin1 && userIp1 == ipMax1)
				return userIp2 < ipMax2 || userIp2 <= ipMax2 && (userIp3 < ipMax3 || userIp3 <= ipMax3 && userIp4 <= ipMax4);
			if(userIp2 > ipMin2 && userIp2 < ipMax2)
				return true;
			if(userIp2 < ipMin2 || userIp2 > ipMax2)
				return false;
			if(userIp2 == ipMin2 && userIp2 != ipMax2)
				return userIp3 > ipMin3 || userIp3 >= ipMin3 && userIp4 >= ipMin4;
			if(userIp2 != ipMin2 && userIp2 == ipMax2)
				return userIp3 < ipMax3 || userIp3 <= ipMax3 && userIp4 <= ipMax4;
			if(userIp3 > ipMin3 && userIp3 < ipMax3)
				return true;
			if(userIp3 < ipMin3 || userIp3 > ipMax3)
				return false;
			if(userIp3 == ipMin3 && userIp3 != ipMax3)
				return userIp4 >= ipMin4;
			if(userIp3 != ipMin3 && userIp3 == ipMax3)
				return userIp4 <= ipMax4;
			if(userIp4 >= ipMin4 && userIp4 <= ipMax4)
				return true;
			if(userIp4 < ipMin4 || userIp4 > ipMax4)
				return false;
		}
		else if(st.countTokens() == 1)
		{
			if(ip.equalsIgnoreCase(ipRange))
				return true;
		}
		else
			Util._log.warn("Error in internal ip detection: " + ipRange);
		return false;
	}

	public static boolean isMatchingRegexp(final String text, final String template)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e)
		{
			e.printStackTrace();
		}
		if(pattern == null)
			return false;
		final Matcher regexp = pattern.matcher(text);
		return regexp.matches();
	}

	public static String replaceRegexp(String source, final String template, final String replacement)
	{
		Pattern pattern = null;
		try
		{
			pattern = Pattern.compile(template);
		}
		catch(PatternSyntaxException e)
		{
			e.printStackTrace();
		}
		if(pattern != null)
		{
			final Matcher regexp = pattern.matcher(source);
			source = regexp.replaceAll(replacement);
		}
		return source;
	}

	public static String printData(final byte[] data, final int len)
	{
		final StringBuffer result = new StringBuffer();
		int counter = 0;
		for(int i = 0; i < len; ++i)
		{
			if(counter % 16 == 0)
				result.append(fillHex(i, 4) + ": ");
			result.append(fillHex(data[i] & 0xFF, 2) + " ");
			if(++counter == 16)
			{
				result.append("   ");
				int charpoint = i - 15;
				for(int a = 0; a < 16; ++a)
				{
					final int t1 = data[charpoint++];
					if(t1 > 31 && t1 < 128)
						result.append((char) t1);
					else
						result.append('.');
				}
				result.append("\n");
				counter = 0;
			}
		}
		final int rest = data.length % 16;
		if(rest > 0)
		{
			for(int j = 0; j < 17 - rest; ++j)
				result.append("   ");
			int charpoint = data.length - rest;
			for(int a = 0; a < rest; ++a)
			{
				final int t1 = data[charpoint++];
				if(t1 > 31 && t1 < 128)
					result.append((char) t1);
				else
					result.append('.');
			}
			result.append("\n");
		}
		return result.toString();
	}

	public static String fillHex(final int data, final int digits)
	{
		String number = Integer.toHexString(data);
		for(int i = number.length(); i < digits; ++i)
			number = "0" + number;
		return number;
	}

	public static String printData(final byte[] raw)
	{
		return printData(raw, raw.length);
	}

	public static long getTime()
	{
		return (System.currentTimeMillis() + 500L) / 1000L;
	}

	public static String formatDouble(final double x, final String nanString, final boolean forceExponents)
	{
		if(Double.isNaN(x))
			return nanString;
		if(forceExponents)
			return Util.df.format(x);
		if((long) x == x)
			return String.valueOf((long) x);
		return String.valueOf(x);
	}

	public static void handleIllegalPlayerAction(final Player actor, final String etc_str, final int isBug)
	{
		ThreadPoolManager.getInstance().schedule(new IllegalPlayerAction(actor, etc_str, isBug), 500L);
	}

	public static String getRelativePath(final File base, final File file)
	{
		return file.toURI().getPath().substring(base.toURI().getPath().length());
	}

	public static PositionUtils.TargetDirection getDirectionTo(final Creature target, final Creature attacker)
	{
		if(target == null || attacker == null)
			return PositionUtils.TargetDirection.NONE;
		if(isBehind(target, attacker))
			return PositionUtils.TargetDirection.BEHIND;
		if(isInFrontOf(target, attacker))
			return PositionUtils.TargetDirection.FRONT;
		return PositionUtils.TargetDirection.SIDE;
	}

	public static boolean isInFrontOf(final Creature target, final Creature attacker)
	{
		if(target == null)
			return false;
		final double angleTarget = calculateAngleFrom(target, attacker);
		final double angleChar = convertHeadingToDegree(target.getHeading());
		double angleDiff = angleChar - angleTarget;
		if(angleDiff <= -260.0)
			angleDiff += 360.0;
		if(angleDiff >= 260.0)
			angleDiff -= 360.0;
		return Math.abs(angleDiff) <= 100.0;
	}

	public static boolean isBehind(final Creature target, final Creature attacker)
	{
		if(target == null)
			return false;
		final double angleChar = calculateAngleFrom(attacker, target);
		final double angleTarget = convertHeadingToDegree(target.getHeading());
		double angleDiff = angleChar - angleTarget;
		if(angleDiff <= -320.0)
			angleDiff += 360.0;
		if(angleDiff >= 320.0)
			angleDiff -= 360.0;
		return Math.abs(angleDiff) <= 40.0;
	}

	public static boolean isFacing(final Creature attacker, final GameObject target, final int maxAngle)
	{
		if(target == null)
			return false;
		final double maxAngleDiff = maxAngle / 2;
		final double angleTarget = calculateAngleFrom(attacker, target);
		final double angleChar = convertHeadingToDegree(attacker.getHeading());
		double angleDiff = angleChar - angleTarget;
		if(angleDiff <= -360.0 + maxAngleDiff)
			angleDiff += 360.0;
		if(angleDiff >= 360.0 - maxAngleDiff)
			angleDiff -= 360.0;
		return Math.abs(angleDiff) <= maxAngleDiff;
	}

	public static double calculateAngleFrom(final GameObject obj1, final GameObject obj2)
	{
		return calculateAngleFrom(obj1.getX(), obj1.getY(), obj2.getX(), obj2.getY());
	}

	public static double calculateAngleFrom(final int obj1X, final int obj1Y, final int obj2X, final int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if(angleTarget < 0.0)
			angleTarget += 360.0;
		return angleTarget;
	}

	public static Location getPointInRadius(final Location a, final Location b, final double angle)
	{
		final double rad = Math.toRadians(angle + calculateAngleFrom(a.getX(), a.getY(), b.getX(), b.getY()));
		final int r = (int) Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY()));
		return new Location((int) Math.round(b.getX() + Math.cos(rad) * r), (int) Math.round(b.getY() + Math.sin(rad) * r), b.getZ());
	}

	public static Location getPointInRadius(final Location a, final int radius, final double angle)
	{
		final double rad = Math.toRadians(angle);
		return new Location((int) Math.round(a.getX() + Math.cos(rad) * radius), (int) Math.round(a.getY() + Math.sin(rad) * radius), a.getZ());
	}

	public static boolean checkIfInRange(final int range, final int x1, final int y1, final int x2, final int y2)
	{
		return checkIfInRange(range, x1, y1, 0, x2, y2, 0, false);
	}

	public static boolean checkIfInRange(final int range, final int x1, final int y1, final int z1, final int x2, final int y2, final int z2, final boolean includeZAxis)
	{
		final int dx = x1 - x2;
		final int dy = y1 - y2;
		if(includeZAxis)
		{
			final int dz = z1 - z2;
			return dx * dx + dy * dy + dz * dz <= range * range;
		}
		return dx * dx + dy * dy <= range * range;
	}

	public static boolean checkIfInRange(final int range, final GameObject obj1, final GameObject obj2, final boolean includeZAxis)
	{
		return obj1 != null && obj2 != null && checkIfInRange(range, obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}

	public static double convertHeadingToDegree(final int heading)
	{
		return heading / 182.044444444;
	}

	public static double convertHeadingToRadian(final int heading)
	{
		return Math.toRadians(convertHeadingToDegree(heading) - 90.0);
	}

	public static int calculateHeadingFrom(final GameObject obj1, final GameObject obj2)
	{
		return convertDegreeToClientHeading(calculateAngleFrom(obj1, obj2));
	}

	public static int calculateHeadingFrom(final int obj1X, final int obj1Y, final int obj2X, final int obj2Y)
	{
		double angleTarget = Math.toDegrees(Math.atan2(obj2Y - obj1Y, obj2X - obj1X));
		if(angleTarget < 0.0)
			angleTarget += 360.0;
		return (int) (angleTarget * 182.044444444);
	}

	public static final int calculateHeadingFrom(final double dx, final double dy)
	{
		double angleTarget = Math.toDegrees(Math.atan2(dy, dx));
		if(angleTarget < 0.0)
			angleTarget += 360.0;
		return (int) (angleTarget * 182.044444444);
	}

	public static final int convertDegreeToClientHeading(double degree)
	{
		if(degree < 0.0)
			degree += 360.0;
		return (int) (degree * 182.044444444);
	}

	public static double calculateDistance(final int x1, final int y1, final int z1, final int x2, final int y2)
	{
		return calculateDistance(x1, y1, 0, x2, y2, 0, false);
	}

	public static double calculateDistance(final int x1, final int y1, final int z1, final int x2, final int y2, final int z2, final boolean includeZAxis)
	{
		final long dx = x1 - x2;
		final long dy = y1 - y2;
		if(includeZAxis)
		{
			final long dz = z1 - z2;
			return Math.sqrt(dx * dx + dy * dy + dz * dz);
		}
		return Math.sqrt(dx * dx + dy * dy);
	}

	public static double calculateDistance(final GameObject obj1, final GameObject obj2, final boolean includeZAxis)
	{
		if(obj1 == null || obj2 == null)
			return 2.147483647E9;
		return calculateDistance(obj1.getX(), obj1.getY(), obj1.getZ(), obj2.getX(), obj2.getY(), obj2.getZ(), includeZAxis);
	}

	public static short getShort(final byte[] bs, final int offset)
	{
		return (short) (bs[offset + 1] << 8 | bs[offset] & 0xFF);
	}

	public static double getDistance(final GameObject a1, final GameObject a2)
	{
		return getDistance(a1.getX(), a2.getY(), a2.getX(), a2.getY());
	}

	public static double getDistance(final Location loc1, final Location loc2)
	{
		return getDistance(loc1.getX(), loc1.getY(), loc2.getX(), loc2.getY());
	}

	public static double getDistance(final int x1, final int y1, final int x2, final int y2)
	{
		return Math.hypot(x1 - x2, y1 - y2);
	}

	public static int getHeadingTo(final GameObject actor, final GameObject target)
	{
		if(actor == null || target == null || target == actor)
			return -1;
		return getHeadingTo(actor.getLoc(), target.getLoc());
	}

	public static int getHeadingTo(final Location actor, final Location target)
	{
		if(actor == null || target == null || target.equals(actor))
			return -1;
		final int dx = target.x - actor.x;
		final int dy = target.y - actor.y;
		int heading = target.h - (int) (Math.atan2(-dy, -dx) * 10430.378350470453 + 32768.0);
		if(heading < 0)
			heading = heading + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(heading > 65535)
			heading &= 0xFFFF;
		return heading;
	}

	public static String formatAdena(final long amount)
	{
		return Util.adenaFormatter.format(amount);
	}

	public static int getPacketLength(final byte first, final byte second)
	{
		int lenght = first & 0xFF;
		return lenght |= second << 8 & 0xFF00;
	}

	public static byte[] writeLenght(final byte[] data)
	{
		final int newLenght = data.length + 2;
		final byte[] result = new byte[newLenght];
		result[0] = (byte) (newLenght & 0xFF);
		result[1] = (byte) (newLenght >> 8 & 0xFF);
		System.arraycopy(data, 0, result, 2, data.length);
		return result;
	}

	public static byte[] generateHex(final int size)
	{
		final byte[] array = new byte[size];
		final Random rnd = new Random();
		for(int i = 0; i < size; ++i)
			array[i] = (byte) rnd.nextInt(256);
		if(Config.DEBUG)
			Util._log.info("Generated random String:  \"" + array + "\"");
		return array;
	}

	public static String formatTime(long time)
	{
		String ret = "";
		final long numDays = time / 86400L;
		time -= numDays * 86400L;
		final long numHours = time / 3600L;
		time -= numHours * 3600L;
		final long numMins = time / 60L;
		final long numSeconds;
		time = numSeconds = time - numMins * 60L;
		if(numDays > 0L)
			ret = ret + numDays + "d ";
		if(numHours > 0L)
			ret = ret + numHours + "h ";
		if(numMins > 0L)
			ret = ret + numMins + "m ";
		if(numSeconds > 0L)
			ret = ret + numSeconds + "s";
		return ret.trim();
	}

	public static long rollDrop(final long min, final long max, double calcChance, final boolean rate)
	{
		if(calcChance <= 0.0 || min <= 0L || max <= 0L)
			return 0L;
		int dropmult = 1;
		if(rate)
			calcChance *= Config.RATE_DROP_ITEMS;
		if(calcChance > 1000000.0)
			if(calcChance % 1000000.0 == 0.0)
				dropmult = (int) (calcChance / 1000000.0);
			else
			{
				dropmult = (int) Math.ceil(calcChance / 1000000.0);
				calcChance /= dropmult;
			}
		return Rnd.chance(calcChance / 10000.0) ? Rnd.get(min * dropmult, max * dropmult) : 0L;
	}

	public static int packInt(final int[] a, final int bits) throws Exception
	{
		final int m = 32 / bits;
		if(a.length > m)
			throw new Exception("Overflow");
		int result = 0;
		final int mval = (int) Math.pow(2.0, bits);
		for(int i = 0; i < m; ++i)
		{
			result <<= bits;
			int next;
			if(a.length > i)
			{
				next = a[i];
				if(next >= mval || next < 0)
					throw new Exception("Overload, value is out of range");
			}
			else
				next = 0;
			result += next;
		}
		return result;
	}

	public static int[] unpackInt(int a, final int bits)
	{
		final int m = 32 / bits;
		final int mval = (int) Math.pow(2.0, bits);
		final int[] result = new int[m];
		for(int i = m; i > 0; --i)
		{
			final int next = a;
			a >>= bits;
			result[i - 1] = next - a * mval;
		}
		return result;
	}

	public static double[] parseCommaSeparatedDoubleArray(final String s)
	{
		if(s.isEmpty())
			return new double[0];
		final String[] values = s.split("[\\s,;:]+");
		final double[] val = new double[values.length];
		for(int i = 0; i < val.length; ++i)
			val[i] = Double.parseDouble(values[i]);
		return val;
	}

	public static float[] parseCommaSeparatedFloatArray(final String s)
	{
		if(s.isEmpty())
			return new float[0];
		final String[] values = s.split("[\\s,;:]+");
		final float[] val = new float[values.length];
		for(int i = 0; i < val.length; ++i)
			val[i] = Float.parseFloat(values[i]);
		return val;
	}

	public static int[] parseCommaSeparatedIntegerArray(final String s)
	{
		if(s.isEmpty())
			return new int[0];
		final String[] values = s.split("[\\s,;:]+");
		final int[] val = new int[values.length];
		for(int i = 0; i < val.length; ++i)
			val[i] = Integer.parseInt(values[i]);
		return val;
	}

	public static boolean isNumber(final String s)
	{
		try
		{
			Double.parseDouble(s);
		}
		catch(NumberFormatException e)
		{
			return false;
		}
		return true;
	}

	public static Object[] addElementToArray(Object[] arr, final Object o, final Class<?> c)
	{
		if(arr == null)
		{
			arr = (Object[]) Array.newInstance(c, 1);
			arr[0] = o;
			return arr;
		}
		final int len = arr.length;
		final Object[] tmp = (Object[]) Array.newInstance(c, len + 1);
		System.arraycopy(arr, 0, tmp, 0, len);
		tmp[len] = o;
		return tmp;
	}

	public static Location convertVehicleCoordToWorld(final Location vehicleWorldPos, final Location inVehiclePos)
	{
		double angle = convertHeadingToDegree(vehicleWorldPos.h) + 90.0;
		if(angle > 360.0)
			angle -= 360.0;
		final double sinA = Math.sin(Math.toRadians(angle));
		final double cosA = Math.cos(Math.toRadians(angle));
		final int x = (int) Math.round(inVehiclePos.getX() * cosA - inVehiclePos.getY() * sinA + vehicleWorldPos.getX());
		final int y = (int) Math.round(inVehiclePos.getX() * sinA + inVehiclePos.getY() * cosA + vehicleWorldPos.getY());
		final int z = vehicleWorldPos.getZ() + inVehiclePos.getZ();
		return new Location(x, y, z);
	}

	public static Location convertWorldCoordToVehicle(final Location vehicleWorldPos, final Location worldPos)
	{
		final double angle = convertHeadingToDegree(vehicleWorldPos.h) + 90.0;
		final int Xn = -vehicleWorldPos.getX() + worldPos.getX();
		final int Yn = -vehicleWorldPos.getY() + worldPos.getY();
		final int z = Math.max(vehicleWorldPos.getZ() - worldPos.getZ(), vehicleWorldPos.getZ() - 22);
		return getPointInRadius(new Location(Xn, Yn, 0), new Location(0, 0, z), -angle);
	}

	public static String format(final long amount, final boolean ru)
	{
		String n;
		int count;
		for(n = String.valueOf(amount), count = 0; n.length() > 3 && n.endsWith("000"); n = n.substring(0, n.length() - 3), ++count)
		{}
		while(count > 0)
		{
			n += ru ? "\u043a" : "k";
			--count;
		}
		return n;
	}

	public static String dayFormat(final boolean ru, final String n)
	{
		final int i = Integer.parseInt(n.substring(n.length() - 1, n.length()));
		if(ru)
		{
			if(i == 1)
				return "\u0434\u0435\u043d\u044c";
			if(i > 1 && i < 5)
				return "\u0434\u043d\u044f";
			return "\u0434\u043d\u0435\u0439";
		}
		else
		{
			if(i == 1)
				return "day";
			return "days";
		}
	}

	public static String hourFormat(final boolean ru, final String n)
	{
		final int i = Integer.parseInt(n.substring(n.length() - 1, n.length()));
		if(ru)
		{
			if(i == 1)
				return "\u0447\u0430\u0441";
			if(i > 1 && i < 5)
				return "\u0447\u0430\u0441\u0430";
			return "\u0447\u0430\u0441\u043e\u0432";
		}
		else
		{
			if(i == 1)
				return "hour";
			return "hours";
		}
	}

	public static String minuteFormat(final boolean ru, final String n)
	{
		final int i = Integer.parseInt(n.substring(n.length() - 1, n.length()));
		if(ru)
		{
			if(i == 1)
				return "\u043c\u0438\u043d\u0443\u0442\u0443";
			if(i > 1 && i < 5)
				return "\u043c\u0438\u043d\u0443\u0442\u044b";
			return "\u043c\u0438\u043d\u0443\u0442";
		}
		else
		{
			if(i == 1)
				return "minute";
			return "minutes";
		}
	}

	public static String secondFormat(final boolean ru, final String n)
	{
		final int i = Integer.parseInt(n.substring(n.length() - 1, n.length()));
		if(ru)
		{
			if(i == 1)
				return "\u0441\u0435\u043a\u0443\u043d\u0434\u0443";
			if(i > 1 && i < 5)
				return "\u0441\u0435\u043a\u0443\u043d\u0434\u044b";
			return "\u0441\u0435\u043a\u0443\u043d\u0434";
		}
		else
		{
			if(i == 1)
				return "second";
			return "seconds";
		}
	}

	public static void jail(final Player player, final int ms)
	{
		player.setVar("jailedFrom", player.getX() + ";" + player.getY() + ";" + player.getZ());
		player._unjailTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.TeleportTask(player, player.getLoc()), ms * 60000L);
		final int srok = 60 * ms + (int) (System.currentTimeMillis() / 1000L);
		player.setVar("jailed", String.valueOf(srok));
		player.teleToLocation(-114648, -249384, -2984);
	}

	public static void giveItem(final int objId, final int itemId, final int count)
	{
		giveItem(objId, itemId, count, 0);
	}

	public static void giveItem(final int objId, final int itemId, final int count, final int enchant)
	{
		final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if(item == null)
		{
			Util._log.warn("Not found item " + itemId + " by giveItem!");
			return;
		}
		if(item.isStackable())
		{
			final int itemObjId = mysql.simple_get_int("object_id", "items", "`owner_id` = " + objId + " AND `item_id` = " + itemId + " AND loc='INVENTORY'");
			if(itemObjId > 0)
				mysql.set("UPDATE `items` SET `count`=`count` + " + count + " WHERE `object_id`=" + itemObjId + " LIMIT 1");
			else
			{
				final int id = IdFactory.getInstance().getNextId();
				final int maxId = mysql.simple_get_int("MAX(loc_data) AS loc_data", "items", "`owner_id` = " + objId + " AND loc='INVENTORY'") + 1;
				mysql.set("INSERT INTO `items` (`owner_id`,`object_id`,`item_id`,`name`,`count`,`enchant_level`,`class`,`loc`,`loc_data`) VALUES (" + objId + "," + id + "," + itemId + ",'" + item.getName() + "'," + count + ",'0','" + item.getItemClass().name() + "','INVENTORY'," + maxId + ")");
			}
		}
		else
		{
			int id2 = IdFactory.getInstance().getNextId();
			int maxId2 = mysql.simple_get_int("MAX(loc_data) AS loc_data", "items", "`owner_id` = " + objId + " AND loc='INVENTORY'") + 1;
			mysql.set("INSERT INTO `items` (`owner_id`,`object_id`,`item_id`,`name`,`count`,`enchant_level`,`class`,`loc`,`loc_data`) VALUES (" + objId + "," + id2 + "," + itemId + ",'" + item.getName() + "'," + count + "," + enchant + ",'" + item.getItemClass().name() + "','INVENTORY'," + maxId2 + ")");
			if(count > 1)
				for(int i = 1; i < count; ++i)
				{
					id2 = IdFactory.getInstance().getNextId();
					++maxId2;
					mysql.set("INSERT INTO `items` (`owner_id`,`object_id`,`item_id`,`name`,`count`,`enchant_level`,`class`,`loc`,`loc_data`) VALUES (" + objId + "," + id2 + "," + itemId + ",'" + item.getName() + "'," + count + "," + enchant + ",'" + item.getItemClass().name() + "','INVENTORY'," + maxId2 + ")");
				}
		}
	}

	public static String maskHWID(final String h)
	{
		if(Config.MASK_HWID <= 0)
			return h;
		if(Config.MASK_HWID == 14)
			return h.substring(0, 20);
		if(Config.MASK_HWID == 12)
			return h.substring(0, 12);
		if(Config.MASK_HWID == 1)
			return h.substring(20, 28);
		if(Config.MASK_HWID == 2)
			return h.substring(12, 20);
		if(Config.MASK_HWID == 3)
			return h.substring(12, 28);
		if(Config.MASK_HWID == 4)
			return h.substring(4, 12);
		if(Config.MASK_HWID == 5)
			return h.substring(4, 12) + h.substring(20, 28);
		if(Config.MASK_HWID == 6)
			return h.substring(4, 20);
		if(Config.MASK_HWID == 7)
			return h.substring(4, 28);
		if(Config.MASK_HWID == 8)
			return h.substring(0, 4);
		if(Config.MASK_HWID == 9)
			return h.substring(0, 4) + h.substring(20, 28);
		if(Config.MASK_HWID == 10)
			return h.substring(0, 4) + h.substring(12, 20);
		if(Config.MASK_HWID == 11)
			return h.substring(0, 4) + h.substring(12, 28);
		if(Config.MASK_HWID == 13)
			return h.substring(0, 12) + h.substring(20, 28);
		return h.substring(0, 28);
	}

	private static Pattern _pattern = Pattern.compile("<!--(TEMPLATE|TEMPLET)(\\d+)(.*?)(TEMPLATE|TEMPLET)-->", Pattern.DOTALL);

	public static HtmTemplates parseTemplates(String filename, Language lang, String html)
	{
		if(html == null)
			return null;

		Matcher m = _pattern.matcher(html);
		HtmTemplates tpls = new HtmTemplates(filename, lang);
		while(m.find())
		{
			tpls.put(Integer.parseInt(m.group(2)), m.group(3));
			html = html.replace(m.group(0), "");
		}

		tpls.put(0, html);
		return tpls;
	}

	static
	{
		Util._log = LoggerFactory.getLogger(Util.class);
		Util.adenaFormatter = NumberFormat.getIntegerInstance(Locale.FRANCE);
		(df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.ENGLISH)).applyPattern("0.0000000000E00");
		Util.df.setPositivePrefix("+");
	}
}
