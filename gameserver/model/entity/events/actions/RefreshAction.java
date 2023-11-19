package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class RefreshAction implements EventAction
{
	private final String _name;

	public RefreshAction(final String name)
	{
		_name = name;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		event.refreshAction(_name);
	}
}
