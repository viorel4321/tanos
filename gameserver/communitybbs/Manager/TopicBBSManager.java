package l2s.gameserver.communitybbs.Manager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javolution.text.TextBuilder;
import l2s.gameserver.communitybbs.BB.Forum;
import l2s.gameserver.communitybbs.BB.Post;
import l2s.gameserver.communitybbs.BB.Topic;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ShowBoard;
import l2s.gameserver.tables.ClanTable;

public class TopicBBSManager extends BaseBBSManager
{
	private List<Topic> _table;
	private Map<Forum, Integer> _Maxid;
	private static TopicBBSManager _Instance;

	public static TopicBBSManager getInstance()
	{
		if(TopicBBSManager._Instance == null)
			TopicBBSManager._Instance = new TopicBBSManager();
		return TopicBBSManager._Instance;
	}

	public TopicBBSManager()
	{
		_table = new ArrayList<Topic>();
		_Maxid = new HashMap<Forum, Integer>();
	}

	public void addTopic(final Topic tt)
	{
		_table.add(tt);
	}

	public void delTopic(final Topic topic)
	{
		_table.remove(topic);
	}

	public void setMaxID(final int id, final Forum f)
	{
		_Maxid.remove(f);
		_Maxid.put(f, id);
	}

	public int getMaxID(final Forum f)
	{
		final Integer i = _Maxid.get(f);
		if(i == null)
			return 0;
		return i;
	}

