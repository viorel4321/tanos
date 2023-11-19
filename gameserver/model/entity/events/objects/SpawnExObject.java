package l2s.gameserver.model.entity.events.objects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.NpcInstance;

public class SpawnExObject implements SpawnableObject
{
	private static final Logger _log;
	private final List<Spawner> _spawns;
	private boolean _spawned;
	private String _name;

	public SpawnExObject(final String name)
	{
		_name = name;
		_spawns = SpawnManager.getInstance().getSpawners(_name);
		if(_spawns.isEmpty())
			SpawnExObject._log.info("SpawnExObject: not found spawn group: " + name);
	}

	@Override
	public void spawnObject(final GlobalEvent event)
	{
		if(_spawned)
			SpawnExObject._log.info("SpawnExObject: can't spawn twice: " + _name + "; event: " + event);
		else
		{
			for(final Spawner spawn : _spawns)
			{
				if(event.isInProgress())
					spawn.addEvent(event);
				else
					spawn.removeEvent(event);
				spawn.init();
			}
			_spawned = true;
		}
	}

	@Override
	public void despawnObject(final GlobalEvent event)
	{
		if(!_spawned)
			return;
		_spawned = false;
		for(final Spawner spawn : _spawns)
		{
			spawn.removeEvent(event);
			spawn.deleteAll();
		}
	}

	@Override
	public void refreshObject(final GlobalEvent event)
	{
		for(final NpcInstance npc : getAllSpawned())
			if(event.isInProgress())
				npc.addEvent(event);
			else
				npc.removeEvent(event);
	}

	public List<Spawner> getSpawns()
	{
		return _spawns;
	}

	public List<NpcInstance> getAllSpawned()
	{
		final List<NpcInstance> npcs = new ArrayList<NpcInstance>();
		for(final Spawner spawn : _spawns)
			npcs.addAll(spawn.getAllSpawned());
		return npcs.isEmpty() ? Collections.emptyList() : npcs;
	}

	public NpcInstance getFirstSpawned()
	{
		final List<NpcInstance> npcs = getAllSpawned();
		return npcs.size() > 0 ? npcs.get(0) : null;
	}

	public boolean isSpawned()
	{
		return _spawned;
	}

	static
	{
		_log = LoggerFactory.getLogger(SpawnExObject.class);
	}
}
