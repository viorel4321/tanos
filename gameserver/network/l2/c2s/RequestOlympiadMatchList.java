package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.olympiad.Olympiad;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;

public class RequestOlympiadMatchList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(!activeChar.inObserverMode())
			return;
		final String[] matches = Olympiad.getAllTitles();
		final NpcHtmlMessage reply = new NpcHtmlMessage(0);
		final StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<center><br>Grand Olympiad Game View");
		replyMSG.append("<table width=270 border=0 bgcolor=\"000000\">");
		replyMSG.append("<tr><td fixwidth=30>NO.</td><td>Status &nbsp; &nbsp; Player1 / Player2</td></tr>");
		for(int i = 0; i < matches.length; ++i)
		{
			final int n = i + 1;
			replyMSG.append("<tr><td fixwidth=30><a action=\"bypass -h oly_" + i + "\">" + n + "</a></td><td>" + matches[i] + "</td></tr>");
		}
		replyMSG.append("</table></center></body></html>");
		reply.setHtml(replyMSG.toString());
		activeChar.sendPacket(reply);
	}
}
