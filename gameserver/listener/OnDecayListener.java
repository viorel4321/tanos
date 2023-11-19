package l2s.gameserver.listener;

import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.model.Creature;

public abstract class OnDecayListener implements MethodInvokeListener
{
	@Override
	public final void methodInvoked(final MethodEvent e)
	{
		onDecay((Creature) e.getOwner());
	}

	@Override
	public final boolean accept(final MethodEvent event)
	{
		return event.getMethodName().equals("Creature.onDecay");
	}

	public abstract void onDecay(final Creature p0);
}
