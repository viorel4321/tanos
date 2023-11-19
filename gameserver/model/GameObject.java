package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import gnu.trove.map.TIntObjectMap;
import l2s.gameserver.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.dao.CastleHiredGuardDAO;
import l2s.gameserver.geodata.GeoControl;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geodata.GeoEngine.CeilGeoControlType;
import l2s.gameserver.instancemanager.QuestManager;
import l2s.gameserver.listener.MethodInvokeListener;
import l2s.gameserver.listener.PropertyChangeListener;
import l2s.gameserver.listener.engine.DefaultListenerEngine;
import l2s.gameserver.listener.engine.ListenerEngine;
import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.listener.events.PropertyEvent;
import l2s.gameserver.listener.events.GameObject.TerritoryChangeEvent;
import l2s.gameserver.model.entity.events.EventOwner;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.network.l2.s2c.DeleteObject;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.scripts.Events;
import l2s.gameserver.utils.Location;

import org.napile.primitive.pair.ByteObjectPair;

public abstract class GameObject extends EventOwner implements GeoControl
{
	private static final long serialVersionUID = 1L;

	private static final Logger _log = LoggerFactory.getLogger(GameObject.class);

	protected int _reflectionId;

	protected int _objectId;

	private int _x;
	private int _y;
	private int _z;

	private List<Territory> _territories;
	private final ReentrantLock territoriesLock;
	private List<Zone> _zones;
	protected boolean _hidden;
	WorldRegion _currentRegion;
	private DefaultListenerEngine<GameObject> listenerEngine;

	private Shape _geoShape;
	private TIntObjectMap<ByteObjectPair<CeilGeoControlType>> _geoAround;
	private int _geoControlIndex = -1;
	private final Lock _geoLock = new ReentrantLock();

	public GameObject(int objectId)
	{
		_reflectionId = 0;
		_territories = null;
		territoriesLock = new ReentrantLock();
		_zones = null;
		_objectId = objectId;
	}

	public HardReference<? extends GameObject> getRef()
	{
		return HardReferences.emptyRef();
	}

	private void clearRef()
	{
		final HardReference<? extends GameObject> reference = getRef();
		if(reference != null)
			reference.clear();
	}

	public int getGeoIndex()
	{
		return World.getInstance().getDefaultGeoIndex();
	}

	public int getReflectionId()
	{
		return _reflectionId;
	}

	public void setReflectionId(int reflectionId)
	{
		if(_reflectionId == reflectionId)
			return;

		boolean respawn = false;
		if(!_hidden)
		{
			decayMe();
			respawn = true;
		}

		_reflectionId = reflectionId;

		if(respawn)
			spawnMe();
	}

	@Override
	public final int hashCode()
	{
		return _objectId;
	}

	public final int getObjectId()
	{
		return _objectId;
	}

	public int getNpcId()
	{
		return 0;
	}

	public int getX()
	{
		return _x;
	}

	public int getY()
	{
		return _y;
	}

	public int getZ()
	{
		return _z;
	}

	public Location getLoc()
	{
		return new Location(_x, _y, _z, getHeading());
	}

	public boolean setLoc(final Location loc)
	{
		return setXYZ(loc.x, loc.y, loc.z);
	}

	public int getGeoZ(int x, int y, int z)
	{
		return GeoEngine.correctGeoZ(x, y, z, getGeoIndex());
	}

	public final int getGeoZ(Location loc)
	{
		return getGeoZ(loc.getX(), loc.getY(), loc.getZ());
	}

	public boolean setXYZ(int x, int y, int z)
	{
		x = World.validCoordX(x);
		y = World.validCoordY(y);
		z = World.validCoordZ(z);
		z = getGeoZ(x, y, z);

		if(!isVehicle()) {
			if (isFlying())
				z += 32;
			else if (isInWater())
				z += 16;
		}

		if(_x == x && _y == y && _z == z)
			return false;

		_x = x;
		_y = y;
		_z = z;

		World.addVisibleObject(this, null);

		// Обновляем геодату при изменении координат.
		refreshGeoControl();
		return true;
	}

