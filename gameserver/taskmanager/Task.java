package l2s.gameserver.taskmanager;

import java.util.concurrent.ScheduledFuture;

public abstract class Task
{
	public abstract void initializate();

	public ScheduledFuture<?> launchSpecial(final TaskManager.ExecutedTask instance)
	{
		return null;
	}

	public abstract String getName();

	public abstract void onTimeElapsed(final TaskManager.ExecutedTask p0);

	public void onDestroy()
	{}
}
