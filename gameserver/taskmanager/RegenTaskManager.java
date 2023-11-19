package l2s.gameserver.taskmanager;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.gameserver.ThreadPoolManager;

public class RegenTaskManager extends SteppingRunnableQueueManager
{
	private static final RegenTaskManager _instance;

	public static final RegenTaskManager getInstance()
	{
		return RegenTaskManager._instance;
	}

	private RegenTaskManager()
	{
		super(1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			RegenTaskManager.this.purge();
		}, 10000L, 10000L);
	}

	static
	{
		_instance = new RegenTaskManager();
	}
}
