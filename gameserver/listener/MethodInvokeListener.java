package l2s.gameserver.listener;

import l2s.gameserver.listener.events.MethodEvent;

public interface MethodInvokeListener
{
	void methodInvoked(final MethodEvent p0);

	boolean accept(final MethodEvent p0);
}