	public void updateTerritories()
	{
		final List<Territory> current_territories = World.getTerritories(getX(), getY(), getZ());
		List<Territory> new_territories = null;
		List<Territory> old_territories = null;
		territoriesLock.lock();
		try
		{
			if(_territories == null)
				new_territories = current_territories;
			else
			{
				if(current_territories != null)
					for(final Territory terr : current_territories)
						if(!_territories.contains(terr))
						{
							if(new_territories == null)
								new_territories = new ArrayList<Territory>();
							new_territories.add(terr);
						}
				if(_territories.size() > 0)
					for(final Territory terr : _territories)
						if(current_territories == null || !current_territories.contains(terr))
						{
							if(old_territories == null)
								old_territories = new ArrayList<Territory>();
							old_territories.add(terr);
						}
			}
			if(current_territories != null && current_territories.size() > 0)
				_territories = current_territories;
			else
				_territories = null;
		}
		finally
		{
			territoriesLock.unlock();
		}
		if(old_territories != null)
			for(final Territory terr : old_territories)
				if(terr != null)
					terr.doLeave(this, true);
		if(new_territories != null)
			for(final Territory terr : new_territories)
				if(terr != null)
					terr.doEnter(this);
		firePropertyChanged(new TerritoryChangeEvent(old_territories, new_territories, this));
	}

	public void setXYZInvisible(final int x, final int y, final int z)
	{
		_x = World.validCoordX(x);
		_y = World.validCoordY(y);
		_z = World.validCoordZ(z);
		_hidden = true;
	}

	public void setXYZInvisible(final Location loc)
	{
		setXYZInvisible(loc.x, loc.y, loc.z);
	}

	public final boolean isVisible()
	{
		return !_hidden;
	}

	public boolean isInvisible()
	{
		return false;
	}

	public void dropMe(final Creature dropper, final Location loc)
	{
		if(dropper != null)
			setReflectionId(dropper.getReflectionId());
		_hidden = false;
		_x = loc.x;
		_y = loc.y;
		_z = getGeoZ(loc);
		World.addVisibleObject(this, dropper);
	}

	public final void spawnMe(final Location loc)
	{
		if(loc.x > World.MAP_MAX_X)
			loc.x = World.MAP_MAX_X - 5000;
		if(loc.x < World.MAP_MIN_X)
			loc.x = World.MAP_MIN_X + 5000;
		if(loc.y > World.MAP_MAX_Y)
			loc.y = World.MAP_MAX_Y - 5000;
		if(loc.y < World.MAP_MIN_Y)
			loc.y = World.MAP_MIN_Y + 5000;
		_x = loc.x;
		_y = loc.y;
		_z = getGeoZ(loc);
		if(loc.h > 0)
			setHeading(loc.h);
		spawnMe();
	}

	public void spawnMe()
	{
		_hidden = false;
		World.addVisibleObject(this, null);
		if(isCreature())
			updateTerritories();
		onSpawn();
	}

	public void toggleVisible()
	{
		if(isVisible())
			decayMe();
		else
			spawnMe();
	}

	/**
	 * Do Nothing.<BR><BR>
	 *
	 * <B><U> Overriden in </U> :</B><BR><BR>
	 * <li> L2Summon :  Reset isShowSpawnAnimation flag</li>
	 * <li> L2NpcInstance    :  Reset some flags</li><BR><BR>
	 *
	 */
	protected void onSpawn()
	{
		activateGeoControl();
	}

	public final void pickupMe(final Creature target)
	{
		if(isItem() && target != null && target.isPlayer())
		{
			final ItemInstance item = (ItemInstance) this;
			final int itemId = item.getItemId();
			final Player player = (Player) target;
			if(item.isMercTicket())
			{
				player.getCastle().getSpawnMerchantTickets().remove(item);
				CastleHiredGuardDAO.getInstance().delete(player.getCastle(), item);
			}
			else if(itemId == 57 || itemId == 6353)
			{
				final Quest q = QuestManager.getQuest(255);
				if(q != null)
					player.processQuestEvent(q.getId(), "CE" + itemId, null);
			}
		}
		_hidden = true;
		World.removeVisibleObject(this);
	}

