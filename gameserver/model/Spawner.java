package l2s.gameserver.model;

import java.util.List;

import l2s.commons.time.cron.SchedulingPattern;
import l2s.gameserver.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.entity.events.EventOwner;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.taskmanager.SpawnTaskManager;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.Location;

public abstract class Spawner extends EventOwner implements Cloneable
{
	private static final long serialVersionUID = 6326392189707112339L;
	protected static final Logger _log = LoggerFactory.getLogger(Spawner.class);

	protected static final int MIN_RESPAWN_DELAY = 30;

	protected int _maximumCount;
	protected int _referenceCount;
	protected int _currentCount;
	protected int _scheduledCount;
	protected int _respawnDelay;
	protected int _respawnDelayRandom;
	protected int _nativeRespawnDelay;

	protected SchedulingPattern _respawnPattern;

	protected int _respawnTime;
	protected boolean _doRespawn;
	protected NpcInstance _lastSpawn;
	protected List<NpcInstance> _spawned;

	public void decreaseScheduledCount()
	{
		if(_scheduledCount > 0)
			--_scheduledCount;
	}

	public boolean isDoRespawn()
	{
		return _doRespawn;
	}

	public int getRespawnDelay()
	{
		return _respawnDelay;
	}

	public int getNativeRespawnDelay()
	{
		return _nativeRespawnDelay;
	}

	public int getRespawnDelayRandom()
	{
		return _respawnDelayRandom;
	}

	public int getRespawnDelayWithRnd()
	{
		return _respawnDelayRandom == 0 ? _respawnDelay : Rnd.get(_respawnDelay - _respawnDelayRandom, _respawnDelay);
	}

	public SchedulingPattern getRespawnPattern()
	{
		return _respawnPattern;
	}

	public boolean hasRespawn()
	{
		if(getRespawnDelay() == 0 && getRespawnDelayRandom() == 0 && getRespawnPattern() == null)
			return false;
		return true;
	}

	public int getRespawnTime()
	{
		return _respawnTime;
	}

	public NpcInstance getLastSpawn()
	{
		return _lastSpawn;
	}

	public void setAmount(final int amount)
	{
		if(_referenceCount == 0)
			_referenceCount = amount;
		_maximumCount = amount;
	}

	public void deleteAll()
	{
		stopRespawn();
		for(final NpcInstance npc : _spawned)
			npc.deleteMe();
		_spawned.clear();
		_respawnTime = 0;
		_scheduledCount = 0;
		_currentCount = 0;
	}

	public abstract void decreaseCount(final NpcInstance p0);

	public abstract NpcInstance doSpawn(final boolean p0);

	public abstract void respawnNpc(final NpcInstance p0);

	protected abstract NpcInstance initNpc(final NpcInstance p0, final boolean p1);

	public abstract int getCurrentNpcId();

	public abstract SpawnRange getCurrentSpawnRange();

	public int init()
	{
		while(_currentCount + _scheduledCount < _maximumCount)
			doSpawn(false);
		_doRespawn = true;
		return _currentCount;
	}

	public NpcInstance spawnOne()
	{
		return doSpawn(false);
	}

	public void stopRespawn()
	{
		_doRespawn = false;
	}

	public void startRespawn()
	{
		_doRespawn = true;
	}

	public List<NpcInstance> getAllSpawned()
	{
		return _spawned;
	}

	public NpcInstance getFirstSpawned()
	{
		final List<NpcInstance> npcs = getAllSpawned();
		return npcs.size() > 0 ? npcs.get(0) : null;
	}

	public void setRespawnDelay(final int respawnDelay, final int respawnDelayRandom)
	{
		if(respawnDelay < 0)
			Spawner._log.warn("respawn delay is negative");
		_nativeRespawnDelay = respawnDelay;
		_respawnDelay = respawnDelay;
		_respawnDelayRandom = respawnDelayRandom;
	}

	public void setRespawnDelay(final int respawnDelay)
	{
		this.setRespawnDelay(respawnDelay, 0);
	}

	public void setRespawnPattern(SchedulingPattern pattern)
	{
		_respawnPattern = pattern;
	}

	public void setRespawnTime(final int respawnTime)
	{
		_respawnTime = respawnTime;
	}

	protected NpcInstance doSpawn0(final NpcTemplate template, boolean spawn)
	{
		if(template.isInstanceOf(PetInstance.class))
		{
			++_currentCount;
			return null;
		}
		final NpcInstance tmp = template.getNewInstance();
		if(tmp == null)
			return null;
		if(!spawn)
			spawn = _respawnTime <= System.currentTimeMillis() / 1000L + 30L;
		return initNpc(tmp, spawn);
	}

	protected NpcInstance initNpc0(final NpcInstance mob, final Location newLoc, final boolean spawn)
	{
		mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);
		mob.setSpawn2(this);
		mob.setSpawnedLoc(newLoc);
		mob.setUnderground(GeoEngine.getLowerHeight(newLoc, mob.getGeoIndex()) < GeoEngine.getLowerHeight(newLoc.clone().changeZ(5000), mob.getGeoIndex()));
		for(final GlobalEvent e : getEvents())
			mob.addEvent(e);
		if(spawn)
		{
			if(mob.isMonster())
				((MonsterInstance) mob).setChampion();
			mob.spawnMe(newLoc);
			++_currentCount;
		}
		else
		{
			mob.setLoc(newLoc);
			++_scheduledCount;
			SpawnTaskManager.getInstance().addSpawnTask(mob, _respawnTime * 1000L - System.currentTimeMillis());
		}
		_spawned.add(mob);
		return _lastSpawn = mob;
	}

	public void decreaseCount0(NpcTemplate template, NpcInstance spawnedNpc, long deathTime)
	{
		--_currentCount;

		if(_currentCount < 0)
			_currentCount = 0;

		if(template == null || spawnedNpc == null)
			return;

		if(!hasRespawn())
			return;

		if(_doRespawn && _scheduledCount + _currentCount < _maximumCount)
		{
			// Update the current number of SpawnTask in progress or stand by of this L2Spawn
			_scheduledCount++;
			_respawnTime = Math.max(calcRespawnTime(deathTime, template.isRaid), (int) ((System.currentTimeMillis() + 1000) / 1000));

			SpawnTaskManager.getInstance().addSpawnTask(spawnedNpc, _respawnTime * 1000L - System.currentTimeMillis());
		}
	}

	public int calcRespawnTime(long deathTime, boolean isRaid)
	{
		int respawnTime;
		if(getRespawnPattern() != null)
			respawnTime = (int) (getRespawnPattern().next(deathTime) / 1000);
		else
		{
			long delay = (long) (/*isRaid ? Config.ALT_RAID_RESPAWN_MULTIPLIER : */1.) * getRespawnDelayWithRnd() * 1000L;
			respawnTime = (int) ((deathTime + delay) / 1000);
		}
		return respawnTime;
	}
}
