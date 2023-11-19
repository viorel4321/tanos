package l2s.gameserver.listener.events.Zone;

import l2s.gameserver.listener.events.DefaultMethodInvokeEvent;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Zone;

public class ZoneEnterLeaveEvent extends DefaultMethodInvokeEvent
{
	public ZoneEnterLeaveEvent(final String methodName, final Zone owner, final GameObject[] args)
	{
		super(methodName, owner, args);
	}

	@Override
	public Zone getOwner()
	{
		return (Zone) super.getOwner();
	}

	@Override
	public GameObject[] getArgs()
	{
		return (GameObject[]) super.getArgs();
	}
}
