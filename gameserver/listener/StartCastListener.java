package l2s.gameserver.listener;

import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;

public abstract class StartCastListener implements MethodInvokeListener, MethodCollection
{
	@Override
	public final void methodInvoked(final MethodEvent e)
	{
		final Object[] args = e.getArgs();
		onCastStart((Skill) args[0], (Creature) args[1], (boolean) args[2]);
	}

	@Override
	public final boolean accept(final MethodEvent event)
	{
		return event.getMethodName().equals("Creature.doCast");
	}

	public abstract void onCastStart(final Skill p0, final Creature p1, final boolean p2);
}
