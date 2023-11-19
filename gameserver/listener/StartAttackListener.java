package l2s.gameserver.listener;

import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.model.Creature;

public abstract class StartAttackListener implements MethodInvokeListener, MethodCollection
{
	@Override
	public final void methodInvoked(final MethodEvent e)
	{
		onAttackStart((Creature) e.getArgs()[0], (Creature) e.getArgs()[1]);
	}

	@Override
	public final boolean accept(final MethodEvent event)
	{
		return event.getMethodName().equals("Creature.doAttack");
	}

	public abstract void onAttackStart(final Creature p0, final Creature p1);
}
