package l2s.gameserver.listener.events.GameObject;

import java.util.Collection;

import l2s.gameserver.listener.events.PropertyEvent;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Territory;

public class TerritoryChangeEvent implements PropertyEvent
{
	private final Collection<Territory> enter;
	private final Collection<Territory> exit;
	private final GameObject object;

	public TerritoryChangeEvent(final Collection<Territory> enter, final Collection<Territory> exit, final GameObject object)
	{
		this.enter = enter;
		this.exit = exit;
		this.object = object;
	}

	@Override
	public GameObject getObject()
	{
		return object;
	}

	@Override
	public Collection<Territory> getOldValue()
	{
		return exit;
	}

	@Override
	public Collection<Territory> getNewValue()
	{
		return enter;
	}

	@Override
	public String getProperty()
	{
		return "GameObject.TerritoryChangeEvent";
	}
}
