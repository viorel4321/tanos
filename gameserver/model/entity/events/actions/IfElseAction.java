package l2s.gameserver.model.entity.events.actions;

import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class IfElseAction implements EventAction
{
	private String _name;
	private boolean _reverse;
	private List<EventAction> _ifList;
	private List<EventAction> _elseList;

	public IfElseAction(final String name, final boolean reverse)
	{
		_ifList = Collections.emptyList();
		_elseList = Collections.emptyList();
		_name = name;
		_reverse = reverse;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		final List<EventAction> list = (_reverse ? !event.ifVar(_name) : event.ifVar(_name)) ? _ifList : _elseList;
		for(final EventAction action : list)
			action.call(event);
	}

	public void setIfList(final List<EventAction> ifList)
	{
		_ifList = ifList;
	}

	public void setElseList(final List<EventAction> elseList)
	{
		_elseList = elseList;
	}
}
