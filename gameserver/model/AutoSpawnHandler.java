package l2s.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.instancemanager.TownManager;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;

public class AutoSpawnHandler
{
	protected static Logger _log = LoggerFactory.getLogger(AutoSpawnHandler.class);

	private static AutoSpawnHandler _instance;
	private static final int DEFAULT_INITIAL_SPAWN = 30000;
	private static final int DEFAULT_RESPAWN = 3600000;
	private static final int DEFAULT_DESPAWN = 3600000;
	protected Map<Integer, AutoSpawnInstance> _registeredSpawns;
	protected Map<Integer, ScheduledFuture<?>> _runningSpawns;
	protected boolean _activeState;

	public AutoSpawnHandler()
	{
		_activeState = true;
		_registeredSpawns = new HashMap<Integer, AutoSpawnInstance>();
		_runningSpawns = new HashMap<Integer, ScheduledFuture<?>>();
		restoreSpawnData();
	}

	public static AutoSpawnHandler getInstance()
	{
		if(_instance == null)
			_instance = new AutoSpawnHandler();
		return _instance;
	}

	public final int size()
	{
		return _registeredSpawns.size();
	}

	private void restoreSpawnData()
	{
		int numLoaded = 0;
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		ResultSet rset2 = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");
			statement2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final AutoSpawnInstance spawnInst = this.registerSpawn(rset.getInt("npcId"), rset.getInt("initialDelay"), rset.getInt("respawnDelay"), rset.getInt("despawnDelay"));
				spawnInst.setSpawnCount(rset.getByte("count"));
				spawnInst.setBroadcast(rset.getBoolean("broadcastSpawn"));
				spawnInst.setRandomSpawn(rset.getBoolean("randomSpawn"));
				++numLoaded;
				statement2.setInt(1, rset.getInt("groupId"));
				rset2 = statement2.executeQuery();
				while(rset2.next())
					spawnInst.addSpawnLocation(rset2.getInt("x"), rset2.getInt("y"), rset2.getInt("z"), rset2.getInt("heading"));
				DbUtils.close(rset2);
			}
		}
		catch(Exception e)
		{
			_log.warn("AutoSpawnHandler: Could not restore spawn data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public AutoSpawnInstance registerSpawn(final int npcId, final int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay)
	{
		if(initialDelay < 0)
			initialDelay = 30000;
		if(respawnDelay < 0)
			respawnDelay = 3600000;
		if(despawnDelay < 0)
			despawnDelay = 3600000;
		final AutoSpawnInstance newSpawn = new AutoSpawnInstance(npcId, initialDelay, respawnDelay, despawnDelay);
		if(spawnPoints != null)
			for(final int[] spawnPoint : spawnPoints)
				newSpawn.addSpawnLocation(spawnPoint);
		final int newId = IdFactory.getInstance().getNextId();
		newSpawn._objectId = newId;
		_registeredSpawns.put(newId, newSpawn);
		setSpawnActive(newSpawn, true);
		return newSpawn;
	}

	public AutoSpawnInstance registerSpawn(final int npcId, final int initialDelay, final int respawnDelay, final int despawnDelay)
	{
		return this.registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
	}

	public boolean removeSpawn(final AutoSpawnInstance spawnInst)
	{
		if(!this.isSpawnRegistered(spawnInst))
			return false;
		try
		{
			_registeredSpawns.remove(spawnInst.getNpcId());
			final ScheduledFuture<?> respawnTask = _runningSpawns.remove(spawnInst._objectId);
			respawnTask.cancel(false);
		}
		catch(Exception e)
		{
			_log.warn("AutoSpawnHandler: Could not auto spawn for NPC ID " + spawnInst._npcId + " (Object ID = " + spawnInst._objectId + "): " + e);
			return false;
		}
		return true;
	}

	public void removeSpawn(final int objectId)
	{
		this.removeSpawn(_registeredSpawns.get(objectId));
	}

	public void setSpawnActive(final AutoSpawnInstance spawnInst, final boolean isActive)
	{
		final int objectId = spawnInst._objectId;
		if(this.isSpawnRegistered(objectId))
		{
			ScheduledFuture<?> spawnTask = null;
			if(isActive)
			{
				final AutoSpawner rset = new AutoSpawner(objectId);
				if(spawnInst._desDelay > 0)
					spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(rset, spawnInst._initDelay, spawnInst._resDelay);
				else
					spawnTask = ThreadPoolManager.getInstance().schedule(rset, spawnInst._initDelay);
				_runningSpawns.put(objectId, spawnTask);
			}
			else
			{
				spawnTask = _runningSpawns.remove(objectId);
				if(spawnTask != null)
					spawnTask.cancel(false);
			}
			spawnInst.setSpawnActive(isActive);
		}
	}

	public final long getTimeToNextSpawn(final AutoSpawnInstance spawnInst)
	{
		final int objectId = spawnInst._objectId;
		if(!this.isSpawnRegistered(objectId))
			return -1L;
		return _runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
	}

	public final AutoSpawnInstance getAutoSpawnInstance(final int id, final boolean isObjectId)
	{
		if(isObjectId)
		{
			if(this.isSpawnRegistered(id))
				return _registeredSpawns.get(id);
		}
		else
			for(final AutoSpawnInstance spawnInst : _registeredSpawns.values())
				if(spawnInst._npcId == id)
					return spawnInst;
		return null;
	}

	public Map<Integer, AutoSpawnInstance> getAllAutoSpawnInstance(final int id)
	{
		final Map<Integer, AutoSpawnInstance> spawnInstList = new HashMap<Integer, AutoSpawnInstance>();
		for(final AutoSpawnInstance spawnInst : _registeredSpawns.values())
			if(spawnInst._npcId == id)
				spawnInstList.put(spawnInst._objectId, spawnInst);
		return spawnInstList;
	}

	public final boolean isSpawnRegistered(final int objectId)
	{
		return _registeredSpawns.containsKey(objectId);
	}

	public final boolean isSpawnRegistered(final AutoSpawnInstance spawnInst)
	{
		return _registeredSpawns.containsValue(spawnInst);
	}

	private class AutoSpawner implements Runnable
	{
		private int _objectId;

		AutoSpawner(final int objectId)
		{
			_objectId = objectId;
		}

		@Override
		public void run()
		{
			try
			{
				final AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);
				if(!spawnInst.isSpawnActive() || Config.DONTLOADSPAWN)
					return;
				final Location[] locationList = spawnInst.getLocationList();
				if(locationList.length == 0)
				{
					_log.info("AutoSpawnHandler: No location co-ords specified for spawn instance (Object ID = " + _objectId + ").");
					return;
				}
				final int locationCount = locationList.length;
				int locationIndex = Rnd.get(locationCount);
				if(!spawnInst.isRandomSpawn())
				{
					locationIndex = spawnInst._lastLocIndex;
					if(++locationIndex == locationCount)
						locationIndex = 0;
					spawnInst._lastLocIndex = locationIndex;
				}
				final int x = locationList[locationIndex].x;
				final int y = locationList[locationIndex].y;
				final int z = locationList[locationIndex].z;
				final int heading = locationList[locationIndex].h;
				final NpcTemplate npcTemp = NpcTable.getTemplate(spawnInst.getNpcId());
				final Spawn newSpawn = new Spawn(npcTemp);
				newSpawn.setLocx(x);
				newSpawn.setLocy(y);
				newSpawn.setLocz(z);
				if(heading != -1)
					newSpawn.setHeading(heading);
				newSpawn.setAmount(spawnInst.getSpawnCount());
				if(spawnInst._desDelay == 0)
					newSpawn.setRespawnDelay(spawnInst._resDelay);
				NpcInstance npcInst = null;
				for(int i = 0; i < spawnInst._spawnCount; ++i)
				{
					npcInst = newSpawn.doSpawn(true);
					npcInst.setXYZ(npcInst.getX() + Rnd.get(50), npcInst.getY() + Rnd.get(50), npcInst.getZ());
					spawnInst.addAttackable(npcInst);
				}
				final String nearestTown = TownManager.getInstance().getClosestTownName(npcInst);
				if(spawnInst.isBroadcasting() && npcInst != null)
					Announcements.getInstance().announceToAll("The " + npcInst.getName() + " has spawned near " + nearestTown + "!");
				if(spawnInst.getDespawnDelay() > 0)
				{
					final AutoDespawner rd = new AutoDespawner(_objectId);
					ThreadPoolManager.getInstance().schedule(rd, spawnInst.getDespawnDelay() - 1000);
				}
			}
			catch(Exception e)
			{
				_log.error("AutoSpawnHandler: An error occurred while initializing spawn instance (Object ID = " + _objectId + "): ", e);
			}
		}
	}

	private class AutoDespawner implements Runnable
	{
		private int _objectId;

		AutoDespawner(final int objectId)
		{
			_objectId = objectId;
		}

		@Override
		public void run()
		{
			try
			{
				final AutoSpawnInstance spawnInst = _registeredSpawns.get(_objectId);
				for(final NpcInstance npcInst : spawnInst.getAttackableList())
				{
					npcInst.deleteMe();
					spawnInst.removeAttackable(npcInst);
				}
			}
			catch(Exception e)
			{
				_log.warn("AutoSpawnHandler: An error occurred while despawning spawn (Object ID = " + _objectId + "): " + e);
			}
		}
	}

	public class AutoSpawnInstance
	{
		protected int _objectId;
		protected int _spawnIndex;
		protected int _npcId;
		protected int _initDelay;
		protected int _resDelay;
		protected int _desDelay;
		protected byte _spawnCount;
		protected int _lastLocIndex;
		private List<NpcInstance> _npcList;
		private List<Location> _locList;
		private boolean _spawnActive;
		private boolean _randomSpawn;
		private boolean _broadcastAnnouncement;

		protected AutoSpawnInstance(final int npcId, final int initDelay, final int respawnDelay, final int despawnDelay)
		{
			_spawnCount = 1;
			_lastLocIndex = -1;
			_npcList = new ArrayList<NpcInstance>();
			_locList = new ArrayList<Location>();
			_randomSpawn = false;
			_broadcastAnnouncement = false;
			_npcId = npcId;
			_initDelay = initDelay;
			_resDelay = respawnDelay;
			_desDelay = despawnDelay;
		}

		void setSpawnActive(final boolean activeValue)
		{
			_spawnActive = activeValue;
		}

		boolean addAttackable(final NpcInstance npcInst)
		{
			return _npcList.add(npcInst);
		}

		boolean removeAttackable(final NpcInstance npcInst)
		{
			return _npcList.remove(npcInst);
		}

		public int getObjectId()
		{
			return _objectId;
		}

		public int getInitialDelay()
		{
			return _initDelay;
		}

		public int getRespawnDelay()
		{
			return _resDelay;
		}

		public int getDespawnDelay()
		{
			return _desDelay;
		}

		public int getNpcId()
		{
			return _npcId;
		}

		public int getSpawnCount()
		{
			return _spawnCount;
		}

		public Location[] getLocationList()
		{
			return (Location[]) _locList.toArray((Object[]) new Location[_locList.size()]);
		}

		public NpcInstance[] getAttackableList()
		{
			return (NpcInstance[]) _npcList.toArray((Object[]) new NpcInstance[_npcList.size()]);
		}

		public Spawn[] getSpawns()
		{
			final List<Spawn> npcSpawns = new ArrayList<Spawn>();
			for(final NpcInstance npcInst : _npcList)
				npcSpawns.add(npcInst.getSpawn());
			return (Spawn[]) npcSpawns.toArray((Object[]) new Spawn[npcSpawns.size()]);
		}

		public void setSpawnCount(final byte spawnCount)
		{
			_spawnCount = spawnCount;
		}

		public void setRandomSpawn(final boolean randValue)
		{
			_randomSpawn = randValue;
		}

		public void setBroadcast(final boolean broadcastValue)
		{
			_broadcastAnnouncement = broadcastValue;
		}

		public boolean isSpawnActive()
		{
			return _spawnActive;
		}

		public boolean isRandomSpawn()
		{
			return _randomSpawn;
		}

		public boolean isBroadcasting()
		{
			return _broadcastAnnouncement;
		}

		public boolean addSpawnLocation(final int x, final int y, final int z, final int heading)
		{
			return _locList.add(new Location(x, y, z, heading));
		}

		public boolean addSpawnLocation(final int[] spawnLoc)
		{
			return spawnLoc.length == 3 && this.addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
		}

		public Location removeSpawnLocation(final int locIndex)
		{
			try
			{
				return _locList.remove(locIndex);
			}
			catch(IndexOutOfBoundsException e)
			{
				return null;
			}
		}
	}
}
