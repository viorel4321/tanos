package l2s.gameserver.communitybbs.Manager;

import l2s.gameserver.communitybbs.CommunityBoard;
import l2s.gameserver.data.htm.HtmCache;
import l2s.gameserver.model.Player;

public class FailBBSManager extends BaseBBSManager
{
	public static FailBBSManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void parsecmd(final String command, final Player player)
	{
		if(command.equals("_bbsbash;"))
		{
			String content = HtmCache.getInstance().getHtml("CommunityBoard/10.htm", player);
			content = content.replace("%name%", player.getName());
			content = CommunityBoard.htmlAll(content, player);
			separateAndSend(content, player);
		}
	}

	@Override
	public void parsewrite(final String ar1, final String ar2, final String ar3, final String ar4, final String ar5, final Player player)
	{}

	private static class SingletonHolder
	{
		protected static final FailBBSManager _instance = new FailBBSManager();
	}
}
