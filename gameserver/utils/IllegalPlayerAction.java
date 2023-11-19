package l2s.gameserver.utils;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.tables.GmListTable;

public final class IllegalPlayerAction implements Runnable
{
	String etc_str;
	int isBug;
	Player actor;
	public static final int INFO = 0;
	public static final int WARNING = 1;
	public static final int CRITICAL = 2;

	public IllegalPlayerAction(final Player actor, final String etc_str, final int isBug)
	{
		this.etc_str = etc_str;
		this.isBug = isBug;
		this.actor = actor;
	}

	@Override
	public void run()
	{
		final StringBuffer msgb = new StringBuffer(160);
		int punishment = -1;
		msgb.append("IllegalAction: " + actor.toString() + " " + etc_str);
		switch(isBug)
		{
			case 0:
			{
				punishment = 0;
				break;
			}
			case 1:
			{
				punishment = Config.DEFAULT_PUNISH;
				break;
			}
			case 2:
			{
				punishment = Config.BUGUSER_PUNISH;
				break;
			}
		}
		if(actor.isGM())
			punishment = 0;
		switch(punishment)
		{
			case 0:
			{
				msgb.append(" punish: none");
				actor.sendMessage(new CustomMessage("l2s.gameserver.utils.IllegalAction.case0"));
				return;
			}
			case 1:
			{
				actor.sendMessage(new CustomMessage("l2s.gameserver.utils.IllegalAction.case1"));
				try
				{
					Thread.sleep(1000L);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				actor.kick(true);
				msgb.append(" punish: kicked");
				break;
			}
			case 2:
			{
				actor.sendMessage(new CustomMessage("l2s.gameserver.utils.IllegalAction.case2"));
				actor.setAccessLevel(-100);
				actor.setAccountAccesslevel(-100, -1);
				try
				{
					Thread.sleep(1000L);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
				actor.kick(true);
				msgb.append(" punish: banned");
				Log.addLog(msgb.toString(), "banned");
				break;
			}
		}
		GmListTable.broadcastMessageToGMs(msgb.toString());
	}
}
