package l2s.gameserver.model.entity.events;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public abstract class EventOwner implements Serializable
{
	private static final long serialVersionUID = 1L;
	private Set<GlobalEvent> _events;

	public EventOwner()
	{
		_events = new HashSet<GlobalEvent>(2);
	}

	@SuppressWarnings("unchecked")
	public <E extends GlobalEvent> E getEvent(final Class<E> eventClass)
	{
		for(final GlobalEvent e : _events)
		{
			if(e.getClass() == eventClass)
				return (E) e;
			if(eventClass.isAssignableFrom(e.getClass()))
				return (E) e;
		}
		return null;
	}

	public void addEvent(final GlobalEvent event)
	{
		_events.add(event);
	}

	public void removeEvent(final GlobalEvent event)
	{
		_events.remove(event);
	}

	public Set<GlobalEvent> getEvents()
	{
		return _events;
	}
}
