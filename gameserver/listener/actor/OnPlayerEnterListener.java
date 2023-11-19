package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

public interface OnPlayerEnterListener extends PlayerListener
{
	void onPlayerEnter(final Player p0);
}
