package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import l2s.commons.collections.LazyArrayList;
import l2s.gameserver.Config;
import l2s.gameserver.ai.DefaultAI;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public final class WorldRegion
{
	private GameObject[] _objects;
	private List<Territory> territories;
	private int tileX;
	private int tileY;
	private int tileZ;
	private int _objectsSize;
	private int _playersSize;
	private boolean _small;
	private final ReentrantLock objects_lock;
	private final ReentrantLock territories_lock;

	public WorldRegion(final int pTileX, final int pTileY, final int pTileZ, final boolean small)
	{
		_objects = null;
		territories = null;
		_objectsSize = 0;
		_playersSize = 0;
		_small = false;
		objects_lock = new ReentrantLock();
		territories_lock = new ReentrantLock();
		tileX = pTileX;
		tileY = pTileY;
		tileZ = pTileZ;
		_small = small;
	}

	public void addToPlayers(final GameObject object, final Creature dropper)
	{
		if(_objects == null)
		{
			_objectsSize = 0;
			_playersSize = 0;
			return;
		}
		Player player = null;
		if(object.isPlayer())
			player = (Player) object;
		if(player != null)
		{
			final List<L2GameServerPacket> result = new ArrayList<L2GameServerPacket>(_objectsSize);
			for(final GameObject obj : getObjectsListLimit(new ArrayList<GameObject>(_objectsSize), object.getObjectId(), object.getReflectionId()))
			{
				if(obj.inObserverMode() && obj.getOlympiadObserveId() == -1)
				{
					if(obj.getCurrentRegion() == null)
						continue;
					if(!obj.getCurrentRegion().equals(this))
						continue;
				}
				result.addAll(player.addVisibleObject(obj, dropper));
			}
			player.sendPacket(result);
		}
		for(final Player pl : this.getPlayersList(new ArrayList<Player>(_playersSize), object.getObjectId(), object.getReflectionId()))
			pl.sendPacket(pl.addVisibleObject(object, dropper));
	}

	public void removeFromPlayers(final GameObject object, final boolean move)
	{
		if(_objects == null)
		{
			_objectsSize = 0;
			_playersSize = 0;
			return;
		}
		Player player = null;
		if(object.isPlayer())
			player = (Player) object;
		for(final GameObject obj : this.getObjectsList(new ArrayList<GameObject>(_objectsSize), object.getObjectId(), object.getReflectionId()))
		{
			if(player != null)
				player.sendPacket(player.removeVisibleObject(obj, null, move));
			if(obj.isPlayer())
			{
				final Player p = (Player) obj;
				p.sendPacket(p.removeVisibleObject(object, object.deletePacketList(), move));
			}
		}
	}

	public GameObject[] getObjects()
	{
		objects_lock.lock();
		try
		{
			if(_objects == null)
			{
				_objects = new GameObject[50];
				_objectsSize = 0;
				_playersSize = 0;
			}
			return _objects;
		}
		finally
		{
			objects_lock.unlock();
		}
	}

	public void addObject(final GameObject obj)
	{
		if(obj == null)
			return;
		objects_lock.lock();
		try
		{
			if(_objects == null)
			{
				_objects = new GameObject[50];
				_objectsSize = 0;
			}
			else if(_objectsSize >= _objects.length)
			{
				final GameObject[] temp = new GameObject[_objects.length * 2];
				for(int i = 0; i < _objectsSize; ++i)
					temp[i] = _objects[i];
				_objects = temp;
			}
			_objects[_objectsSize] = obj;
			++_objectsSize;
			if(obj.isPlayer())
				++_playersSize;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		if(obj.isNpc() && obj.getAI() instanceof DefaultAI && obj.getAI().isGlobalAI() && !obj.getAI().isActive())
			obj.enableAI();
	}

	public void removeObject(final GameObject obj, final boolean move)
	{
		if(obj == null)
			return;
		objects_lock.lock();
		try
		{
			if(_objects == null)
			{
				_objectsSize = 0;
				_playersSize = 0;
				return;
			}
			if(_objectsSize > 1)
			{
				int k = -1;
				for(int i = 0; i < _objectsSize; ++i)
					if(_objects[i] == obj)
					{
						k = i;
						break;
					}
				if(k > -1)
				{
					_objects[k] = _objects[_objectsSize - 1];
					_objects[_objectsSize - 1] = null;
					--_objectsSize;
				}
			}
			else if(_objectsSize == 1 && _objects[0] == obj)
			{
				_objects[0] = null;
				_objects = null;
				_objectsSize = 0;
				_playersSize = 0;
			}
			if(obj.isPlayer())
			{
				--_playersSize;
				if(_playersSize <= 0)
					_playersSize = 0;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		if(!move && obj.isNpc() && obj.getAI() instanceof DefaultAI && !obj.getAI().isGlobalAI())
			obj.disableAI();
	}

	private NpcInstance getNearestNpc(final List<NpcInstance> targets, final Player player)
	{
		NpcInstance nextTarget = null;
		long minDist = Long.MAX_VALUE;
		for(int i = 0; i < targets.size(); ++i)
		{
			final NpcInstance target = targets.get(i);
			final long dist = player.getXYZDeltaSq(target.getX(), target.getY(), target.getZ());
			if(dist < minDist)
			{
				minDist = dist;
				nextTarget = target;
			}
		}
		return nextTarget;
	}

	public List<GameObject> getObjectsListLimit(final List<GameObject> result, final int exclude, final int instanceId)
	{
		if(Config.NPC_SHOW_LIMIT <= 0)
			return this.getObjectsList(result, exclude, instanceId);
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			final List<NpcInstance> npcs = new LazyArrayList<NpcInstance>();
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.getObjectId() != exclude && obj.getReflectionId() == instanceId)
					if(obj.isNpc())
						npcs.add((NpcInstance) obj);
					else
						result.add(obj);
			}
			if(!npcs.isEmpty())
			{
				final Player player = GameObjectsStorage.getPlayer(exclude);
				if(player != null)
				{
					int count = 0;
					int cnt = 0;
					final int size = Math.min(npcs.size(), 1000);
					while(!npcs.isEmpty())
					{
						if(++count > size)
							break;
						final NpcInstance npc = getNearestNpc(npcs, player);
						if(npc == null)
							break;
						++cnt;
						result.add(npc);
						npcs.remove(npc);
						if(cnt >= Config.NPC_SHOW_LIMIT)
							break;
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<GameObject> getObjectsList(final List<GameObject> result, final int exclude, final int instanceId)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.getObjectId() != exclude && obj.getReflectionId() == instanceId)
					result.add(obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<GameObject> getObjectsList(final List<GameObject> result, final int exclude, final int instanceId, final int x, final int y, final int z, final long sqrad, final int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.getObjectId() != exclude)
					if(obj.getReflectionId() == instanceId)
						if(Math.abs(obj.getZ() - z) <= height)
						{
							long dx = obj.getX() - x;
							dx *= dx;
							if(dx <= sqrad)
							{
								long dy = obj.getY() - y;
								dy *= dy;
								if(dx + dy < sqrad)
									result.add(obj);
							}
						}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<Creature> getCharactersList(final List<Creature> result, final int exclude, final int instanceId)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isCreature() && obj.getObjectId() != exclude && obj.getReflectionId() == instanceId)
					result.add((Creature) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<Creature> getCharactersList(final List<Creature> result, final int exclude, final int instanceId, final int x, final int y, final int z, final long sqrad, final int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isCreature() && obj.getObjectId() != exclude)
					if(obj.getReflectionId() == instanceId)
						if(Math.abs(obj.getZ() - z) <= height)
						{
							long dx = obj.getX() - x;
							dx *= dx;
							if(dx <= sqrad)
							{
								long dy = obj.getY() - y;
								dy *= dy;
								if(dx + dy < sqrad)
									result.add((Creature) obj);
							}
						}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<NpcInstance> getNpcsList(final List<NpcInstance> result, final int exclude, final int instanceId)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isNpc() && obj.getObjectId() != exclude && obj.getReflectionId() == instanceId)
					result.add((NpcInstance) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<NpcInstance> getNpcsList(final List<NpcInstance> result, final int exclude, final int instanceId, final int x, final int y, final int z, final long sqrad, final int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isNpc() && obj.getObjectId() != exclude)
					if(obj.getReflectionId() == instanceId)
						if(Math.abs(obj.getZ() - z) <= height)
						{
							long dx = obj.getX() - x;
							dx *= dx;
							if(dx <= sqrad)
							{
								long dy = obj.getY() - y;
								dy *= dy;
								if(dx + dy < sqrad)
									result.add((NpcInstance) obj);
							}
						}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<Player> getPlayersList(final List<Player> result, final int exclude, final int instanceId)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isPlayer() && obj.getObjectId() != exclude && obj.getReflectionId() == instanceId)
					result.add((Player) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<Player> getPlayersList(final List<Player> result, final int exclude, final int instanceId, final int x, final int y, final int z, final long sqrad, final int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isPlayer() && obj.getObjectId() != exclude)
					if(obj.getReflectionId() == instanceId)
						if(Math.abs(obj.getZ() - z) <= height)
						{
							long dx = obj.getX() - x;
							dx *= dx;
							if(dx <= sqrad)
							{
								long dy = obj.getY() - y;
								dy *= dy;
								if(dx + dy < sqrad)
									result.add((Player) obj);
							}
						}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<Playable> getPlayablesList(final List<Playable> result, final int exclude, final int instanceId)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isPlayable() && obj.getObjectId() != exclude && obj.getReflectionId() == instanceId)
					result.add((Playable) obj);
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public List<Playable> getPlayablesList(final List<Playable> result, final int exclude, final int instanceId, final int x, final int y, final int z, final long sqrad, final int height)
	{
		objects_lock.lock();
		try
		{
			if(_objects == null || _objectsSize == 0)
				return result;
			for(int i = 0; i < _objectsSize; ++i)
			{
				final GameObject obj = _objects[i];
				if(obj != null && obj.isPlayable() && obj.getObjectId() != exclude)
					if(obj.getReflectionId() == instanceId)
						if(Math.abs(obj.getZ() - z) <= height)
						{
							long dx = obj.getX() - x;
							dx *= dx;
							if(dx <= sqrad)
							{
								long dy = obj.getY() - y;
								dy *= dy;
								if(dx + dy < sqrad)
									result.add((Playable) obj);
							}
						}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		return result;
	}

	public void deleteVisibleNpcSpawns()
	{
		final List<NpcInstance> toRemove = new ArrayList<NpcInstance>(_objectsSize);
		objects_lock.lock();
		try
		{
			if(_objects != null)
				for(int i = 0; i < _objectsSize; ++i)
				{
					final GameObject obj = _objects[i];
					if(obj != null && obj.isNpc())
						toRemove.add((NpcInstance) obj);
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			objects_lock.unlock();
		}
		for(final NpcInstance npc : toRemove)
		{
			final Spawn spawn = npc.getSpawn();
			final Spawner sp = npc.getSpawn2();
			if(spawn != null)
			{
				npc.deleteMe();
				spawn.stopRespawn();
			}
			else
			{
				if(sp == null)
					continue;
				npc.deleteMe();
				sp.stopRespawn();
			}
		}
	}

	public void showObjectsToPlayer(final Player player)
	{
		if(player != null && _objects != null)
			for(final GameObject obj : getObjectsListLimit(new ArrayList<GameObject>(_objectsSize), player.getObjectId(), player.getReflectionId()))
				player.sendPacket(player.addVisibleObject(obj, null));
	}

	public void removeObjectsFromPlayer(final Player player)
	{
		if(player != null && _objects != null)
			for(final GameObject obj : this.getObjectsList(new ArrayList<GameObject>(_objectsSize), player.getObjectId(), player.getReflectionId()))
				player.sendPacket(player.removeVisibleObject(obj, null, true));
	}

	public void removePlayerFromOtherPlayers(final GameObject object)
	{
		if(object != null && _objects != null)
			for(final Player pl : this.getPlayersList(new ArrayList<Player>(_playersSize), object.getObjectId(), object.getReflectionId()))
				if(pl != null)
					pl.sendPacket(pl.removeVisibleObject(object, object.deletePacketList(), true));
	}

	public boolean areNeighborsEmpty()
	{
		if(!isEmpty())
			return false;
		for(final WorldRegion neighbor : getNeighbors())
			if(!neighbor.isEmpty())
				return false;
		return true;
	}

	public List<WorldRegion> getNeighbors()
	{
		return World.getNeighbors(tileX, tileY, tileZ, _small);
	}

	public int getObjectsSize()
	{
		return _objectsSize;
	}

	public int getPlayersSize()
	{
		return _playersSize;
	}

	public boolean isEmpty()
	{
		return _playersSize <= 0;
	}

	public boolean isNull()
	{
		return _objectsSize <= 0;
	}

	public String getName()
	{
		return "(" + tileX + ", " + tileY + ", " + tileZ + ")";
	}

	public void addTerritory(final Territory territory)
	{
		territories_lock.lock();
		try
		{
			if(territories == null)
				territories = new ArrayList<Territory>(5);
			if(!territories.contains(territory))
				territories.add(territory);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			territories_lock.unlock();
		}
	}

	public void removeTerritory(final Territory territory)
	{
		territories_lock.lock();
		try
		{
			if(territories == null)
				return;
			territories.remove(territory);
			if(territories.isEmpty())
				territories = null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			territories_lock.unlock();
		}
	}

	public List<Territory> getTerritories(final int x, final int y, final int z)
	{
		territories_lock.lock();
		try
		{
			if(territories == null)
				return null;
			final List<Territory> result = new ArrayList<Territory>(territories.size());
			for(final Territory terr : territories)
				if(terr != null && terr.isInside(x, y, z))
					result.add(terr);
			return result.isEmpty() ? null : result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			territories_lock.unlock();
		}
		return null;
	}
}
