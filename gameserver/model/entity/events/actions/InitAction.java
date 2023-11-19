package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class InitAction implements EventAction
{
	private String _name;

	public InitAction(final String name)
	{
		_name = name;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		event.initAction(_name);
	}
}
