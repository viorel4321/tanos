package l2s.gameserver.model.quest;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.NpcInstance;

public class QuestTimer
{
	private boolean _isActive;
	private String _name;
	private NpcInstance _npc;
	private long _time;
	private int _ownerObjectId;
	private Quest _quest;
	private ScheduledFuture<?> _schedular;

	public QuestTimer(final Quest quest, final String name, final long time, final NpcInstance npc, final Player player)
	{
		_isActive = true;
		_name = name;
		_quest = quest;
		_ownerObjectId = player == null ? 0 : player.getObjectId();
		_npc = npc;
		_time = time;
		_schedular = ThreadPoolManager.getInstance().schedule(new ScheduleTimerTask(), time);
	}

	public void cancel()
	{
		_isActive = false;
		if(_schedular != null)
		{
			_time = _schedular.getDelay(TimeUnit.SECONDS);
			_schedular.cancel(false);
		}
		getQuest().removeQuestTimer(this);
	}

	public final boolean isActive()
	{
		return _isActive;
	}

	public final String getName()
	{
		return _name;
	}

	public final NpcInstance getNpc()
	{
		return _npc;
	}

	public final Player getPlayer()
	{
		return GameObjectsStorage.getPlayer(_ownerObjectId);
	}

	public final Quest getQuest()
	{
		return _quest;
	}

	public boolean isMatch(final Quest quest, final String name, final Player player)
	{
		return quest != null && name != null && quest == getQuest() && name.equalsIgnoreCase(getName()) && player == getPlayer();
	}

	@Override
	public final String toString()
	{
		return _name;
	}

	public long getTime()
	{
		return _time;
	}

	public class ScheduleTimerTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isActive())
				return;
			final Player pl = getPlayer();
			if(pl != null && getQuest() != null && getName() != null)
				pl.processQuestEvent(getQuest().getId(), getName(), getNpc());
			cancel();
		}
	}
}
