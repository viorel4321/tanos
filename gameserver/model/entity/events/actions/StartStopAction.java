package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class StartStopAction implements EventAction
{
	public static final String EVENT = "event";
	private final String _name;
	private final boolean _start;

	public StartStopAction(final String name, final boolean start)
	{
		_name = name;
		_start = start;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		event.action(_name, _start);
	}
}
