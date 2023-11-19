package l2s.gameserver.templates.spawn;

import l2s.commons.time.cron.SchedulingPattern;

import java.util.ArrayList;
import java.util.List;

public class SpawnTemplate
{
	private String _name;
	private final PeriodOfDay _periodOfDay;
	private final int _count;
	private final int _respawn;
	private final int _respawnRandom;
	private final SchedulingPattern _respawnPattern;

	private final List<SpawnNpcInfo> _npcList = new ArrayList<SpawnNpcInfo>(1);
	private final List<SpawnRange> _spawnRangeList = new ArrayList<SpawnRange>(1);

	public SpawnTemplate(String name, PeriodOfDay periodOfDay, int count, int respawn, int respawnRandom, String respawnPattern)
	{
		_name = name;
		_periodOfDay = periodOfDay;
		_count = count;
		_respawn = respawn;
		_respawnRandom = respawnRandom;
		_respawnPattern = respawnPattern == null || respawnPattern.isEmpty() ? null : new SchedulingPattern(respawnPattern);
	}

	public void addSpawnRange(final SpawnRange range)
	{
		_spawnRangeList.add(range);
	}

	public SpawnRange getSpawnRange(final int index)
	{
		return _spawnRangeList.get(index);
	}

	public void addNpc(final SpawnNpcInfo info)
	{
		_npcList.add(info);
	}

	public SpawnNpcInfo getNpcId(final int index)
	{
		return _npcList.get(index);
	}

	public int getNpcSize()
	{
		return _npcList.size();
	}

	public int getSpawnRangeSize()
	{
		return _spawnRangeList.size();
	}

	public int getCount()
	{
		return _count;
	}

	public int getRespawn()
	{
		return _respawn;
	}

	public int getRespawnRandom()
	{
		return _respawnRandom;
	}

	public SchedulingPattern getRespawnPattern()
	{
		return _respawnPattern;
	}

	public PeriodOfDay getPeriodOfDay()
	{
		return _periodOfDay;
	}
}