	public final void decayMe()
	{
		_hidden = true;
		World.removeVisibleObject(this);
		onDespawn();
	}

	protected void onDespawn()
	{
		deactivateGeoControl();
	}

	public void deleteMe()
	{
		decayMe();
		onDelete();
	}

	protected void onDelete()
	{
		World.removeObject(this);
		clearRef();
	}

	public void onAction(final Player player, final boolean shift)
	{
		if(Events.onAction(player, this, shift))
			return;
		player.sendActionFailed();
	}

	public void onForcedAttack(final Player player, final boolean shift)
	{
		player.sendActionFailed();
	}

	public boolean isAttackable(final Creature attacker)
	{
		return false;
	}

	public abstract boolean isAutoAttackable(final Creature p0);

	public boolean isMarker()
	{
		return false;
	}

	public String getL2ClassShortName()
	{
		return getClass().getSimpleName();
	}

	public final long getXYDeltaSq(final int x, final int y)
	{
		final long dx = x - getX();
		final long dy = y - getY();
		return dx * dx + dy * dy;
	}

	public final long getXYDeltaSq(final Location loc)
	{
		return getXYDeltaSq(loc.x, loc.y);
	}

	public final long getZDeltaSq(final int z)
	{
		final long dz = z - getZ();
		return dz * dz;
	}

	public final long getZDeltaSq(final Location loc)
	{
		return getZDeltaSq(loc.z);
	}

	public final long getXYZDeltaSq(final int x, final int y, final int z)
	{
		return getXYDeltaSq(x, y) + getZDeltaSq(z);
	}

	public final long getXYZDeltaSq(final Location loc)
	{
		return getXYDeltaSq(loc.x, loc.y) + getZDeltaSq(loc.z);
	}

	public final double getDistance(final int x, final int y)
	{
		return Math.sqrt(getXYDeltaSq(x, y));
	}

	public final double getDistance(final int x, final int y, final int z)
	{
		return Math.sqrt(getXYZDeltaSq(x, y, z));
	}

	public final double getDistance(final Location loc)
	{
		return getDistance(loc.x, loc.y, loc.z);
	}

	public final boolean isInRange(final GameObject obj, final long range)
	{
		if(obj == null)
			return false;
		final long dx = Math.abs(obj.getX() - getX());
		if(dx > range)
			return false;
		final long dy = Math.abs(obj.getY() - getY());
		if(dy > range)
			return false;
		final long dz = Math.abs(obj.getZ() - getZ());
		return dz <= 1500L && dx * dx + dy * dy <= range * range;
	}

	public final boolean isInRangeZ(final GameObject obj, final long range)
	{
		if(obj == null)
			return false;
		final long dx = Math.abs(obj.getX() - getX());
		if(dx > range)
			return false;
		final long dy = Math.abs(obj.getY() - getY());
		if(dy > range)
			return false;
		final long dz = Math.abs(obj.getZ() - getZ());
		return dz <= range && dx * dx + dy * dy + dz * dz <= range * range;
	}

	public final boolean isInRange(final Location loc, final long range)
	{
		return isInRangeSq(loc, range * range);
	}

	public final boolean isInRangeSq(final Location loc, final long range)
	{
		return getXYDeltaSq(loc) <= range;
	}

	public final boolean isInRangeZ(final Location loc, final long range)
	{
		return isInRangeZSq(loc, range * range);
	}

	public final boolean isInRangeZSq(final Location loc, final long range)
	{
		return getXYZDeltaSq(loc) <= range;
	}

	public final double getDistance(final GameObject obj)
	{
		if(obj == null)
			return 0.0;
		return Math.sqrt(getXYDeltaSq(obj.getX(), obj.getY()));
	}

	public final double getDistance3D(final GameObject obj)
	{
		if(obj == null)
			return 0.0;
		return Math.sqrt(getXYZDeltaSq(obj.getX(), obj.getY(), obj.getZ()));
	}

