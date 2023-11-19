package l2s.gameserver.communitybbs.Manager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.Say2;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.FriendsTable;
import l2s.gameserver.utils.Log;

public class FriendsBBSManager extends BaseBBSManager
{
	private static final int friendsPerPage = 12;
	private static final int blocksPerPage = 12;
	private static FriendsBBSManager _Instance;

	private String joinTokens(final StringTokenizer st, final String glue)
	{
		if(!st.hasMoreTokens())
			return "";
		String result = st.nextToken();
		while(st.hasMoreTokens())
			result = result + glue + st.nextToken();
		return result;
	}

	@Override
	public void parsecmd(final String command, final Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, "_");
		final String cmd = st.nextToken();
		if(cmd.equalsIgnoreCase("friendlist"))
		{
			int page = 0;
			try
			{
				page = Integer.parseInt(st.nextToken());
			}
			catch(Exception ex)
			{}
			showFriendsList(activeChar, page);
			return;
		}
		if(cmd.equalsIgnoreCase("friendinfo"))
		{
			showFriendsPI(activeChar, joinTokens(st, "_"));
			return;
		}
		if(cmd.equalsIgnoreCase("friendadd"))
		{
			FriendsTable.getInstance().TryFriendInvite(activeChar, joinTokens(st, "_"));
			showFriendsList(activeChar, 0);
			return;
		}
		if(cmd.equalsIgnoreCase("frienddel"))
		{
			FriendsTable.getInstance().TryFriendDelete(activeChar, joinTokens(st, "_"));
			showFriendsList(activeChar, 0);
			return;
		}
		if(cmd.equalsIgnoreCase("blocklist"))
		{
			int page = 0;
			try
			{
				page = Integer.parseInt(st.nextToken());
			}
			catch(Exception ex2)
			{}
			showBlockList(activeChar, page);
			return;
		}
		if(cmd.equalsIgnoreCase("blockadd"))
		{
			activeChar.addToBlockList(joinTokens(st, "_").trim());
			showBlockList(activeChar, 0);
			return;
		}
		if(cmd.equalsIgnoreCase("blockdel"))
		{
			activeChar.removeFromBlockList(joinTokens(st, "_").trim());
			showBlockList(activeChar, 0);
			return;
		}
		separateAndSend("<html><body><br><br><center>BBS function " + command + " cancelled.</center><br><br></body></html>", activeChar);
	}

	private void showFriendsPI(final Player activeChar, final String name)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=? LIMIT 1");
			statement.setString(1, name);
			rset = statement.executeQuery();
			if(!rset.next())
			{
				activeChar.sendPacket(new SystemMessage(171).addString(name));
				showFriendsList(activeChar, 0);
				return;
			}
			final int targetId = rset.getInt("obj_Id");
			if(!FriendsTable.getInstance().checkIsFriends(activeChar.getObjectId(), targetId))
			{
				activeChar.sendPacket(new SystemMessage(171).addString(name));
				showFriendsList(activeChar, 0);
				return;
			}
		}
		catch(Exception e)
		{
			showFriendsList(activeChar, 0);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		final TextBuilder htmlCode = new TextBuilder("<html><body><br>");
		htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
		final Player player = GameObjectsStorage.getPlayer(name);
		if(player != null)
		{
			final String sex = player.getSex() == 1 ? "Female" : "Male";
			String levelApprox = "low";
			if(player.getLevel() >= 60)
				levelApprox = "very high";
			else if(player.getLevel() >= 40)
				levelApprox = "high";
			else if(player.getLevel() >= 20)
				levelApprox = "medium";
			htmlCode.append("<table border=0><tr><td>").append(player.getName()).append(" (").append(sex).append(" ").append(player.getTemplate().className).append("):</td></tr>");
			htmlCode.append("<tr><td>Level: ").append(levelApprox).append("</td></tr>");
			htmlCode.append("<tr><td><br></td></tr>");
			final int uptime = (int) player.getUptime() / 1000;
			final int h = uptime / 3600;
			final int m = (uptime - h * 3600) / 60;
			final int s = uptime - h * 3600 - m * 60;
			htmlCode.append("<tr><td>Uptime: ").append(String.valueOf(h)).append("h ").append(String.valueOf(m)).append("m ").append(String.valueOf(s)).append("s</td></tr>");
			htmlCode.append("<tr><td><br></td></tr>");
			if(player.getClan() != null)
			{
				htmlCode.append("<tr><td>Clan: ").append(player.getClan().getName()).append("</td></tr>");
				htmlCode.append("<tr><td><br></td></tr>");
			}
			htmlCode.append("<tr><td><multiedit var=\"pm\" width=240 height=40><button value=\"Send PM\" action=\"Write Region PM ").append(player.getName()).append(" pm pm pm\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr><tr><td><br><button value=\"Back\" action=\"bypass _friendlist_0_\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
			htmlCode.append("</td></tr></table>");
			htmlCode.append("</body></html>");
			separateAndSend(htmlCode.toString(), activeChar);
		}
		else
			separateAndSend("<html><body><br><br><center>No player with name " + name + "</center><br><br></body></html>", activeChar);
	}

	private void showBlockList(final Player activeChar, final int page)
	{
		final TextBuilder htmlCode = new TextBuilder("<html><body><br><br>");
		htmlCode.append("<table width=755 bgcolor=000000>");
		htmlCode.append("<tr><td WIDTH=5></td><td height=10 WIDTH=750></td></tr>");
		htmlCode.append("<tr><td></td><td height=20>");
		htmlCode.append("<a action=\"bypass _friendlist_0_\">[\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439]</a> &nbsp; ");
		htmlCode.append("<a action=\"bypass _blocklist_0_\">[\u0421\u043f\u0438\u0441\u043e\u043a \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0445]</a>");
		htmlCode.append("</td></tr><tr><td></td><td height=10></td></tr></table>");
		htmlCode.append("<img src=\"L2UI.squareblank\" width=1 height=10>");
		htmlCode.append("<center><font color=\"FF3333\">\u0421\u043f\u0438\u0441\u043e\u043a \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0445</font></center>");
		float total = 0.0f;
		final String[] blockList = activeChar.getBlockList().toArray(new String[activeChar.getBlockList().size()]);
		Arrays.sort(blockList);
		if(blockList != null && blockList.length > 0)
		{
			htmlCode.append("<center><img src=\"L2UI.SquareWhite\" width=625 height=1></center><br>");
			htmlCode.append("<table height=350><tr><td height=350 valign=top><table>");
			total = blockList.length;
			int i = 0;
			for(final String blockname : blockList)
			{
				if(i >= page * 12 && i < (page + 1) * 12)
				{
					htmlCode.append("<tr><td width=20></td>");
					htmlCode.append("<td width=99 valign=top>").append(blockname).append("</td>");
					htmlCode.append("<td width=99 valign=top><button value=\"&$425;\" action=\"bypass _blockdel_").append(blockname).append("\" width=75 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
					htmlCode.append("</tr>");
				}
				++i;
			}
			htmlCode.append("</table></td></tr></table>");
			htmlCode.append("<br><center><img src=\"L2UI.SquareWhite\" width=625 height=1></center><br><br>");
		}
		final int pages = total == 0.0f ? 0 : (int) Math.ceil(total / 12.0f) - 1;
		htmlCode.append("<table>");
		htmlCode.append("<tr><td width=20></td>");
		htmlCode.append("<td valign=top width=99><edit var=\"block\" width=95></td>");
		htmlCode.append("<td width=99><button value=\"&$993;\" action=\"bypass _blockadd_ $block\" width=95 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		if(pages > 0)
		{
			final String bt_prev = page > 0 ? "<button value=\"&$543;\" action=\"bypass _blocklist_" + String.valueOf(page - 1) + "_\" width=80 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\">" : "";
			final String bt_next = pages > page ? "<button value=\"&$544;\" action=\"bypass _blocklist_" + String.valueOf(page + 1) + "_\" width=80 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\">" : "";
			htmlCode.append("<td width=250></td>");
			htmlCode.append("<td width=99>").append(bt_prev).append("</td>");
			htmlCode.append("<td align=center width=99>Page: ").append(String.valueOf(page + 1)).append("/").append(String.valueOf(pages + 1)).append("</td>");
			htmlCode.append("<td width=99>").append(bt_next).append("</td>");
		}
		htmlCode.append("</tr></table>");
		htmlCode.append("</body></html>");
		separateAndSend(htmlCode.toString(), activeChar);
	}

	private void showFriendsList(final Player activeChar, final int page)
	{
		final TextBuilder htmlCode = new TextBuilder("<html><body><br><br>");
		htmlCode.append("<table width=755 bgcolor=000000>");
		htmlCode.append("<tr><td WIDTH=5></td><td height=10 WIDTH=750></td></tr>");
		htmlCode.append("<tr><td></td><td height=20>");
		htmlCode.append("<a action=\"bypass _friendlist_0_\">[\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439]</a> &nbsp; ");
		htmlCode.append("<a action=\"bypass _blocklist_0_\">[\u0421\u043f\u0438\u0441\u043e\u043a \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0445]</a>");
		htmlCode.append("</td></tr><tr><td></td><td height=10></td></tr></table>");
		htmlCode.append("<img src=\"L2UI.squareblank\" width=1 height=10>");
		htmlCode.append("<center><font color=\"33FF33\">\u0421\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439</font></center>");
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		float total = 0.0f;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT friend_id,char_name,(SELECT COUNT(*) FROM character_friends WHERE char_id=?) AS total FROM character_friends LEFT JOIN characters ON ( character_friends.friend_id = characters.obj_Id ) WHERE char_id=? LIMIT ?,?");
			statement.setInt(1, activeChar.getObjectId());
			statement.setInt(2, activeChar.getObjectId());
			statement.setInt(3, page * 12);
			statement.setInt(4, 12);
			rset = statement.executeQuery();
			if(rset.next())
			{
				htmlCode.append("<center><img src=\"L2UI.SquareWhite\" width=625 height=1></center><br>");
				htmlCode.append("<table height=350><tr><td height=350 valign=top><table>");
				do
				{
					if(total == 0.0f)
						total = rset.getInt("total");
					final int friendId = rset.getInt("friend_id");
					final String friendName = rset.getString("char_name");
					final Player friend = friendId != 0 ? GameObjectsStorage.getPlayer(friendId) : GameObjectsStorage.getPlayer(friendName);
					htmlCode.append("<tr><td width=20></td>");
					htmlCode.append("<td width=99 valign=top>").append(friend == null ? friendName : "<a action=\"bypass _friendinfo_" + friendName + "\">" + friendName + "</a>").append("</td>");
					htmlCode.append("<td align=center width=70 valign=top>").append(friend == null ? "Offline" : "Online").append("</td>");
					htmlCode.append("<td width=99 valign=top><button value=\"&$425;\" action=\"bypass _frienddel_").append(friendName).append("\" width=75 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
					htmlCode.append("</tr>");
				} while(rset.next());
				htmlCode.append("</table></td></tr></table>");
				htmlCode.append("<br><center><img src=\"L2UI.SquareWhite\" width=625 height=1></center><br><br>");
			}
		}
		catch(Exception e)
		{
			htmlCode.append("<tr><td>Can`t show friends list, call to GM</td></tr>");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		final int pages = total == 0.0f ? 0 : (int) Math.ceil(total / 12.0f) - 1;
		htmlCode.append("<table>");
		htmlCode.append("<tr><td width=20></td>");
		htmlCode.append("<td valign=top width=99><edit var=\"invite\" width=95></td>");
		htmlCode.append("<td width=99><button value=\"&$396;\" action=\"bypass _friendadd_ $invite\" width=80 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		if(pages > 0)
		{
			final String bt_prev = page > 0 ? "<button value=\"&$543;\" action=\"bypass _friendlist_" + String.valueOf(page - 1) + "_\" width=80 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\">" : "";
			final String bt_next = pages > page ? "<button value=\"&$544;\" action=\"bypass _friendlist_" + String.valueOf(page + 1) + "_\" width=80 height=18 back=\"sek.cbui94\" fore=\"sek.cbui92\">" : "";
			htmlCode.append("<td width=250></td>");
			htmlCode.append("<td width=99>").append(bt_prev).append("</td>");
			htmlCode.append("<td align=center width=99>Page: ").append(String.valueOf(page + 1)).append("/").append(String.valueOf(pages + 1)).append("</td>");
			htmlCode.append("<td width=99>").append(bt_next).append("</td>");
		}
		htmlCode.append("</tr></table>");
		htmlCode.append("</body></html>");
		separateAndSend(htmlCode.toString(), activeChar);
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player activeChar)
	{
		if(activeChar == null)
			return;
		if(ar1.equals("PM"))
		{
			final TextBuilder htmlCode = new TextBuilder("<html><body><br>");
			htmlCode.append("<table border=0><tr><td FIXWIDTH=15></td><td align=center>Community Board<img src=\"sek.cbui355\" width=610 height=1></td></tr><tr><td FIXWIDTH=15></td><td>");
			try
			{
				final Player receiver = GameObjectsStorage.getPlayer(ar2);
				if(receiver == null)
				{
					htmlCode.append("Player not found!<br><button value=\"Back\" action=\"bypass _friendinfo_").append(ar2).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					htmlCode.append("</td></tr></table></body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
					return;
				}
				if(activeChar.getNoChannel() != 0L)
				{
					if(activeChar.getNoChannelRemained() > 0L || activeChar.getNoChannel() < 0L)
					{
						if(activeChar.getNoChannel() > 0L)
							htmlCode.append("You are banned in all chats, time remained ").append(String.valueOf(Math.round(activeChar.getNoChannelRemained() / 60000L))).append(" min.<br><button value=\"Back\" action=\"bypass _friendinfo_").append(receiver.getName()).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						else
							htmlCode.append("You are banned in all chats permanently.<br><button value=\"Back\" action=\"bypass _friendinfo_").append(receiver.getName()).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
						htmlCode.append("</td></tr></table></body></html>");
						separateAndSend(htmlCode.toString(), activeChar);
						return;
					}
					activeChar.updateNoChannel(0L);
				}
				Log.LogChat("TELL", activeChar.getName(), receiver.getName(), ar3);
				final Say2 cs = new Say2(activeChar.getObjectId(), ChatType.TELL, activeChar.getName(), ar3);
				if(!receiver.getMessageRefusal())
				{
					receiver.sendPacket(cs);
					activeChar.sendPacket(cs);
					htmlCode.append("Message Sent<br><button value=\"Back\" action=\"bypass _friendinfo_").append(receiver.getName()).append("\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
					htmlCode.append("</td></tr></table></body></html>");
					separateAndSend(htmlCode.toString(), activeChar);
				}
				else
				{
					final SystemMessage sm = new SystemMessage(176);
					activeChar.sendPacket(sm);
					showFriendsPI(activeChar, receiver.getName());
				}
			}
			catch(StringIndexOutOfBoundsException ex)
			{}
		}
		else
			separateAndSend("<html><body><br><br><center>BBS function " + ar1 + " disabled.</center><br><br></body></html>", activeChar);
	}

	public static FriendsBBSManager getInstance()
	{
		if(FriendsBBSManager._Instance == null)
			FriendsBBSManager._Instance = new FriendsBBSManager();
		return FriendsBBSManager._Instance;
	}

	static
	{
		FriendsBBSManager._Instance = null;
	}
}
