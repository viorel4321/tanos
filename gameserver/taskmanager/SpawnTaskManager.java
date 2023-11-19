package l2s.gameserver.taskmanager;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.Spawner;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpawnTaskManager
{
	private static final Logger LOGGER = LoggerFactory.getLogger(SpawnTaskManager.class);
	private SpawnTask[] _spawnTasks;
	private int _spawnTasksSize;
	private final Object spawnTasks_lock;
	private static SpawnTaskManager _instance;

	public SpawnTaskManager()
	{
		_spawnTasks = new SpawnTask[500];
		_spawnTasksSize = 0;
		spawnTasks_lock = new Object();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new SpawnScheduler(), 2000L, 2000L);
	}

	public static SpawnTaskManager getInstance()
	{
		if(SpawnTaskManager._instance == null)
			SpawnTaskManager._instance = new SpawnTaskManager();
		return SpawnTaskManager._instance;
	}

	public void addSpawnTask(final NpcInstance actor, final long interval)
	{
		removeObject(actor);
		addObject(new SpawnTask(actor, System.currentTimeMillis() + interval));
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer("============= SpawnTask Manager Report ============\n\r");
		sb.append("Tasks count: ").append(_spawnTasksSize).append("\n\r");
		sb.append("Tasks dump:\n\r");
		final long current = System.currentTimeMillis();
		for(final SpawnTask container : _spawnTasks)
		{
			sb.append("Class/Name: ").append(container.getClass().getSimpleName()).append('/').append(container.getActor());
			sb.append(" spawn timer: ").append(Util.formatTime((int) (container.endtime - current))).append("\n\r");
		}
		return sb.toString();
	}

	private void addObject(final SpawnTask decay)
	{
		synchronized (spawnTasks_lock)
		{
			if(_spawnTasksSize >= _spawnTasks.length)
			{
				final SpawnTask[] temp = new SpawnTask[_spawnTasks.length * 2];
				for(int i = 0; i < _spawnTasksSize; ++i)
					temp[i] = _spawnTasks[i];
				_spawnTasks = temp;
			}
			_spawnTasks[_spawnTasksSize] = decay;
			++_spawnTasksSize;
		}
	}

	private void removeObject(final NpcInstance actor)
	{
		synchronized (spawnTasks_lock)
		{
			if(_spawnTasksSize > 1)
			{
				int k = -1;
				for(int i = 0; i < _spawnTasksSize; ++i)
					if(_spawnTasks[i].getActor() == actor)
						k = i;
				if(k > -1)
				{
					_spawnTasks[k] = _spawnTasks[_spawnTasksSize - 1];
					_spawnTasks[_spawnTasksSize - 1] = null;
					--_spawnTasksSize;
				}
			}
			else if(_spawnTasksSize == 1 && _spawnTasks[0].getActor() == actor)
			{
				_spawnTasks[0] = null;
				_spawnTasksSize = 0;
			}
		}
	}

	public class SpawnScheduler implements Runnable
	{
		@Override
		public void run()
		{
			if(_spawnTasksSize > 0)
				try
				{
					final List<NpcInstance> works = new ArrayList<NpcInstance>();
					synchronized (spawnTasks_lock)
					{
						final long current = System.currentTimeMillis();
						final int size = _spawnTasksSize;
						for(int i = size - 1; i >= 0; --i)
							try
							{
								final SpawnTask container = _spawnTasks[i];
								if(container != null && container.endtime > 0L && current > container.endtime)
								{
									final NpcInstance actor = container.getActor();
									if(actor != null && (actor.getSpawn() != null || actor.getSpawn2() != null))
										works.add(actor);
									container.endtime = -1L;
								}
								if(container == null || container.getActor() == null || container.endtime < 0L)
								{
									if(i == _spawnTasksSize - 1)
										_spawnTasks[i] = null;
									else
									{
										_spawnTasks[i] = _spawnTasks[_spawnTasksSize - 1];
										_spawnTasks[_spawnTasksSize - 1] = null;
									}
									if(_spawnTasksSize > 0)
										_spawnTasksSize--;
								}
							}
							catch(Exception e)
							{
								LOGGER.error("", e);
							}
					}
					for(final NpcInstance work : works)
					{
						final Spawn spawn = work.getSpawn();
						if(spawn != null)
						{
							spawn.decreaseScheduledCount();
							if(!spawn.isDoRespawn())
								continue;
							spawn.respawnNpc(work);
						}
						else
						{
							final Spawner spawn2 = work.getSpawn2();
							if(spawn2 == null)
								continue;
							spawn2.decreaseScheduledCount();
							if(!spawn2.isDoRespawn())
								continue;
							spawn2.respawnNpc(work);
						}
					}
				}
				catch(Exception e2)
				{
					LOGGER.error("", e2);
				}
		}
	}

	private class SpawnTask
	{
		private final HardReference<NpcInstance> _npcRef;
		public long endtime;

		SpawnTask(final NpcInstance npc, final long delay)
		{
			_npcRef = npc.getRef();
			endtime = delay;
		}

		public NpcInstance getActor()
		{
			return _npcRef.get();
		}
	}
}
