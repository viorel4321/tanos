package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class SpawnDespawnAction implements EventAction
{
	private final boolean _spawn;
	private final String _name;

	public SpawnDespawnAction(final String name, final boolean spawn)
	{
		_spawn = spawn;
		_name = name;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		event.spawnAction(_name, _spawn);
	}
}
