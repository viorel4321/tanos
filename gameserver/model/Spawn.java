package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.instancemanager.SpawnManager;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.tables.TerritoryTable;
import l2s.gameserver.taskmanager.SpawnTaskManager;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.SchedulableEvent;

public class Spawn implements Cloneable
{
	private static Logger _log = LoggerFactory.getLogger(Spawn.class);

	private static final int DEFAULT_RESPAWN_DELAY = 30;
	private int _npcId;
	private int _locx;
	private int _locy;
	private int _locz;
	private int _heading;
	private int _location;
	private int _maximumCount;
	private int _referenceCount;
	private int _currentCount;
	private int _scheduledCount;
	private int _respawnDelay;
	private int _respawnDelayRandom;
	private int _respawnTime;
	boolean _doRespawn;
	private NpcInstance _lastSpawn;
	private static final List<SpawnListener> _spawnListeners = new ArrayList<SpawnListener>();
	public HashMap<String, List<SchedulableEvent>> _events;
	private List<NpcInstance> _spawned;
	private int _instanceId;
	private int _fixResp;

	public boolean isDoRespawn()
	{
		return _doRespawn;
	}

	public int getInstanceId()
	{
		return _instanceId;
	}

	public void setInstanceId(final int id)
	{
		_instanceId = id;
	}

	public void decreaseScheduledCount()
	{
		if(_scheduledCount > 0)
			--_scheduledCount;
	}

	public Spawn(final NpcTemplate mobTemplate) throws ClassNotFoundException
	{
		this(mobTemplate.getId());
	}

	public Spawn(final int npcId) throws ClassNotFoundException
	{
		_npcId = npcId;
		final NpcTemplate mobTemplate = NpcTable.getTemplate(npcId);
		if(mobTemplate == null || mobTemplate.getInstanceConstructor() == null)
			throw new ClassNotFoundException("Unable to instantiate npc " + npcId);
		_spawned = new ArrayList<NpcInstance>(1);
	}

	public int getAmount()
	{
		return _maximumCount;
	}

	public int getSpawnedCount()
	{
		return _currentCount;
	}

	public int getSheduledCount()
	{
		return _scheduledCount;
	}

	public int getLocation()
	{
		return _location;
	}

	public Location getLoc()
	{
		return new Location(_locx, _locy, _locz);
	}

	public int getLocx()
	{
		return _locx;
	}

	public int getLocy()
	{
		return _locy;
	}

