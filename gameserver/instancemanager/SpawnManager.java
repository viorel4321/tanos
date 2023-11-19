package l2s.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.SpawnHolder;
import l2s.gameserver.model.HardSpawner;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.PeriodOfDay;
import l2s.gameserver.templates.spawn.SpawnTemplate;

public class SpawnManager
{
	private static final Logger _log;
	private static SpawnManager _instance;
	private Map<String, List<Spawner>> _spawns;
	public static boolean retardation;
	public static boolean completed;

	public static SpawnManager getInstance()
	{
		return SpawnManager._instance;
	}

	private SpawnManager()
	{
		_spawns = new ConcurrentHashMap<String, List<Spawner>>();
		for(final Map.Entry<String, List<SpawnTemplate>> entry : SpawnHolder.getInstance().getSpawns().entrySet())
			fillSpawn(entry.getKey(), entry.getValue());
	}

	public List<Spawner> fillSpawn(final String group, final List<SpawnTemplate> templateList)
	{
		if(Config.DONTLOADSPAWN)
			return Collections.emptyList();
		List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			_spawns.put(group, spawnerList = new ArrayList<Spawner>(templateList.size()));
		for(final SpawnTemplate template : templateList)
		{
			final HardSpawner spawner = new HardSpawner(template);
			spawnerList.add(spawner);
			final NpcTemplate npcTemplate = NpcTable.getTemplate(spawner.getCurrentNpcId());

			if(Config.RATE_MOB_SPAWN > 1 && npcTemplate.isInstanceOf(MonsterInstance.class) && !npcTemplate.isRaid && npcTemplate.level >= Config.RATE_MOB_SPAWN_MIN_LEVEL && npcTemplate.level <= Config.RATE_MOB_SPAWN_MAX_LEVEL)
				spawner.setAmount(template.getCount() * Config.RATE_MOB_SPAWN);
			else
				spawner.setAmount(template.getCount());

			spawner.setRespawnDelay(template.getRespawn(), template.getRespawnRandom());
			spawner.setRespawnPattern(template.getRespawnPattern());
			spawner.setRespawnTime(0);
		}
		return spawnerList;
	}

	public void spawnAll()
	{
		spawn(PeriodOfDay.NONE.name());
	}

	public void spawn(final String group)
	{
		final List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			return;
		int npcSpawnCount = 0;
		for(final Spawner spawner : spawnerList)
		{
			npcSpawnCount += spawner.init();
			if(SpawnManager.retardation && Config.DELAY_SPAWN_NPC > 0L)
				try
				{
					Thread.sleep(Config.DELAY_SPAWN_NPC);
				}
				catch(InterruptedException ex)
				{}
		}
		SpawnManager._log.info("SpawnManager: spawned " + npcSpawnCount + " npc; spawns: " + spawnerList.size() + "; group: " + group);
	}

	public void despawn(final String group)
	{
		final List<Spawner> spawnerList = _spawns.get(group);
		if(spawnerList == null)
			return;
		for(final Spawner spawner : spawnerList)
			spawner.deleteAll();
	}

	public List<Spawner> getSpawners(final String group)
	{
		final List<Spawner> list = _spawns.get(group);
		return list == null ? Collections.emptyList() : list;
	}

	public void reloadAll()
	{
		for(final List<Spawner> spawnerList : _spawns.values())
			for(final Spawner spawner : spawnerList)
				spawner.deleteAll();
		spawnAll();
	}

	static
	{
		_log = LoggerFactory.getLogger(SpawnManager.class);
		SpawnManager._instance = new SpawnManager();
		SpawnManager.retardation = false;
		SpawnManager.completed = false;
	}
}