	public final double getRealDistance(final GameObject obj)
	{
		return getRealDistance3D(obj, true);
	}

	public final double getRealDistance3D(final GameObject obj)
	{
		return getRealDistance3D(obj, false);
	}

	public final double getRealDistance3D(final GameObject obj, final boolean ignoreZ)
	{
		double distance = ignoreZ ? getDistance(obj) : getDistance3D(obj);
		if(isCreature())
			distance -= ((Creature) this).getTemplate().collisionRadius;
		if(obj.isCreature())
			distance -= ((Creature) obj).getTemplate().collisionRadius;
		return distance > 0.0 ? distance : 0.0;
	}

	public final long getSqDistance(final int x, final int y)
	{
		return getXYDeltaSq(x, y);
	}

	public final long getSqDistance(final GameObject obj)
	{
		if(obj == null)
			return 0L;
		return getXYDeltaSq(obj.getLoc());
	}

	public final double calculateDistance(final int x, final int y, final int z, final boolean includeZAxis, final boolean squared)
	{
		final double distance = Math.pow(x - getX(), 2.0) + Math.pow(y - getY(), 2.0) + (includeZAxis ? Math.pow(z - getZ(), 2.0) : 0.0);
		return squared ? distance : Math.sqrt(distance);
	}

	public final double calculateDistance(final Location loc, final boolean includeZAxis, final boolean squared)
	{
		return calculateDistance(loc.getX(), loc.getY(), loc.getZ(), includeZAxis, squared);
	}

	public Player getPlayer()
	{
		return null;
	}

	public boolean isInWorld()
	{
		return GameObjectsStorage.findObject(_objectId) != null;
	}

	public int getHeading()
	{
		return 0;
	}

	public int getMoveSpeed()
	{
		return 0;
	}

	public boolean isInZonePeace()
	{
		return isInZone(Zone.ZoneType.peace_zone) && !isInZoneBattle();
	}

	public boolean isInZoneBattle()
	{
		return isInZone(Zone.ZoneType.battle_zone) || isInZone(Zone.ZoneType.OlympiadStadia);
	}

	public boolean isInZoneOlympiad()
	{
		return isInZone(Zone.ZoneType.OlympiadStadia);
	}

	public boolean isInWater()
	{
		return false;
	}

	public void addZone(final Zone zone)
	{
		if(_zones == null)
			_zones = new ArrayList<Zone>(3);
		_zones.add(zone);
	}

	public void removeZone(final Zone zone)
	{
		if(_zones == null)
			return;
		_zones.remove(zone);
	}

	public boolean isInZone(final Zone.ZoneType type)
	{
		if(_zones == null)
			return false;
		for(final Zone z : _zones)
			if(z != null && z.getType() == type)
				return true;
		return false;
	}

	public boolean isInZone(final Zone zone)
	{
		if(_zones == null)
			return false;
		for(final Zone z : _zones)
			if(z == zone)
				return true;
		return false;
	}

	public boolean restrictSkillZone(final int id)
	{
		if(_zones == null)
			return false;
		for(final Zone z : _zones)
			if(z.restrictSkill(id))
				return true;
		return false;
	}

	public boolean restrictEquipZone(final int id)
	{
		if(_zones == null)
			return false;
		for(final Zone z : _zones)
			if(z.restrictEquip(id))
				return true;
		return false;
	}

	public Zone getZone(final Zone.ZoneType type)
	{
		if(_zones == null)
			return null;
		for(final Zone z : _zones)
			if(z != null && z.getType() == type)
				return z;
		return null;
	}

	public int getZoneIndex(final Zone.ZoneType type)
	{
		if(_zones == null)
			return -1;
		for(final Zone z : _zones)
			if(z != null && z.getType() == type)
				return z.getIndex();
		return -1;
	}

	public boolean isActionBlocked(final String action)
	{
		if(_zones == null)
			return false;
		for(final Zone z : _zones)
			if(z != null && z.getType() == Zone.ZoneType.unblock_actions && z.isActionBlocked(action))
				return false;
		for(final Zone z : _zones)
			if(z != null && z.getType() != Zone.ZoneType.unblock_actions && z.isActionBlocked(action))
				return true;
		return false;
	}

