package l2s.gameserver.model.instances;

import java.util.HashSet;
import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.templates.npc.NpcTemplate;

public class ControlTowerInstance extends SiegeToggleNpcInstance
{
	private Set<Spawner> _spawnList;

	public ControlTowerInstance(final int objectId, final NpcTemplate template)
	{
		super(objectId, template);
		_spawnList = new HashSet<Spawner>();
	}

	@Override
	public void onDeathImpl(final Creature killer)
	{
		for(final Spawner spawn : _spawnList)
			spawn.stopRespawn();
		_spawnList.clear();
	}

	@Override
	public void register(final Spawner spawn)
	{
		_spawnList.add(spawn);
	}

	@Override
	public boolean isDmg()
	{
		return true;
	}
}
