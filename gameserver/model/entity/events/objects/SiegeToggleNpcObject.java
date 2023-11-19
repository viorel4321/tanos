package l2s.gameserver.model.entity.events.objects;

import java.util.Set;

import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.SiegeToggleNpcInstance;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.utils.Location;

public class SiegeToggleNpcObject implements SpawnableObject
{
	private SiegeToggleNpcInstance _toggleNpc;
	private Location _location;

	public SiegeToggleNpcObject(final int id, final int fakeNpcId, final Location loc, final int hp, final Set<String> set)
	{
		_location = loc;
		(_toggleNpc = (SiegeToggleNpcInstance) NpcTable.getTemplate(id).getNewInstance()).initFake(fakeNpcId);
		_toggleNpc.setMaxHp(hp);
		_toggleNpc.setZoneList(set);
	}

	@Override
	public void spawnObject(final GlobalEvent event)
	{
		_toggleNpc.decayFake();
		if(event.isInProgress())
			_toggleNpc.addEvent(event);
		else
			_toggleNpc.removeEvent(event);
		_toggleNpc.setCurrentHp(_toggleNpc.getMaxHp(), false);
		_toggleNpc.spawnMe(_location);
	}

	@Override
	public void despawnObject(final GlobalEvent event)
	{
		_toggleNpc.removeEvent(event);
		_toggleNpc.decayFake();
		_toggleNpc.decayMe();
	}

	@Override
	public void refreshObject(final GlobalEvent event)
	{}

	public SiegeToggleNpcInstance getToggleNpc()
	{
		return _toggleNpc;
	}

	public boolean isAlive()
	{
		return _toggleNpc.isVisible();
	}
}
