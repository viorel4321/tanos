package l2s.gameserver.communitybbs.Manager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javolution.text.TextBuilder;
import l2s.gameserver.Config;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ShowBoard;

public class RegionBBSManager extends BaseBBSManager
{
	private static RegionBBSManager _Instance;

	@Override
	public void parsecmd(final String command, final Player activeChar)
	{
		if(command.equals("_bbsloc"))
			showRegion(activeChar, 0);
		else if(command.startsWith("_bbsloc;page;"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			int index = 0;
			try
			{
				index = Integer.parseInt(st.nextToken());
				showRegion(activeChar, index);
			}
			catch(NumberFormatException nfe)
			{
				final ShowBoard sb = new ShowBoard("<html><body><br><br><center>Error!</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
		}
		else
		{
			final ShowBoard sb2 = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb2);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	private void showRegion(final Player activeChar, final int startIndex)
	{
		final TextBuilder htmlCode = new TextBuilder("<html><body><br>");
		final TreeSet<String> temp = new TreeSet<String>();
		for(final Player player : GameObjectsStorage.getPlayers())
			if(player != null)
			{
				if(player.isInvisible() && player != activeChar && !activeChar.isGM())
					continue;
				temp.add(player.getName());
			}
		final String[] player_names = temp.toArray(new String[temp.size()]);
		final String tdClose = "</td>";
		final String tdOpen = "<td align=left valign=top>";
		final String trClose = "</tr>";
		final String trOpen = "<tr>";
		final String colSpacer = "<td FIXWIDTH=10></td>";
		htmlCode.append("<table>");
		final SimpleDateFormat format = new SimpleDateFormat("H:mm");
		final Calendar cal = Calendar.getInstance();
		final int t = GameTimeController.getInstance().getGameTime();
		htmlCode.append(trOpen);
		htmlCode.append("<td>Server Time: " + format.format(cal.getTime()) + tdClose);
		htmlCode.append(colSpacer);
		cal.set(11, t / 60);
		cal.set(12, t % 60);
		htmlCode.append("<td>Game Time: " + format.format(cal.getTime()) + tdClose);
		htmlCode.append(trClose);
		htmlCode.append("</table>");
		if(activeChar.getPlayerAccess().CanUseOnline)
		{
			htmlCode.append("<table>");
			htmlCode.append(trOpen);
			htmlCode.append("<td><img src=\"L2UI.SquareWhite\" width=625 height=1><br></td>");
			htmlCode.append(trClose);
			htmlCode.append(trOpen);
			htmlCode.append(tdOpen + player_names.length + " Player(s) Online:</td>");
			htmlCode.append(trClose);
			htmlCode.append("</table>");
			htmlCode.append("<table border=0>");
			htmlCode.append("<tr><td><table border=0>");
			int cell = 0;
			int n = startIndex;
			for(int i = startIndex; i < startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD && i < player_names.length; ++i)
			{
				final String player2 = player_names[i];
				if(++cell == 1)
					htmlCode.append(trOpen);
				htmlCode.append("<td align=left valign=top FIXWIDTH=75>");
				htmlCode.append(player2);
				htmlCode.append(tdClose);
				if(cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
					htmlCode.append(colSpacer);
				if(cell == Config.NAME_PER_ROW_COMMUNITYBOARD)
				{
					cell = 0;
					htmlCode.append(trClose);
				}
				++n;
			}
			if(cell > 0 && cell < Config.NAME_PER_ROW_COMMUNITYBOARD)
				htmlCode.append(trClose);
			htmlCode.append("</table></td></tr>");
			if(player_names.length > Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
			{
				htmlCode.append("<tr><td align=center valign=top>Displaying " + (startIndex + 1) + " - " + n + " player(s)</td></tr>");
				htmlCode.append("<tr><td align=center valign=top>");
				htmlCode.append("<table border=0 width=610><tr>");
				if(startIndex == 0)
					htmlCode.append("<td><button value=\"Prev\" width=50 height=15 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td>");
				else
					htmlCode.append("<td><button value=\"Prev\" action=\"bypass _bbsloc;page;" + (startIndex - Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + "\" width=50 height=15 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td>");
				htmlCode.append(colSpacer);
				if(player_names.length <= startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD)
					htmlCode.append("<td><button value=\"Next\" width=50 height=15 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td>");
				else
					htmlCode.append("<td><button value=\"Next\" action=\"bypass _bbsloc;page;" + (startIndex + Config.NAME_PAGE_SIZE_COMMUNITYBOARD) + "\" width=50 height=15 back=\"L2UI_CT1.Button_DF\" fore=\"L2UI_CT1.Button_DF\"></td>");
				htmlCode.append("</tr></table>");
				htmlCode.append("</td></tr>");
			}
			htmlCode.append("</table>");
		}
		htmlCode.append("</body></html>");
		separateAndSend(htmlCode.toString(), activeChar);
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player activeChar)
	{
		if(activeChar == null)
			return;
		final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the command: " + ar1 + " is not implemented yet</center><br><br></body></html>", "101");
		activeChar.sendPacket(sb);
		activeChar.sendPacket(new ShowBoard(null, "102"));
		activeChar.sendPacket(new ShowBoard(null, "103"));
	}

	public static RegionBBSManager getInstance()
	{
		if(RegionBBSManager._Instance == null)
			RegionBBSManager._Instance = new RegionBBSManager();
		return RegionBBSManager._Instance;
	}

	static
	{
		RegionBBSManager._Instance = null;
	}
}
