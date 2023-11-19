package l2s.gameserver.taskmanager;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.lang.reference.HardReference;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.utils.Util;

public class DecayTaskManager
{
	private DecayTask[] _decayTasks;
	private int _decayTasksSize;
	private final Object decayTasks_lock;
	private static DecayTaskManager _instance;

	private DecayTaskManager()
	{
		_decayTasks = new DecayTask[500];
		_decayTasksSize = 0;
		decayTasks_lock = new Object();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new DecayScheduler(), 2000L, 2000L);
	}

	public static DecayTaskManager getInstance()
	{
		if(DecayTaskManager._instance == null)
			DecayTaskManager._instance = new DecayTaskManager();
		return DecayTaskManager._instance;
	}

	public void addDecayTask(final Creature actor)
	{
		if(actor.isFlying())
			this.addDecayTask(actor, 4500L);
		else
			this.addDecayTask(actor, 8500L);
	}

	public void addDecayTask(final Creature actor, final long interval)
	{
		removeObject(actor);
		addObject(new DecayTask(actor, System.currentTimeMillis() + interval));
	}

	public void cancelDecayTask(final Creature actor)
	{
		removeObject(actor);
	}

	@Override
	public String toString()
	{
		final StringBuffer sb = new StringBuffer("============= DecayTask Manager Report ============\n\r");
		sb.append("Tasks count: ").append(_decayTasksSize).append("\n\r");
		sb.append("Tasks dump:\n\r");
		final long current = System.currentTimeMillis();
		for(final DecayTask container : _decayTasks)
		{
			sb.append("Class/Name: ").append(container.getClass().getSimpleName()).append('/').append(container.getActor());
			sb.append(" decay timer: ").append(Util.formatTime(container.endtime - current)).append("\n\r");
		}
		return sb.toString();
	}

	private void addObject(final DecayTask decay)
	{
		synchronized (decayTasks_lock)
		{
			if(_decayTasksSize >= _decayTasks.length)
			{
				final DecayTask[] temp = new DecayTask[_decayTasks.length * 2];
				for(int i = 0; i < _decayTasksSize; ++i)
					temp[i] = _decayTasks[i];
				_decayTasks = temp;
			}
			_decayTasks[_decayTasksSize] = decay;
			++_decayTasksSize;
		}
	}

	private void removeObject(final Creature actor)
	{
		synchronized (decayTasks_lock)
		{
			if(_decayTasksSize > 1)
			{
				int k = -1;
				for(int i = 0; i < _decayTasksSize; ++i)
					if(_decayTasks[i].getActor() == actor)
						k = i;
				if(k > -1)
				{
					_decayTasks[k] = _decayTasks[_decayTasksSize - 1];
					_decayTasks[_decayTasksSize - 1] = null;
					--_decayTasksSize;
				}
			}
			else if(_decayTasksSize == 1 && _decayTasks[0].getActor() == actor)
			{
				_decayTasks[0] = null;
				_decayTasksSize = 0;
			}
		}
	}

	public class DecayScheduler implements Runnable
	{
		@Override
		public void run()
		{
			if(_decayTasksSize > 0)
				try
				{
					final List<Creature> works = new ArrayList<Creature>();
					synchronized (decayTasks_lock)
					{
						final long current = System.currentTimeMillis();
						final int size = _decayTasksSize;
						for(int i = size - 1; i >= 0; --i)
							try
							{
								final DecayTask container = _decayTasks[i];
								if(container != null && container.endtime > 0L && current > container.endtime)
								{
									final Creature actor = container.getActor();
									if(actor != null)
										works.add(actor);
									container.endtime = -1L;
								}
								if(container == null || container.getActor() == null || container.endtime < 0L)
								{
									if(i == _decayTasksSize - 1)
										_decayTasks[i] = null;
									else
									{
										_decayTasks[i] = _decayTasks[_decayTasksSize - 1];
										_decayTasks[_decayTasksSize - 1] = null;
									}
									if(_decayTasksSize > 0)
										_decayTasksSize--;
								}
							}
							catch(Exception e)
							{
								e.printStackTrace();
							}
					}
					for(final Creature work : works)
						work.onDecay();
				}
				catch(Exception e2)
				{
					e2.printStackTrace();
				}
		}
	}

	private class DecayTask
	{
		private final HardReference<? extends Creature> _creatureRef;
		public long endtime;

		public DecayTask(Creature creature, final long delay)
		{
			_creatureRef = creature.getRef();
			endtime = delay;
		}

		public Creature getActor()
		{
			return _creatureRef.get();
		}
	}
}
