package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.taskmanager.Task;
import l2s.gameserver.taskmanager.TaskManager;
import l2s.gameserver.taskmanager.TaskTypes;

public class TaskRecom extends Task
{
	private static final String NAME = "sp_recommendations";

	@Override
	public String getName()
	{
		return "sp_recommendations";
	}

	@Override
	public void onTimeElapsed(final TaskManager.ExecutedTask task)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			player.restartRecom();
	}

	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask("sp_recommendations", TaskTypes.TYPE_GLOBAL_TASK, "1", "13:00:00", "");
	}
}
