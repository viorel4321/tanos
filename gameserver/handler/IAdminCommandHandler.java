package l2s.gameserver.handler;

import l2s.gameserver.model.Player;

public interface IAdminCommandHandler
{
	boolean useAdminCommand(final String p0, final Player p1);

	String[] getAdminCommandList();
}