	public void clearTerritories()
	{
		territoriesLock.lock();
		try
		{
			if(_territories != null)
				for(final Territory t : _territories)
					if(t != null)
						t.doLeave(this, false);
			_territories = null;
		}
		finally
		{
			territoriesLock.unlock();
		}
	}

	public WorldRegion getCurrentRegion()
	{
		return _currentRegion;
	}

	public void setCurrentRegion(final WorldRegion region)
	{
		_currentRegion = region;
	}

	public CharacterAI getAI()
	{
		return null;
	}

	public boolean hasAI()
	{
		return false;
	}

	public boolean inObserverMode()
	{
		return false;
	}

	public int getOlympiadObserveId()
	{
		return -1;
	}

	public boolean isInOlympiadMode()
	{
		return false;
	}

	public boolean isInVehicle()
	{
		return false;
	}

	public boolean isFlying()
	{
		return false;
	}

	public double getCollisionRadius()
	{
		_log.warn("getCollisionRadius called directly from GameObject");
		Thread.dumpStack();
		return 0;
	}

	public double getCollisionHeight()
	{
		_log.warn("getCollisionHeight called directly from GameObject");
		Thread.dumpStack();
		return 0;
	}

	public double getCurrentCollisionRadius()
	{
		return getCollisionRadius();
	}

	public double getCurrentCollisionHeight()
	{
		return getCollisionHeight();
	}

	public void setHeading(final int heading)
	{}

