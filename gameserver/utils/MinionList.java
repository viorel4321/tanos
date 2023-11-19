package l2s.gameserver.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.commons.util.Rnd;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.MinionData;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.tables.NpcTable;

public class MinionList
{
	private final ConcurrentLinkedQueue<MonsterInstance> _minionReferences;
	private final MonsterInstance _master;

	public MinionList(final MonsterInstance master)
	{
		_minionReferences = new ConcurrentLinkedQueue<MonsterInstance>();
		_master = master;
	}

	public int countSpawnedMinions()
	{
		return _minionReferences.size();
	}

	public boolean hasAliveMinions()
	{
		synchronized (_minionReferences)
		{
			for(final MonsterInstance m : getSpawnedMinions())
				if(m.isVisible() && !m.isDead())
					return true;
		}
		return false;
	}

	public boolean hasMinions()
	{
		return _minionReferences.size() > 0;
	}

	public List<MonsterInstance> getAliveMinions()
	{
		final List<MonsterInstance> result = new ArrayList<MonsterInstance>(_minionReferences.size());
		synchronized (_minionReferences)
		{
			for(final MonsterInstance m : getSpawnedMinions())
				if(m.isVisible() && !m.isDead())
					result.add(m);
		}
		return result;
	}

	public ConcurrentLinkedQueue<MonsterInstance> getSpawnedMinions()
	{
		return _minionReferences;
	}

	public void addSpawnedMinion(final MonsterInstance minion)
	{
		synchronized (_minionReferences)
		{
			_minionReferences.add(minion);
		}
	}

	public void removeSpawnedMinion(final MonsterInstance minion)
	{
		synchronized (_minionReferences)
		{
			_minionReferences.remove(minion);
		}
	}

	public void maintainMinions()
	{
		final List<MinionData> minions = _master.getTemplate().getMinionData();
		synchronized (_minionReferences)
		{
			for(final MinionData minion : minions)
			{
				int minionCount = Rnd.get(minion.getAmountMin(), minion.getAmountMax());
				final int minionId = minion.getMinionId();
				for(final MonsterInstance m : _minionReferences)
					if(m.getNpcId() == minionId)
						--minionCount;
				for(int i = 0; i < minionCount; ++i)
					spawnSingleMinion(minionId);
			}
		}
	}

	public void maintainLonelyMinions()
	{
		synchronized (_minionReferences)
		{
			for(final MonsterInstance minion : getSpawnedMinions())
				if(!minion.isDead())
				{
					removeSpawnedMinion(minion);
					minion.deleteMe();
				}
		}
	}

	private void spawnSingleMinion(final int minionid)
	{
		final MonsterInstance monster = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcTable.getTemplate(minionid));
		monster.setLeader(_master);
		monster.setHeading(_master.getHeading());
		addSpawnedMinion(monster);
		monster.spawnMe(_master.getMinionPosition());
	}

	public void spawnSingleMinionSync(final int minionid)
	{
		synchronized (_minionReferences)
		{
			spawnSingleMinion(minionid);
		}
	}
}
