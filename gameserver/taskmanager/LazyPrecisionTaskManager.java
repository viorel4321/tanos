package l2s.gameserver.taskmanager;

import java.util.concurrent.Future;

import l2s.commons.threading.SteppingRunnableQueueManager;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

public class LazyPrecisionTaskManager extends SteppingRunnableQueueManager
{
	private static final LazyPrecisionTaskManager _instance;

	public static final LazyPrecisionTaskManager getInstance()
	{
		return LazyPrecisionTaskManager._instance;
	}

	private LazyPrecisionTaskManager()
	{
		super(1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			LazyPrecisionTaskManager.this.purge();
		}, 60000L, 60000L);
	}

	public Future<?> addPCCafePointsTask(final Player player)
	{
		final long delay = Config.PCBANG_POINTS_DELAY * 60000L;
		return scheduleAtFixedRate(() -> {
			if(player.isInOfflineMode() || player.getLevel() < Config.PCBANG_POINTS_MIN_LVL)
				return;
			player.addPcBangPoints(Config.PCBANG_POINTS_BONUS, Config.PCBANG_POINTS_BONUS_DOUBLE_CHANCE > 0.0 && Rnd.chance(Config.PCBANG_POINTS_BONUS_DOUBLE_CHANCE));
		}, delay, delay);
	}

	public Future<?> addNpcAnimationTask(final NpcInstance npc)
	{
		return scheduleAtFixedRate(() -> {
			if(npc.isVisible() && !npc.isActionsDisabled() && !npc.isMoving && !npc.isInCombat())
				npc.onRandomAnimation();
		}, 1000L, Rnd.get(Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION) * 1000L);
	}

	static
	{
		_instance = new LazyPrecisionTaskManager();
	}
}
