package l2s.gameserver.listener.events.GameObject;

import l2s.gameserver.listener.events.DefaultPropertyChangeEvent;
import l2s.gameserver.model.GameObject;

public class PropertyChangeEvent extends DefaultPropertyChangeEvent
{
	public PropertyChangeEvent(final String event, final GameObject actor, final Object oldV, final Object newV)
	{
		super(event, actor, oldV, newV);
	}

	@Override
	public GameObject getObject()
	{
		return (GameObject) super.getObject();
	}
}
