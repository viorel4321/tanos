package l2s.gameserver.listener;

import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.listener.events.Zone.ZoneEnterLeaveEvent;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Zone;

public abstract class ZoneEnterLeaveListener implements MethodInvokeListener, MethodCollection
{
	@Override
	public final void methodInvoked(final MethodEvent e)
	{
		final ZoneEnterLeaveEvent event = (ZoneEnterLeaveEvent) e;
		final Zone owner = event.getOwner();
		final GameObject actor = event.getArgs()[0];
		if(e.getMethodName().equals("Zone.onZoneEnter"))
			objectEntered(owner, actor);
		else
			objectLeaved(owner, actor);
	}

	@Override
	public final boolean accept(final MethodEvent event)
	{
		final String method = event.getMethodName();
		return event instanceof ZoneEnterLeaveEvent && (method.equals("Zone.onZoneEnter") || method.equals("Zone.onZoneLeave"));
	}

	public abstract void objectEntered(final Zone p0, final GameObject p1);

	public abstract void objectLeaved(final Zone p0, final GameObject p1);
}