	public Topic getTopicByID(final int idf)
	{
		for(final Topic t : _table)
			if(t.getID() == idf)
				return t;
		return null;
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player activeChar)
	{
		if(ar1.equals("crea"))
		{
			final Forum f = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if(f == null)
			{
				final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + ar2 + " is not implemented yet</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				f.vload();
				final Topic t = new Topic(Topic.ConstructorType.CREATE, getInstance().getMaxID(f) + 1, Integer.parseInt(ar2), ar5, Calendar.getInstance().getTimeInMillis(), activeChar.getName(), activeChar.getObjectId(), 1, 0);
				f.addtopic(t);
				getInstance().setMaxID(t.getID(), f);
				final Post p = new Post(activeChar.getName(), activeChar.getObjectId(), Calendar.getInstance().getTimeInMillis(), t.getID(), f.getID(), ar4);
				PostBBSManager.getInstance().addPostByTopic(p, t);
				parsecmd("_bbsmemo", activeChar);
			}
		}
		else if(ar1.equals("del"))
		{
			final Forum f = ForumsBBSManager.getInstance().getForumByID(Integer.parseInt(ar2));
			if(f == null)
			{
				final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + ar2 + " does not exist !</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				final Topic t = f.gettopic(Integer.parseInt(ar3));
				if(t == null)
				{
					final ShowBoard sb2 = new ShowBoard("<html><body><br><br><center>the topic: " + ar3 + " does not exist !</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb2);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				else
				{
					final Post p = PostBBSManager.getInstance().getGPosttByTopic(t);
					if(p != null)
						p.deleteme(t);
					t.deleteme(f);
					parsecmd("_bbsmemo", activeChar);
				}
			}
		}
		else
		{
			final ShowBoard sb3 = new ShowBoard("<html><body><br><br><center>the command: " + ar1 + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb3);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	@Override
	public void parsecmd(final String command, final Player activeChar)
	{
		if(activeChar == null)
			return;
		if(command.equals("_bbsmemo"))
		{
			if(activeChar.getMemo() != null)
				showTopics(activeChar.getMemo(), activeChar, 1, activeChar.getMemo().getID());
			else
				showTopics(null, activeChar, 1, 0);
		}
		else if(command.startsWith("_bbstopics;read"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			final int idf = Integer.parseInt(st.nextToken());
			String index = null;
			if(st.hasMoreTokens())
				index = st.nextToken();
			int ind = 0;
			if(index == null)
				ind = 1;
			else
				ind = Integer.parseInt(index);
			showTopics(ForumsBBSManager.getInstance().getForumByID(idf), activeChar, ind, idf);
		}
		else if(command.startsWith("_bbstopics;crea"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			final int idf = Integer.parseInt(st.nextToken());
			showNewTopic(ForumsBBSManager.getInstance().getForumByID(idf), activeChar, idf);
		}
		else if(command.startsWith("_bbstopics;del"))
		{
			final StringTokenizer st = new StringTokenizer(command, ";");
			st.nextToken();
			st.nextToken();
			final int idf = Integer.parseInt(st.nextToken());
			final int idt = Integer.parseInt(st.nextToken());
			final Forum f = ForumsBBSManager.getInstance().getForumByID(idf);
			if(f == null)
			{
				final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf + " does not exist !</center><br><br></body></html>", "101");
				activeChar.sendPacket(sb);
				activeChar.sendPacket(new ShowBoard(null, "102"));
				activeChar.sendPacket(new ShowBoard(null, "103"));
			}
			else
			{
				final Topic t = f.gettopic(idt);
				if(t == null)
				{
					final ShowBoard sb2 = new ShowBoard("<html><body><br><br><center>the topic: " + idt + " does not exist !</center><br><br></body></html>", "101");
					activeChar.sendPacket(sb2);
					activeChar.sendPacket(new ShowBoard(null, "102"));
					activeChar.sendPacket(new ShowBoard(null, "103"));
				}
				else
				{
					final Post p = PostBBSManager.getInstance().getGPosttByTopic(t);
					if(p != null)
						p.deleteme(t);
					t.deleteme(f);
					parsecmd("_bbsmemo", activeChar);
				}
			}
		}
		else
		{
			final ShowBoard sb3 = new ShowBoard("<html><body><br><br><center>the command: " + command + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb3);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	private void showNewTopic(final Forum forum, final Player activeChar, final int idf)
	{
		if(forum == null)
		{
			final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if(forum.getType() == 3)
			ShowMemoNewTopics(forum, activeChar);
		else
		{
			final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + forum.getName() + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	private void ShowMemoNewTopics(final Forum forum, final Player activeChar)
	{
		final TextBuilder html = new TextBuilder("<html>");
		html.append("<body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td width=610><img src=\"sek.cbui355\" width=\"610\" height=\"1\"><br1><img src=\"sek.cbui355\" width=\"610\" height=\"1\"></td></tr>");
		html.append("</table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=20></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29>&$413;</td>");
		html.append("<td FIXWIDTH=540><edit var = \"Title\" width=540 height=13></td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr></table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29 valign=top>&$427;</td>");
		html.append("<td align=center FIXWIDTH=540><MultiEdit var =\"Content\" width=535 height=313></td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("</table>");
		html.append("<table fixwidth=610 border=0 cellspacing=0 cellpadding=0>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("<td align=center FIXWIDTH=60 height=29>&nbsp;</td>");
		html.append("<td align=center FIXWIDTH=70><button value=\"&$140;\" action=\"Write Topic crea " + forum.getID() + " Title Content Title\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td>");
		html.append("<td align=center FIXWIDTH=70><button value = \"&$141;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td>");
		html.append("<td align=center FIXWIDTH=400>&nbsp;</td>");
		html.append("<td><img src=\"l2ui.mini_logo\" width=5 height=1></td>");
		html.append("</tr></table>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		send1001(html.toString(), activeChar);
		this.send1002(activeChar);
	}

	private void showTopics(final Forum forum, final Player activeChar, final int index, final int idf)
	{
		if(forum == null)
		{
			final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + idf + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
		else if(forum.getType() == 3)
			ShowMemoTopics(forum, activeChar, index);
		else
		{
			final ShowBoard sb = new ShowBoard("<html><body><br><br><center>the forum: " + forum.getName() + " is not implemented yet</center><br><br></body></html>", "101");
			activeChar.sendPacket(sb);
			activeChar.sendPacket(new ShowBoard(null, "102"));
			activeChar.sendPacket(new ShowBoard(null, "103"));
		}
	}

	private void ShowMemoTopics(final Forum forum, final Player activeChar, final int index)
	{
		forum.vload();
		final TextBuilder html = new TextBuilder("<html><body><br><br>");
		html.append("<table border=0 width=610><tr><td width=10></td><td width=600 align=left>");
		html.append("<a action=\"bypass _bbshome\">HOME</a>&nbsp;>&nbsp;<a action=\"bypass _bbsmemo\">Memo Form</a>");
		html.append("</td></tr>");
		html.append("</table>");
		html.append("<img src=\"L2UI.squareblank\" width=\"1\" height=\"10\">");
		html.append("<center>");
		html.append("<table border=0 cellspacing=0 cellpadding=2 bgcolor=888888 width=610>");
		html.append("<tr>");
		html.append("<td FIXWIDTH=5></td>");
		html.append("<td FIXWIDTH=415 align=center>&$413;</td>");
		html.append("<td FIXWIDTH=120 align=center></td>");
		html.append("<td FIXWIDTH=70 align=center>&$418;</td>");
		html.append("</tr>");
		html.append("</table>");
		for(int i = 0, j = getMaxID(forum) + 1; i < 12 * index && j >= 0; --j)
		{
			final Topic t = forum.gettopic(j);
			if(t != null)
			{
				if(i >= 12 * (index - 1))
				{
					html.append("<table border=0 cellspacing=0 cellpadding=5 WIDTH=610>");
					html.append("<tr>");
					html.append("<td FIXWIDTH=5></td>");
					html.append("<td FIXWIDTH=415><a action=\"bypass _bbsposts;read;" + forum.getID() + ";" + t.getID() + "\">" + t.getName() + "</a></td>");
					html.append("<td FIXWIDTH=120 align=center></td>");
					html.append("<td FIXWIDTH=70 align=center>" + DateFormat.getInstance().format(new Date(t.getDate())) + "</td>");
					html.append("</tr>");
					html.append("</table>");
					html.append("<img src=\"L2UI.Squaregray\" width=\"610\" height=\"1\">");
				}
				++i;
			}
		}
		html.append("<br>");
		html.append("<table width=610 cellspace=0 cellpadding=0>");
		html.append("<tr>");
		html.append("<td width=50>");
		html.append("<button value=\"&$422;\" action=\"bypass _bbsmemo\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">");
		html.append("</td>");
		html.append("<td width=510 align=center>");
		html.append("<table border=0><tr>");
		if(index == 1)
			html.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		else
			html.append("<td><button action=\"bypass _bbstopics;read;" + forum.getID() + ";" + (index - 1) + "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>");
		int nbp = forum.getTopicSize() / 8;
		if(nbp * 8 != ClanTable.getInstance().getClans().length)
			++nbp;
		for(int k = 1; k <= nbp; ++k)
			if(k == index)
				html.append("<td> " + k + " </td>");
			else
				html.append("<td><a action=\"bypass _bbstopics;read;" + forum.getID() + ";" + k + "\"> " + k + " </a></td>");
		if(index == nbp)
			html.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		else
			html.append("<td><button action=\"bypass _bbstopics;read;" + forum.getID() + ";" + (index + 1) + "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>");
		html.append("</tr></table> </td> ");
		html.append("<td align=right><button value = \"&$421;\" action=\"bypass _bbstopics;crea;" + forum.getID() + "\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\" ></td></tr>");
		html.append("<tr><td><img src=\"l2ui.mini_logo\" width=5 height=10></td></tr>");
		html.append("<tr> ");
		html.append("<td></td>");
		html.append("<td align=center><table border=0><tr><td></td><td><edit var = \"Search\" width=130 height=11></td>");
		html.append("<td><button value=\"&$420;\" action=\"Write 5 -2 0 Search _ _\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"> </td> </tr></table> </td>");
		html.append("</tr>");
		html.append("</table>");
		html.append("<br>");
		html.append("<br>");
		html.append("<br>");
		html.append("</center>");
		html.append("</body>");
		html.append("</html>");
		separateAndSend(html.toString(), activeChar);
	}
}
