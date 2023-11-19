package l2s.gameserver.listener.events.GameObject;

import l2s.gameserver.listener.events.DefaultMethodInvokeEvent;
import l2s.gameserver.model.GameObject;

public class MethodInvokeEvent extends DefaultMethodInvokeEvent
{
	public MethodInvokeEvent(final String methodName, final GameObject owner, final Object[] args)
	{
		super(methodName, owner, args);
	}

	public GameObject getObject()
	{
		return (GameObject) getOwner();
	}
}
