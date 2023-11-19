package l2s.gameserver.taskmanager.tasks;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Hero;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.taskmanager.Task;
import l2s.gameserver.taskmanager.TaskManager;
import l2s.gameserver.taskmanager.TaskTypes;

public class TaskEndHero extends Task
{
	private static final String NAME = "TaskEndHero";

	@Override
	public String getName()
	{
		return "TaskEndHero";
	}

	@Override
	public void onTimeElapsed(final TaskManager.ExecutedTask task)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			if(player.getVar("HeroStatus") != null && Long.parseLong(player.getVar("HeroStatus")) < System.currentTimeMillis())
			{
				player.setHero(false);
				Hero.removeSkills(player);
				for(final ItemInstance item : player.getInventory().getItems())
					if(item != null && item.isHeroWeapon())
						player.getInventory().destroyItem(item);
				player.updatePledgeClass();
				player.broadcastUserInfo(true);
				player.unsetVar("HeroStatus");
			}
	}

	@Override
	public void initializate()
	{
		TaskManager.addUniqueTask("TaskEndHero", TaskTypes.TYPE_GLOBAL_TASK, "1", "06:30:00", "");
	}
}
