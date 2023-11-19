package l2s.gameserver.listener.actor;

import l2s.gameserver.listener.PlayerListener;
import l2s.gameserver.model.Player;

public interface OnPlayerPartyInviteListener extends PlayerListener
{
	void onPartyInvite(final Player p0);
}
