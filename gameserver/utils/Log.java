package l2s.gameserver.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import l2s.commons.ban.BanBindType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class Log
{
	private static final Logger _log = LoggerFactory.getLogger(Log.class);

	private static final String DATE_FORMAT = "dd.MM.yy HH:mm:ss";

	private static final Logger _logChat = LoggerFactory.getLogger("chat");
	private static final Logger _logCommand = LoggerFactory.getLogger("commands");
	private static final Logger _logGm = LoggerFactory.getLogger("gmcommands");
	private static final Logger _logItems = LoggerFactory.getLogger("item");
	private static final Logger _logGame = LoggerFactory.getLogger("game");
	private static final Logger _logDebug = LoggerFactory.getLogger("debug");
	private static final Logger _logBans = LoggerFactory.getLogger("bans");

	public static final String Get = "Get";
	public static final String DoubleLink = "DoubleLink";
	public static final String Create = "Create";
	public static final String Delete = "Delete";
	public static final String Drop = "Drop";
	public static final String PvPDrop = "PvPDrop";
	public static final String Crystalize = "Crystalize";
	public static final String EnchantFail = "EnchantFail";
	public static final String Pickup = "Pickup";
	public static final String PartyPickup = "PartyPickup";
	public static final String PrivateStoreBuy = "PrivateStoreBuy";
	public static final String PrivateStoreSell = "PrivateStoreSell";
	public static final String TradeBuy = "TradeBuy";
	public static final String TradeSell = "TradeSell";
	public static final String PostRecieve = "PostRecieve";
	public static final String PostSend = "PostSend";
	public static final String PostCancel = "PostCancel";
	public static final String PostExpire = "PostExpire";
	public static final String Sell = "Sell";
	public static final String WarehouseDeposit = "WarehouseDeposit";
	public static final String WarehouseWithdraw = "WarehouseWithdraw";
	public static final String FreightWithdraw = "FreightWithdraw";
	public static final String FreightDeposit = "FreightDeposit";
	public static final String ClanWarehouseDeposit = "ClanWarehouseDeposit";
	public static final String ClanWarehouseWithdraw = "ClanWarehouseWithdraw";
	public static final String DelayedItemReceive = "DelayedItemReceive";

	public static void add(final PrintfFormat fmt, final Object[] o, final String cat)
	{
		add(fmt.sprintf(o), cat);
	}

	public static void add(final String fmt, final Object[] o, final String cat)
	{
		add(new PrintfFormat(fmt).sprintf(o), cat);
	}

	public static void add(final String text, final String cat, final Player player)
	{
		final StringBuilder output = new StringBuilder();
		output.append(cat);
		if(player != null)
		{
			output.append(' ');
			output.append(player);
		}
		output.append(' ');
		output.append(text);
		_logGame.info(output.toString());
	}

	public static void add(final String text, final String cat)
	{
		add(text, cat, null);
	}

	public static void debug(final String text)
	{
		_logDebug.debug(text);
	}

	public static void debug(final String text, final Throwable t)
	{
		_logDebug.debug(text, t);
	}

	public static void LogChat(final String type, final String player, final String target, final String text)
	{
		if(!Config.LOG_CHAT)
			return;
		final StringBuilder output = new StringBuilder();
		output.append(type);
		output.append(' ');
		output.append('[');
		output.append(player);
		if(target != null)
		{
			output.append(" -> ");
			output.append(target);
		}
		output.append(']');
		output.append(' ');
		output.append(text);
		_logChat.info(output.toString());
	}

	public static void LogItem(String className, int objId, String process, ItemInstance item)
	{
		LogItem(className, objId, process, item, item.getCount());
	}

	public static void LogItem(String className, int objId, String process, ItemInstance item, long count)
	{
		if(!Config.LOG_ITEMS)
			return;

		StringBuilder output = new StringBuilder();
		output.append(process);
		output.append(' ');
		output.append(item);
		output.append(' ');
		output.append(className);
		output.append('[');
		output.append(objId);
		output.append(']');
		output.append(' ');
		output.append(count);

		_logItems.info(output.toString());
	}

	public static void LogItem(Creature activeChar, String process, ItemInstance item)
	{
		LogItem(activeChar, process, item, item.getCount());
	}

	public static void LogItem(Creature activeChar, String process, ItemInstance item, long count)
	{
		LogItem(activeChar, process, item, count, "");
	}

	public static void LogItem(Creature activeChar, String process, ItemInstance item, String desc)
	{
		LogItem(activeChar, process, item, item.getCount(), desc);
	}

	public static void LogItem(Creature activeChar, String process, ItemInstance item, long count, String desc)
	{
		if(!Config.LOG_ITEMS)
			return;

		StringBuilder output = new StringBuilder();
		output.append(process);
		output.append(' ');
		output.append(item);
		output.append(' ');
		output.append(activeChar);
		output.append(' ');
		output.append(count);
		output.append(' ');
		output.append(desc);

		_logItems.info(output.toString());
	}

	public static void LogItem(Creature activeChar, String process, int item, long count)
	{
		if(!Config.LOG_ITEMS)
			return;

		StringBuilder output = new StringBuilder();
		output.append(process);
		output.append(' ');
		output.append(item);
		output.append(' ');
		output.append(activeChar);
		output.append(' ');
		output.append(count);

		_logItems.info(output.toString());
	}

	public static void LogCommand(final Player activeChar, final String command, final Integer success)
	{
		final StringBuffer msgb = new StringBuffer(160);
		if(activeChar.isGM())
			msgb.append("GM ");
		msgb.append(activeChar.toFullString()).append(" success:").append(success).append(" command:").append(command);
		if(activeChar.isGM())
		{
			msgb.append(" [target: ").append(activeChar.getTarget()).append("]");
			_logGm.info(msgb.toString());
		}
		else
			_logCommand.info(msgb.toString());
	}

	public static void LogBan(Player activeChar, String command, BanBindType bindType, Object bindValueObj, int endTime, String reason, boolean auth) {
		_logBans.info(String.format("%s[%s (%d)] %s BAN: BIND_TYPE[%s] BIND_VALUE[%s] END_TIME[%s] REASON[%s] COMMAND[%s]", activeChar.isGM() ? "GM" : "MODERATOR", activeChar.getName(), activeChar.getObjectId(), auth ? "AUTH" : "GAME", bindType, bindValueObj, endTime > 0 ? TimeUtils.toSimpleFormat(endTime) : "NEVER", reason, command));
	}

	public static void LogUnban(Player activeChar, String command, BanBindType bindType, Object bindValueObj, boolean auth) {
		_logBans.info(String.format("%s[%s (%d)] %s UNBAN: BIND_TYPE[%s] BIND_VALUE[%s] COMMAND[%s]", activeChar.isGM() ? "GM" : "MODERATOR", activeChar.getName(), activeChar.getObjectId(), auth ? "AUTH" : "GAME", bindType, bindValueObj, command));
	}

	public static void addLog(final String text, final String cat)
	{
		final File file = new File("log/" + cat + ".txt");
		if(!file.exists())
			try
			{
				file.createNewFile();
			}
			catch(IOException e)
			{
				_log.error("saving " + cat + " log failed, can't create file: " + e);
				return;
			}
		FileWriter save = null;
		final StringBuffer msgb = new StringBuffer();
		try
		{
			save = new FileWriter(file, true);
			final String date = new SimpleDateFormat("dd.MM.yy HH:mm:ss").format(new Date());
			msgb.append("[" + date + "]: ");
			msgb.append(text + "\n");
			save.write(msgb.toString());
		}
		catch(IOException e2)
		{
			_log.error("saving " + cat + " log failed: " + e2);
		}
		finally
		{
			try
			{
				if(save != null)
					save.close();
			}
			catch(Exception ex)
			{}
		}
	}
}
