package l2s.gameserver.utils;

import l2s.gameserver.listener.MethodInvokeListener;
import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.scripts.Functions;

public class SchedulableEvent implements MethodInvokeListener
{
	public String _class;
	public String _method;
	public Integer _delay;
	public String[] _args;

	public SchedulableEvent(final String clas, final String method, final String[] args, final Integer delay)
	{
		_class = clas;
		_method = method;
		_delay = delay;
		_args = args;
	}

	@Override
	public boolean accept(final MethodEvent event)
	{
		return true;
	}

	@Override
	public void methodInvoked(final MethodEvent e)
	{
		Functions.executeTask((GameObject) e.getOwner(), _class, _method, new Object[] { _args }, _delay);
	}
}
