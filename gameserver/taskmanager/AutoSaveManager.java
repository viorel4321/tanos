package l2s.gameserver.taskmanager;

import java.util.concurrent.Future;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;

public class AutoSaveManager extends SteppingRunnableQueueManager
{
	private static final AutoSaveManager _instance;

	public static final AutoSaveManager getInstance()
	{
		return AutoSaveManager._instance;
	}

	private AutoSaveManager()
	{
		super(10000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 10000L, 10000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			AutoSaveManager.this.purge();
		}, 60000L, 60000L);
	}

	public Future<?> addAutoSaveTask(final Player player)
	{
		final long delay = Rnd.get(180, 360) * 1000L;
		return scheduleAtFixedRate(() -> {
			if(!player.isOnline() || player.isLogoutStarted())
				return;
			player.store(true);
		}, delay, delay);
	}

	static
	{
		_instance = new AutoSaveManager();
	}
}
