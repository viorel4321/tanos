package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

public interface OnPlayerPartyLeaveListener extends PlayerListener
{
	void onPartyLeave(final Player p0);
}
