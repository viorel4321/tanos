package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class TeleportPlayersAction implements EventAction
{
	private String _name;

	public TeleportPlayersAction(final String name)
	{
		_name = name;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		event.teleportPlayers(_name);
	}
}
