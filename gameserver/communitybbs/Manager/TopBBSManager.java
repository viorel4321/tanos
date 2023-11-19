package l2s.gameserver.communitybbs.Manager;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l2s.gameserver.Config;
import l2s.gameserver.communitybbs.CommunityBoard;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Scripts;

public class TopBBSManager extends BaseBBSManager
{
	private static final Pattern callsPattern;
	private static TopBBSManager _Instance;

	public void showTopPage(final Player activeChar, final String page, final String subcontent)
	{
		this.showTopPage(activeChar, page, subcontent, null);
	}

	public void showTopPage(final Player activeChar, String page, final String subcontent, final String className)
	{
		if(page == null || page.isEmpty())
			page = "index";
		else
			page = page.replace("../", "").replace("..\\", "");
		page = Config.COMMUNITYBOARD_HTML_ROOT + page + ".htm";
		String content = HtmCache.getInstance().getHtml(page, activeChar);
		if(content == null)
		{
			if(subcontent == null)
				content = "<html><body><br><br><center>404 Not Found: " + page + "</center></body></html>";
			else
				content = "<html><body>%content%</body></html>";
		}
		else
			content = CommunityBoard.htmlAll(content, activeChar);
		if(subcontent != null)
			content = content.replace("%content%", subcontent);
		final Matcher m = TopBBSManager.callsPattern.matcher(content);
		if(m.find())
		{
			final StringBuffer sb = new StringBuffer();
			m.reset();
			while(m.find())
			{
				String method = m.group(1);
				String[] method_args = method.split(":");
				method = method_args[0];
				final String methodclassName = method_args.length > 1 ? method_args[1] : className;
				method_args = m.group(2).split(",");
				final HashMap<String, Object> variables = new HashMap<String, Object>();
				variables.put("npc", null);
				final Object ret_subcontent = Scripts.getInstance().callScripts(activeChar, methodclassName, method, method_args, variables);
				m.appendReplacement(sb, ret_subcontent.toString());
			}
			m.appendTail(sb);
			content = sb.toString();
		}
		separateAndSend(content, activeChar);
	}

	@Override
	public void parsecmd(final String command, final Player activeChar)
	{
		if(command.equals("_bbstop") || command.equals("_bbshome"))
			this.showTopPage(activeChar, "index", null);
		else if(command.startsWith("_bbstop;"))
			this.showTopPage(activeChar, command.replaceFirst("_bbstop;", ""), null);
		else
			separateAndSend("<html><body><br><br><center>Command: " + command + " is not implemented yet</center><br><br></body></html>", activeChar);
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player activeChar)
	{}

	public static TopBBSManager getInstance()
	{
		return TopBBSManager._Instance;
	}

	static
	{
		callsPattern = Pattern.compile("\\{@(.+?)\\((.*?)\\)}");
		TopBBSManager._Instance = new TopBBSManager();
	}
}
