package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

public interface OnPlayerExitListener extends PlayerListener
{
	void onPlayerExit(final Player p0);
}
