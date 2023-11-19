package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.scripts.Scripts.ScriptClassAndMethod;
import l2s.gameserver.utils.HtmlUtils;
import l2s.gameserver.utils.velocity.VelocityUtils;

public class NpcHtmlMessage extends L2GameServerPacket
{
	private static Logger _log = LoggerFactory.getLogger(NpcHtmlMessage.class);

	private int _npcObjId;
	private String _html;
	private String _file = null;
	private Map<String, Object> _variables;
	private List<String> _replaces = new ArrayList<String>();
	private boolean have_appends = false;

	private static final Pattern objectId = Pattern.compile("%objectId%");
	private static final Pattern playername = Pattern.compile("%playername%");

	public NpcHtmlMessage(Player player, NpcInstance npc, String filename, int val)
	{
		_npcObjId = npc.getObjectId();
		player.setLastNpc(npc);

		List<ScriptClassAndMethod> appends = Scripts.dialogAppends.get(npc.getNpcId());
		if(appends != null && appends.size() > 0)
		{
			have_appends = true;
			if(filename != null && filename.equalsIgnoreCase("npcdefault.htm"))
				setHtml(""); // контент задается скриптами через DialogAppend_
			else
				setFile(filename);

			String replaces = "";

			// Добавить в конец странички текст, определенный в скриптах.
			Object[] script_args = new Object[] { new Integer(val) };
			for(ScriptClassAndMethod append : appends)
			{
				Object obj = Scripts.getInstance().callScripts(player, append.className, append.methodName, script_args);
				if(obj != null)
					replaces += obj;
			}

			if(!replaces.equals(""))
				replace("</body>", "\n" + HtmlUtils.bbParse(replaces) + "</body>");
		}
		else
			setFile(filename);

		replace("%npcId%", String.valueOf(npc.getNpcId()));
		replace("%npcname%", npc.getName());
		replace("%festivalMins%", SevenSignsFestival.getInstance().getTimeToNextFestivalStr());
	}

	public NpcHtmlMessage(final Player player, final NpcInstance npc)
	{
		if(npc == null)
		{
			_npcObjId = 5;
			player.setLastNpc(null);
		}
		else
		{
			_npcObjId = npc.getObjectId();
			player.setLastNpc(npc);
		}
	}

	public NpcHtmlMessage(final int npcObjId)
	{
		_npcObjId = npcObjId;
	}

	public final NpcHtmlMessage setHtml(String text)
	{
		if(!text.contains("<html>"))
			text = "<html><body>" + text + "</body></html>";
		_html = text;
		return this;
	}

	public final NpcHtmlMessage setFile(final String file)
	{
		_file = file;
		return this;
	}

	public NpcHtmlMessage replace(final String pattern, final String value)
	{
		if(pattern == null || value == null)
			return this;
		_replaces.add(pattern);
		_replaces.add(value);
		return this;
	}

	public NpcHtmlMessage addVar(String name, Object value)
	{
		if(name == null)
			throw new IllegalArgumentException("Name can't be null!");
		if(value == null)
			throw new IllegalArgumentException("Value can't be null!");
		if(name.startsWith("${"))
			throw new IllegalArgumentException("Incorrect name: " + name);
		if(_variables == null)
			_variables = new HashMap<String, Object>(2);
		_variables.put(name, value);
		return this;
	}

	public Map<String, Object> getVariables()
	{
		return _variables;
	}

	@Override
	protected final void writeImpl()
	{
		Player player = getClient().getActiveChar();
		if(player == null)
			return;

		if(_file != null) //TODO может быть не очень хорошо здесь это делать...
		{
			if(player.isGM())
				player.sendMessage("HTML: " + _file);
			String content = HtmCache.getInstance().getHtml(_file, player);
			String content2 = HtmCache.getInstance().getIfExists(_file, player);
			if(content2 == null)
				setHtml(have_appends && _file.endsWith(".htm") ? "" : content);
			else
				setHtml(content);
		}

		for(int i = 0; i < _replaces.size(); i += 2)
			_html = _html.replace(_replaces.get(i), _replaces.get(i + 1));

		if(_html == null)
			return;

		Matcher m = objectId.matcher(_html);
		if(m != null)
			_html = m.replaceAll(String.valueOf(_npcObjId));

		_html = playername.matcher(_html).replaceAll(player.getName());

		player.cleanBypasses(false);
		_html = player.encodeBypasses(_html, false);
		_html = VelocityUtils.evaluate(_html, _variables);

		if(_html.length() > 8192)
			_html = "<html><body><center>Sorry, to long html.</center></body></html>";
		writeC(15);
		writeD(_npcObjId);
		writeS(_html);
		writeD(0);
	}
}
