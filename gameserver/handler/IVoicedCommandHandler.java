package l2s.gameserver.handler;

import l2s.gameserver.model.Player;

public interface IVoicedCommandHandler
{
	boolean useVoicedCommand(final String p0, final Player p1, final String p2);

	String[] getVoicedCommandList();
}
