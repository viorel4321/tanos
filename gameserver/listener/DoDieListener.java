package l2s.gameserver.listener;

import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.model.Creature;

public abstract class DoDieListener implements MethodInvokeListener
{
	@Override
	public final void methodInvoked(final MethodEvent e)
	{
		onDie((Creature) e.getOwner());
	}

	@Override
	public final boolean accept(final MethodEvent event)
	{
		return event.getMethodName().equals("Creature.onDecay");
	}

	public abstract void onDie(final Creature p0);
}
