package l2s.gameserver.data.xml.holder;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.GlobalEvent;

public final class EventHolder extends AbstractHolder
{
	private static final EventHolder _instance = new EventHolder();

	private final IntObjectMap<GlobalEvent> _events;

	public EventHolder()
	{
		_events = new TreeIntObjectMap<GlobalEvent>();
	}

	public static EventHolder getInstance()
	{
		return _instance;
	}

	public void addEvent(final EventType type, final GlobalEvent event)
	{
		_events.put(type.step() + event.getId(), event);
	}

	@SuppressWarnings("unchecked")
	public <E extends GlobalEvent> E getEvent(final EventType type, final int id)
	{
		return (E) _events.get(type.step() + id);
	}

	public void findEvent(final Player player)
	{
		for(final GlobalEvent event : _events.valueCollection())
			if(event.isParticle(player))
				player.addEvent(event);
	}

	public void callInit()
	{
		for(final GlobalEvent event : _events.valueCollection())
			event.initEvent();
	}

	@Override
	public int size()
	{
		return _events.size();
	}

	@Override
	public void clear()
	{
		_events.clear();
	}
}
