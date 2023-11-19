package l2s.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.NpcHtmlMessage;
import l2s.gameserver.network.l2.s2c.Say2;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class Announcements
{
	private static Logger _log;
	private static final Announcements _instance;
	private ArrayList<String> _announcements;
	private static final int _type = 10;

	public Announcements()
	{
		_announcements = new ArrayList<String>();
		loadAnnouncements();
	}

	public static Announcements getInstance()
	{
		return Announcements._instance;
	}

	public void loadAnnouncements()
	{
		_announcements.clear();
		final File file = new File("./config/Advanced/announcements.txt");
		if(file.exists())
			readFromDisk(file);
		else
			Announcements._log.info("config/Advanced/announcements.txt doesn't exist");
	}

	public void showAnnouncements(final Player activeChar)
	{
		if(activeChar == null)
			return;
		int i = 2;
		final boolean en = !activeChar.isLangRus();
		for(final String _announcement : _announcements)
		{
			if(i % 2 != 1 && en || i % 2 == 1 && !en)
				activeChar.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, activeChar.getName(), _announcement));
			++i;
		}
	}

	public void listAnnouncements(final Player activeChar)
	{
		final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
		final StringBuffer replyMSG = new StringBuffer("<html><body>");
		replyMSG.append("<table width=260><tr>");
		replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("<td width=180><center>Announcement Menu</center></td>");
		replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td>");
		replyMSG.append("</tr></table>");
		replyMSG.append("<br><br>");
		replyMSG.append("<center>Add or announce a new announcement:</center>");
		replyMSG.append("<center><multiedit var=\"new_announcement\" width=240 height=30></center><br>");
		replyMSG.append("<center><table><tr><td>");
		replyMSG.append("<button value=\"Add\" action=\"bypass -h admin_add_announcement $new_announcement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Announce\" action=\"bypass -h admin_announce_menu $new_announcement\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td>");
		replyMSG.append("<button value=\"Reload\" action=\"bypass -h admin_announce_announcements\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\">");
		replyMSG.append("</td></tr></table></center>");
		replyMSG.append("<br>");
		for(int i = 0; i < _announcements.size(); ++i)
		{
			replyMSG.append("<table width=260><tr><td width=220>" + _announcements.get(i) + "</td><td width=40>");
			replyMSG.append("<button value=\"Delete\" action=\"bypass -h admin_del_announcement " + i + "\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr></table>");
		}
		replyMSG.append("</body></html>");
		adminReply.setHtml(replyMSG.toString());
		activeChar.sendPacket(adminReply);
	}

	public void addAnnouncement(final String text)
	{
		_announcements.add(text);
		saveToDisk();
	}

	public void delAnnouncement(final int line)
	{
		_announcements.remove(line);
		saveToDisk();
	}

	private void readFromDisk(final File file)
	{
		LineNumberReader lnr = null;
		try
		{
			lnr = new LineNumberReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line;
			while((line = lnr.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\n\r");
				if(st.hasMoreTokens())
				{
					final String announcement = st.nextToken();
					_announcements.add(announcement);
				}
			}
		}
		catch(IOException e1)
		{
			Announcements._log.error("Error reading announcements", e1);
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception ex)
			{}
		}
	}

	private void saveToDisk()
	{
		final File file = new File("./config/announcements.txt");
		FileWriter save = null;
		try
		{
			save = new FileWriter(file);
			for(final String _announcement : _announcements)
			{
				save.write(_announcement);
				save.write("\r\n");
			}
		}
		catch(IOException e)
		{
			Announcements._log.warn("saving the announcements file has failed: " + e);
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

	public void announceToAll(final String text)
	{
		this.announceToAll(text, ChatType.ANNOUNCEMENT);
	}

	public void announceToAll(final String text, ChatType type)
	{
		final Say2 cs = new Say2(0, type, "", text);
		for(final Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(cs);
	}

	public void announceByCustomMessage(final String address, final String[] replacements)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			announceToPlayerByCustomMessage(player, address, replacements);
	}

	public void announceToPlayerByCustomMessage(final Player player, final String address, final String[] replacements)
	{
		final CustomMessage cm = new CustomMessage(address);
		if(replacements != null)
		{
			for(final String s : replacements)
				cm.addString(s);
		}
		player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", cm.toString(player)));
	}

	public void announceToAll(final SystemMessage sm)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(sm);
	}

	public void handleAnnounce(final String command, final int lengthToTrim)
	{
		this.handleAnnounce(command, lengthToTrim, ChatType.ANNOUNCEMENT);
	}

	public void handleAnnounce(final String command, final int lengthToTrim, ChatType type)
	{
		try
		{
			final String text = command.substring(lengthToTrim);
			getInstance().announceToAll(text, type);
		}
		catch(StringIndexOutOfBoundsException ex)
		{}
	}

	static
	{
		Announcements._log = LoggerFactory.getLogger(Announcements.class);
		_instance = new Announcements();
	}
}
