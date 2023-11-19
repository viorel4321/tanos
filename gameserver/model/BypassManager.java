package l2s.gameserver.model;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.Strings;

public class BypassManager
{
	private static final Logger _log;
	private static final Pattern p;

	public static BypassType getBypassType(final String bypass)
	{
		switch(bypass.charAt(0))
		{
			case '0':
			{
				return BypassType.ENCODED;
			}
			case '1':
			{
				return BypassType.ENCODED_BBS;
			}
			default:
			{
				if(Strings.matches(bypass, "^(_mrsl|_clbbs|_mm|_diary|friendlist|friendmail|manor_menu_select|_match|interface).*", 32))
					return BypassType.SIMPLE;
				if(Strings.matches(bypass, "^(bbs_|_bbs|_mail|_friend|_block).*", 32))
					return BypassType.SIMPLE_BBS;
				return BypassType.SIMPLE_DIRECT;
			}
		}
	}

	public static String encode(final String html, final List<String> bypassStorage, final boolean bbs)
	{
		final Matcher m = BypassManager.p.matcher(html);
		final StringBuffer sb = new StringBuffer();
		while(m.find())
		{
			String code;
			final String bypass = code = m.group(2);
			String params = "";
			final int i = bypass.indexOf(" $");
			final boolean use_params = i >= 0;
			if(use_params)
			{
				code = bypass.substring(0, i);
				params = bypass.substring(i).replace("$", "\\$");
			}
			if(bbs)
				m.appendReplacement(sb, "\"bypass -h 1" + Integer.toHexString(bypassStorage.size()) + params + "\"");
			else
				m.appendReplacement(sb, "\"bypass -h 0" + Integer.toHexString(bypassStorage.size()) + params + "\"");
			bypassStorage.add(code);
		}
		m.appendTail(sb);
		return sb.toString();
	}

	public static DecodedBypass decode(final String bypass, final List<String> bypassStorage, final boolean bbs, final Player player)
	{
		synchronized (bypassStorage)
		{
			final String[] bypass_parsed = bypass.split(" ");
			int idx;
			try
			{
				idx = Integer.parseInt(bypass_parsed[0].substring(1), 16);
			}
			catch(Exception e)
			{
				BypassManager._log.warn("Wrong value for bypass: " + bypass);
				return null;
			}
			String bp;
			try
			{
				bp = bypassStorage.get(idx);
			}
			catch(Exception e2)
			{
				bp = null;
			}
			if(bp == null)
			{
				Log.addLog("Can't decode bypass (bypass not exists): " + (bbs ? "[bbs] " : "") + bypass + " / Player: " + player.toString() + " / Npc: " + (player.getLastNpc() == null ? "null" : player.getLastNpc().toString()), "bypass");
				return null;
			}
			final DecodedBypass result = new DecodedBypass(bp, bbs);
			for(int i = 1; i < bypass_parsed.length; ++i)
			{
				final StringBuilder sb = new StringBuilder();
				final DecodedBypass decodedBypass = result;
				decodedBypass.bypass = sb.append(decodedBypass.bypass).append(" ").append(bypass_parsed[i]).toString();
			}
			result.trim();
			return result;
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(BypassManager.class);
		p = Pattern.compile("\"(bypass +-h +)(.+?)\"");
	}

	public enum BypassType
	{
		ENCODED,
		ENCODED_BBS,
		SIMPLE,
		SIMPLE_BBS,
		SIMPLE_DIRECT;
	}

	public static class DecodedBypass
	{
		public String bypass;
		public boolean bbs;

		public DecodedBypass(final String _bypass, final boolean _bbs)
		{
			bypass = _bypass;
			bbs = _bbs;
		}

		public DecodedBypass trim()
		{
			bypass = bypass.trim();
			return this;
		}
	}
}