	public void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		getListenerEngine().addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		getListenerEngine().removePropertyChangeListener(listener);
	}

	public void addPropertyChangeListener(final String value, final PropertyChangeListener listener)
	{
		getListenerEngine().addPropertyChangeListener(value, listener);
	}

	public void removePropertyChangeListener(final String value, final PropertyChangeListener listener)
	{
		getListenerEngine().removePropertyChangeListener(value, listener);
	}

	public void firePropertyChanged(final String value, final Object oldValue, final Object newValue)
	{
		getListenerEngine().firePropertyChanged(value, this, oldValue, newValue);
	}

	public void firePropertyChanged(final PropertyEvent event)
	{
		getListenerEngine().firePropertyChanged(event);
	}

	public void addProperty(final String property, final Object value)
	{
		getListenerEngine().addProperty(property, value);
	}

	public Object getProperty(final String property)
	{
		return getListenerEngine().getProperty(property);
	}

	public void addMethodInvokeListener(final MethodInvokeListener listener)
	{
		getListenerEngine().addMethodInvokedListener(listener);
	}

	public void addMethodInvokeListener(final String methodName, final MethodInvokeListener listener)
	{
		getListenerEngine().addMethodInvokedListener(methodName, listener);
	}

	public void removeMethodInvokeListener(final MethodInvokeListener listener)
	{
		getListenerEngine().removeMethodInvokedListener(listener);
	}

	public void removeMethodInvokeListener(final String methodName, final MethodInvokeListener listener)
	{
		getListenerEngine().removeMethodInvokedListener(methodName, listener);
	}

	public void fireMethodInvoked(final MethodEvent event)
	{
		getListenerEngine().fireMethodInvoked(event);
	}

	public void fireMethodInvoked(final String methodName, final Object[] args)
	{
		getListenerEngine().fireMethodInvoked(methodName, this, args);
	}

	public ListenerEngine<GameObject> getListenerEngine()
	{
		if(listenerEngine == null)
			listenerEngine = new DefaultListenerEngine<GameObject>(this);
		return listenerEngine;
	}

	public boolean isCreature()
	{
		return false;
	}

	public boolean isPlayer()
	{
		return false;
	}

	public boolean isPet()
	{
		return false;
	}

	public boolean isSummon()
	{
		return false;
	}

	public boolean isServitor()
	{
		return false;
	}

	public boolean isDoor()
	{
		return false;
	}

	public boolean isMonster()
	{
		return false;
	}

	public boolean isNpc()
	{
		return false;
	}

	public boolean isRaid()
	{
		return false;
	}

	public boolean isChampion()
	{
		return false;
	}

	public boolean isDmg()
	{
		return false;
	}

	public boolean isMinion()
	{
		return false;
	}

	public boolean isPlayable()
	{
		return false;
	}

	public boolean isBoss()
	{
		return false;
	}

	public boolean isRB()
	{
		return false;
	}

	public boolean isEpicBoss()
	{
		return false;
	}

	public boolean isArtefact()
	{
		return false;
	}

	public boolean isSiegeGuard()
	{
		return false;
	}

	public boolean isVehicle()
	{
		return false;
	}

	public boolean isItem()
	{
		return false;
	}

	public boolean isBox()
	{
		return false;
	}

	public boolean isChest()
	{
		return false;
	}

	public boolean isGuard()
	{
		return false;
	}

	public boolean isVIP()
	{
		return false;
	}

	public String getName()
	{
		return getClass().getSimpleName() + ":" + _objectId;
	}

	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		return Collections.emptyList();
	}

	public List<L2GameServerPacket> deletePacketList()
	{
		return Collections.singletonList(new DeleteObject(this));
	}

	@Override
	public void addEvent(final GlobalEvent event)
	{
		event.onAddEvent(this);
		super.addEvent(event);
	}

	@Override
	public void removeEvent(final GlobalEvent event)
	{
		event.onRemoveEvent(this);
		super.removeEvent(event);
	}

	@Override
	public boolean equals(final Object obj)
	{
		return obj == this || obj != null && obj.getClass() == getClass() && ((GameObject) obj).getObjectId() == getObjectId();
	}

	public void enableAI()
	{
		//
	}

	public void disableAI()
	{
		//
	}

	public boolean isStaticObject()
	{
		return false;
	}

	protected Shape makeGeoShape()
	{
		return null;
	}

	@Override
	public Shape getGeoShape()
	{
		return _geoShape;
	}

	public void setGeoShape(Shape shape)
	{
		_geoShape = shape;
	}

	@Override
	public TIntObjectMap<ByteObjectPair<CeilGeoControlType>> getGeoAround()
	{
		return _geoAround;
	}

	@Override
	public void setGeoAround(TIntObjectMap<ByteObjectPair<CeilGeoControlType>> value)
	{
		_geoAround = value;
	}

	protected boolean isGeoControlEnabled()
	{
		return false;
	}

	protected final void refreshGeoControl()
	{
		_geoLock.lock();
		try
		{
			deactivateGeoControl();
			setGeoAround(null);
			setGeoShape(null);
			activateGeoControl();
		}
		finally
		{
			_geoLock.unlock();
		}
	}

	public final boolean isGeoControlActivated()
	{
		return _geoControlIndex > 0;
	}

	public final boolean activateGeoControl()
	{
		if(!Config.ALLOW_GEODATA)
			return true;

		_geoLock.lock();
		try
		{
			if(!isGeoControlEnabled())
				return false;

			if(isGeoControlActivated())
				return false;

			if(!isVisible())
				return false;

			if(getGeoShape() == null)
			{
				Shape shape = makeGeoShape();
				if(shape == null)
					return false;

				setGeoShape(shape);
			}

			int geoIndex = getGeoIndex();

			if(!GeoEngine.applyGeoControl(this, geoIndex))
				return false;

			_geoControlIndex = geoIndex;
			return true;
		}
		finally
		{
			_geoLock.unlock();
		}
	}

	public final boolean deactivateGeoControl()
	{
		if(!Config.ALLOW_GEODATA)
			return true;

		_geoLock.lock();
		try
		{
			if(!isGeoControlActivated())
				return false;

			if(!GeoEngine.returnGeoControl(this))
				return false;

			_geoControlIndex = 0;
			return true;
		}
		finally
		{
			_geoLock.unlock();
		}
	}

	@Override
	public final int getGeoControlIndex() {
		return _geoControlIndex;
	}

	@Override
	public boolean isHollowGeo()
	{
		return true;
	}
}
