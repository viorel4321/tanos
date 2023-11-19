package l2s.gameserver.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectTasks;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.Say2;
import l2s.gameserver.tables.GmListTable;

public class SpamAccount
{
	private long messageTime;
	private long spamTime;
	private long blockTime;
	private int spamMsg;
	private Map<Long, String> lastMessages;

	public SpamAccount()
	{
		lastMessages = new HashMap<Long, String>();
	}

	public boolean checkSpam(final Player player, final String msg, final int type)
	{
		if(player.isGM())
			return true;
		if(blockTime < 0L || blockTime > System.currentTimeMillis())
			return false;
		if(Config.SPAM_FILTER && ArrayUtils.contains(Config.SPAM_CHANNELS, type))
		{
			String message = msg;
			if(Config.SPAM_SKIP_SYMBOLS)
				message = message.replaceAll("[^0-9a-zA-Z\u0410-\u042f\u0430-\u044f]", "");
			if(Config.containsSpamWord(message))
			{
				if(Config.SPAM_COUNT == 1 || System.currentTimeMillis() - spamTime <= Config.SPAM_TIME * 1000L)
				{
					++spamMsg;
					if(spamMsg >= Config.SPAM_COUNT)
					{
						blockTime = Config.SPAM_BLOCK_TIME < 0 ? -1L : System.currentTimeMillis() + Config.SPAM_BLOCK_TIME * 1000L;
						spamTime = 0L;
						spamMsg = 0;
						Log.addLog("(" + player.getAccountName() + ")" + player.toString() + ": " + msg + " | period: " + (blockTime < 0L ? "Infinite" : TimeUtils.toSimpleFormat(blockTime)), "spam");
						GmListTable.broadcastToGMs(new Say2(player.getObjectId(), ChatType.ALLIANCE, player.getName(), "[BAN_SPAM]: " + msg));
						if(Config.SPAM_BAN_HWID)
							ThreadPoolManager.getInstance().schedule(new GameObjectTasks.BanHwidTask(player.getObjectId(), player.getName(), player.getHWID(), "spam", 0L, "SpamFilter"), Rnd.get(Config.SPAM_BAN_HWID_MIN, Config.SPAM_BAN_HWID_MAX) * 1000L);
					}
					else
					{
						spamTime = System.currentTimeMillis();
						GmListTable.broadcastToGMs(new Say2(player.getObjectId(), ChatType.ALLIANCE, player.getName(), "[SPAM]: " + msg));
					}
				}
				else
				{
					spamTime = System.currentTimeMillis();
					spamMsg = 1;
				}
				return false;
			}
		}
		if(!Config.SPAM_MESSAGE || !ArrayUtils.contains(Config.SPAM_MESSAGE_CHANNELS, type))
			return true;
		if(blockTime == 0L && messageTime + Config.SPAM_MESSAGE_TIME * 1000L < System.currentTimeMillis())
		{
			if(!lastMessages.isEmpty())
				lastMessages.clear();
			lastMessages.put(System.currentTimeMillis(), msg);
			messageTime = System.currentTimeMillis();
			return true;
		}
		for(final Long time : lastMessages.keySet().toArray(new Long[lastMessages.size()]))
			if(time + Config.SPAM_MESSAGE_TIME * 1000L < System.currentTimeMillis())
				lastMessages.remove(time);
		lastMessages.put(System.currentTimeMillis(), msg);
		messageTime = System.currentTimeMillis();
		if(Config.SPAM_MESSAGE_SAME)
		{
			final Map<String, Integer> countMessages = new HashMap<String, Integer>();
			for(final Map.Entry<Long, String> entry : lastMessages.entrySet())
				if(countMessages.containsKey(entry.getValue()))
				{
					final int count = countMessages.get(entry.getValue()) + 1;
					if(count >= Config.SPAM_MESSAGE_COUNT)
					{
						blockTime = Config.SPAM_MESSAGE_BLOCK_TIME < 0 ? -1L : System.currentTimeMillis() + Config.SPAM_MESSAGE_BLOCK_TIME * 1000L;
						Log.addLog("(" + player.getAccountName() + ")" + player.toString() + ": " + msg + " | MSG period: " + (blockTime < 0L ? "Infinite" : TimeUtils.toSimpleFormat(blockTime)), "spam");
						GmListTable.broadcastToGMs(new Say2(player.getObjectId(), ChatType.ALLIANCE, player.getName(), "[BAN_MSG]: " + msg));
						break;
					}
					countMessages.put(entry.getValue(), count);
				}
				else
					countMessages.put(entry.getValue(), 1);
			countMessages.clear();
		}
		else if(lastMessages.size() >= Config.SPAM_MESSAGE_COUNT)
		{
			blockTime = Config.SPAM_MESSAGE_BLOCK_TIME < 0 ? -1L : System.currentTimeMillis() + Config.SPAM_MESSAGE_BLOCK_TIME * 1000L;
			Log.addLog("(" + player.getAccountName() + ")" + player.toString() + ": " + msg + " | MSG period: " + (blockTime < 0L ? "Infinite" : TimeUtils.toSimpleFormat(blockTime)), "spam");
			GmListTable.broadcastToGMs(new Say2(player.getObjectId(), ChatType.ALLIANCE, player.getName(), "[BAN_MSG]: " + msg));
		}
		return blockTime < System.currentTimeMillis();
	}

	public boolean isSpamer()
	{
		return blockTime < 0L || blockTime > System.currentTimeMillis();
	}

	public void setBlockTime(final long n)
	{
		blockTime = n;
	}

	public long getBlockTime()
	{
		return blockTime;
	}
}
