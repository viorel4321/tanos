package l2s.gameserver.communitybbs.Manager;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ShowBoard;

public abstract class BaseBBSManager
{
	public abstract void parsecmd(final String p0, final Player p1);

	public abstract void parsewrite(final String p0, final String p1, final String p2, final String p3, final String p4, final Player p5);

	protected void separateAndSend(final String html, final Player activeChar)
	{
		ShowBoard.separateAndSend(html, activeChar);
	}

	protected void send1001(final String html, final Player activeChar)
	{
		ShowBoard.send1001(html, activeChar);
	}

	protected void send1002(final Player activeChar)
	{
		ShowBoard.send1002(activeChar, " ", " ", "0");
	}

	protected void send1002(final Player activeChar, final String string, final String string2, final String string3)
	{
		ShowBoard.send1002(activeChar, string, string2, string3);
	}
}