	public int getLocz()
	{
		return _locz;
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public int getHeading()
	{
		return _heading;
	}

	public int getRespawnDelay()
	{
		return _respawnDelay;
	}

	public int getRespawnDelayRandom()
	{
		return _respawnDelayRandom;
	}

	public int getRespawnDelayWithRnd()
	{
		return _respawnDelayRandom == 0 ? _respawnDelay : Rnd.get(_respawnDelay - _respawnDelayRandom, _respawnDelay + _respawnDelayRandom);
	}

	public int getRespawnTime()
	{
		return _respawnTime;
	}

	public void setAmount(final int amount)
	{
		if(_referenceCount == 0)
			_referenceCount = amount;
		_maximumCount = amount;
	}

	public void restoreAmount()
	{
		_maximumCount = _referenceCount;
	}

	public void setLocation(final int location)
	{
		_location = location;
	}

	public void setLoc(final Location loc)
	{
		_locx = loc.x;
		_locy = loc.y;
		_locz = loc.z;
		_heading = loc.h;
	}

	public void setLocx(final int locx)
	{
		_locx = locx;
	}

	public void setLocy(final int locy)
	{
		_locy = locy;
	}

	public void setLocz(final int locz)
	{
		_locz = locz;
	}

	public void setHeading(final int heading)
	{
		_heading = heading;
	}

	public void decreaseCount(final NpcInstance oldNpc)
	{
		--_currentCount;
		if(_currentCount < 0)
			_currentCount = 0;
		notifyNpcDeSpawned(oldNpc);
		if(_doRespawn && _scheduledCount + _currentCount < _maximumCount)
		{
			++_scheduledCount;
			long delay = _fixResp > 0 ? _fixResp * 1000L - System.currentTimeMillis() : getRespawnDelayWithRnd() * 1000L;
			delay = Math.max(1000L, delay - oldNpc.getDeadTime());
			_respawnTime = _fixResp > 0 ? _fixResp : (int) ((System.currentTimeMillis() + delay) / 1000L);
			addSpawnTask(oldNpc, delay);
		}
	}

	public void setFixResp(final int fixResp)
	{
		_fixResp = fixResp;
	}

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

	public void despawnAll()
	{
		stopRespawn();
		for(final NpcInstance npc : getAllSpawned())
			if(npc != null)
				npc.deleteMe();
		_currentCount = 0;
	}

	public void stopRespawn()
	{
		_doRespawn = false;
	}

	public void startRespawn()
	{
		_doRespawn = true;
	}

	public NpcInstance doSpawn(boolean spawn)
	{
		try
		{
			final NpcTemplate template = NpcTable.getTemplate(_npcId);
			if(template.isInstanceOf(PetInstance.class))
			{
				++_currentCount;
				return null;
			}
			final Object tmp = template.getNewInstance();
			if(!(tmp instanceof NpcInstance))
				return null;
			if(!spawn)
				spawn = _respawnTime <= System.currentTimeMillis() / 1000L + 30L;
			_spawned.add((NpcInstance) tmp);
			if(_events != null)
				for(final String methodName : _events.keySet())
					for(final SchedulableEvent se : _events.get(methodName))
						if(se != null)
							((NpcInstance) tmp).addMethodInvokeListener(methodName, se);
			return intializeNpc((NpcInstance) tmp, spawn);
		}
		catch(Exception e)
		{
			Spawn._log.error("NPC " + _npcId + " class not found", e);
			return null;
		}
	}

	public List<NpcInstance> getAllSpawned()
	{
		return _spawned;
	}

	private NpcInstance intializeNpc(NpcInstance mob, final boolean spawn)
	{
		Location newLoc;
		if(getLocation() != 0 && (_locx == 0 || !SpawnManager.retardation))
		{
			final Location p = TerritoryTable.getInstance().getRandomLoc(getLocation(), mob.getGeoIndex());
			newLoc = Config.GEO_SP_LOC ? Location.findPointToStay(p.x, p.y, p.z, Config.GEO_SP1, Config.GEO_SP2, mob.getGeoIndex()).setH(Rnd.get(65535)) : p.setH(Rnd.get(65535));
		}
		else
		{
			newLoc = getLoc();
			newLoc.h = getHeading() == -1 ? Rnd.get(65535) : getHeading();
		}
		if(mob.isChest() && Rnd.chance(Config.ALT_TRUE_CHESTS) || mob.isBox() && mob.getNpcId() > 18264 && !Rnd.chance(Config.ALT_TRUE_CHESTS))
		{
			final NpcTemplate template = NpcTable.getTemplate(mob.getTrueId());
			mob.deleteMe();
			mob = template.getNewInstance();
			startRespawn();
		}
		mob.getAbnormalList().stopAll();
		mob.setCurrentHpMp(mob.getMaxHp(), mob.getMaxMp(), true);
		mob.setSpawn(this);
		mob.setHeading(newLoc.h);
		mob.setSpawnedLoc(newLoc);
		mob.setReflectionId(getInstanceId());
		mob.setUnderground(GeoEngine.getLowerHeight(newLoc, mob.getGeoIndex()) < GeoEngine.getLowerHeight(newLoc.clone().changeZ(5000), mob.getGeoIndex()));
		if(spawn)
		{
			if(mob.isMonster())
				((MonsterInstance) mob).setChampion();
			mob.spawnMe(newLoc);
			notifyNpcSpawned(mob);
			++_currentCount;
		}
		else
		{
			mob.setXYZInvisible(newLoc);
			++_scheduledCount;
			addSpawnTask(mob, _respawnTime * 1000L - System.currentTimeMillis());
		}
		return _lastSpawn = mob;
	}

	private void addSpawnTask(final NpcInstance actor, final long interval)
	{
		SpawnTaskManager.getInstance().addSpawnTask(actor, interval);
	}

	public static void addSpawnListener(final SpawnListener listener)
	{
		synchronized (Spawn._spawnListeners)
		{
			Spawn._spawnListeners.add(listener);
		}
	}

	public static void notifyNpcSpawned(final NpcInstance npc)
	{
		synchronized (Spawn._spawnListeners)
		{
			for(final SpawnListener listener : Spawn._spawnListeners)
				listener.npcSpawned(npc);
		}
	}

	public static void notifyNpcDeSpawned(final NpcInstance npc)
	{
		synchronized (Spawn._spawnListeners)
		{
			for(final SpawnListener listener : Spawn._spawnListeners)
				listener.npcDeSpawned(npc);
		}
	}

	public void setRespawnDelay(final int respawnDelay, final int respawnDelayRandom)
	{
		if(respawnDelay < 0)
			Spawn._log.warn("respawn delay is negative for npcId: " + getNpcId());
		_respawnDelay = respawnDelay >= 5 ? respawnDelay : 30;
		_respawnDelayRandom = respawnDelayRandom > 0 ? respawnDelayRandom : 0;
	}

	public void setRespawnDelay(final int respawnDelay)
	{
		this.setRespawnDelay(respawnDelay, 0);
	}

	public void setRespawnTime(final int respawnTime)
	{
		_respawnTime = respawnTime;
	}

	public NpcInstance getLastSpawn()
	{
		return _lastSpawn;
	}

	public void respawnNpc(final NpcInstance oldNpc)
	{
		oldNpc.refreshID();
		intializeNpc(oldNpc, true);
	}

	public NpcTemplate getTemplate()
	{
		return NpcTable.getTemplate(_npcId);
	}

	@Override
	public Spawn clone()
	{
		Spawn spawnDat = null;
		try
		{
			spawnDat = new Spawn(_npcId);
			spawnDat.setLocation(_location);
			spawnDat.setLocx(_locx);
			spawnDat.setLocy(_locy);
			spawnDat.setLocz(_locz);
			spawnDat.setHeading(_heading);
			spawnDat.setAmount(_maximumCount);
			spawnDat.setRespawnDelay(_respawnDelay, _respawnDelayRandom);
			spawnDat._events = _events;
		}
		catch(Exception e)
		{
			Spawn._log.error("", e);
		}
		return spawnDat;
	}
}
