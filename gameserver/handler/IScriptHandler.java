package l2s.gameserver.handler;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public interface IScriptHandler
{
	void onDie(final Creature p0, final Creature p1);

	Location onEscape(final Player p0);
}
