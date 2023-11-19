package l2s.gameserver.handler;

import l2s.gameserver.model.Player;

public interface IUserCommandHandler
{
	boolean useUserCommand(final int p0, final Player p1);

	int[] getUserCommandList();
}
