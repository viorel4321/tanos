package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;

public class EffectTaskManager extends SteppingRunnableQueueManager
{
	private static final long TICK = 250L;
	private static final EffectTaskManager[] _instances;
	private static int randomizer;

	public static final EffectTaskManager getInstance()
	{
		return EffectTaskManager._instances[EffectTaskManager.randomizer++ & EffectTaskManager._instances.length - 1];
	}

	private EffectTaskManager()
	{
		super(250L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, Rnd.get(250L), 250L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			EffectTaskManager.this.purge();
		}, 30000L, 30000L);
	}

	public CharSequence getStats(final int num)
	{
		return EffectTaskManager._instances[num].getStats();
	}

	static
	{
		_instances = new EffectTaskManager[Config.EFFECT_TASK_MANAGER_COUNT];
		for(int i = 0; i < EffectTaskManager._instances.length; ++i)
			EffectTaskManager._instances[i] = new EffectTaskManager();
		EffectTaskManager.randomizer = 0;
	}
}
