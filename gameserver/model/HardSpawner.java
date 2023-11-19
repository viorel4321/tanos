package l2s.gameserver.model;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.templates.spawn.SpawnNpcInfo;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.templates.spawn.SpawnTemplate;

public class HardSpawner extends Spawner
{
	private static final long serialVersionUID = -3566741484655685267L;
	private final SpawnTemplate _template;
	private int _pointIndex;
	private int _npcIndex;
	private List<NpcInstance> _reSpawned;

	public HardSpawner(final SpawnTemplate template)
	{
		_reSpawned = new CopyOnWriteArrayList<NpcInstance>();
		_template = template;
		_spawned = new CopyOnWriteArrayList<NpcInstance>();
	}

	@Override
	public void decreaseCount(final NpcInstance oldNpc)
	{
		oldNpc.setSpawn2(null);
		oldNpc.deleteMe();
		_spawned.remove(oldNpc);
		final SpawnNpcInfo npcInfo = getNextNpcInfo();
		final NpcInstance npc = npcInfo.getTemplate().getNewInstance();
		npc.setSpawn2(this);
		_reSpawned.add(npc);
		decreaseCount0(npcInfo.getTemplate(), npc, oldNpc.getDeadTime());
	}

	@Override
	public NpcInstance doSpawn(final boolean spawn)
	{
		final SpawnNpcInfo npcInfo = getNextNpcInfo();
		return doSpawn0(npcInfo.getTemplate(), spawn);
	}

	@Override
	protected NpcInstance initNpc(final NpcInstance mob, final boolean spawn)
	{
		_reSpawned.remove(mob);
		final SpawnRange range = _template.getSpawnRange(getNextRangeId());
		mob.setSpawnRange(range);
		return initNpc0(mob, range.getRandomLoc(mob.getGeoIndex()), spawn);
	}

	@Override
	public int getCurrentNpcId()
	{
		final SpawnNpcInfo npcInfo = _template.getNpcId(_npcIndex);
		return npcInfo.getTemplate().npcId;
	}

	@Override
	public SpawnRange getCurrentSpawnRange()
	{
		return _template.getSpawnRange(_pointIndex);
	}

	@Override
	public void respawnNpc(final NpcInstance oldNpc)
	{
		initNpc(oldNpc, true);
	}

	@Override
	public void deleteAll()
	{
		super.deleteAll();
		for(final NpcInstance npc : _reSpawned)
		{
			npc.setSpawn(null);
			npc.deleteMe();
		}
		_reSpawned.clear();
	}

	private synchronized SpawnNpcInfo getNextNpcInfo()
	{
		final int old = _npcIndex++;
		if(_npcIndex >= _template.getNpcSize())
			_npcIndex = 0;
		final SpawnNpcInfo npcInfo = _template.getNpcId(old);
		if(npcInfo.getMax() > 0)
		{
			int count = 0;
			for(final NpcInstance npc : _spawned)
				if(npc.getNpcId() == npcInfo.getTemplate().getId())
					++count;
			if(count >= npcInfo.getMax())
				return getNextNpcInfo();
		}
		return npcInfo;
	}

	private synchronized int getNextRangeId()
	{
		final int old = _pointIndex++;
		if(_pointIndex >= _template.getSpawnRangeSize())
			_pointIndex = 0;
		return old;
	}

	@Override
	public HardSpawner clone()
	{
		final HardSpawner spawnDat = new HardSpawner(_template);
		spawnDat.setAmount(_maximumCount);
		spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
		spawnDat.setRespawnPattern(getRespawnPattern());
		spawnDat.setRespawnTime(0);
		return spawnDat;
	}
}
