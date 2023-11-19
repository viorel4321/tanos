package l2s.gameserver.model.entity.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;

import org.napile.primitive.pair.IntObjectPair;
import org.napile.primitive.Containers;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import l2s.commons.collections.MultiValueSet;
import l2s.commons.listener.Listener;
import l2s.commons.listener.ListenerList;
import l2s.commons.logging.LoggerObject;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.listener.events.OnStartStopListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.base.RestartType;
import l2s.gameserver.model.entity.events.objects.DoorObject;
import l2s.gameserver.model.entity.events.objects.InitableObject;
import l2s.gameserver.model.entity.events.objects.SpawnableObject;
import l2s.gameserver.model.entity.events.objects.ZoneObject;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.TimeUtils;

public abstract class GlobalEvent extends LoggerObject
{
	public static final String EVENT = "event";
	protected final IntObjectMap<List<EventAction>> _onTimeActions;
	protected final List<EventAction> _onStartActions;
	protected final List<EventAction> _onStopActions;
	protected final List<EventAction> _onInitActions;
	protected final Map<Object, List<Object>> _objects;
	protected final int _id;
	protected final String _name;
	protected final ListenerListImpl _listenerList;
	protected IntObjectMap<ItemInstance> _banishedItems;
	private List<Future<?>> _tasks;

	protected GlobalEvent(final MultiValueSet<String> set)
	{
		this(set.getInteger("id"), set.getString("name"));
	}

	protected GlobalEvent(final int id, final String name)
	{
		_onTimeActions = new TreeIntObjectMap<List<EventAction>>();
		_onStartActions = new ArrayList<EventAction>(0);
		_onStopActions = new ArrayList<EventAction>(0);
		_onInitActions = new ArrayList<EventAction>(0);
		_objects = new HashMap<Object, List<Object>>(0);
		_listenerList = new ListenerListImpl();
		_banishedItems = Containers.emptyIntObjectMap();
		_tasks = null;
		_id = id;
		_name = name;
	}

	public void initEvent()
	{
		callActions(_onInitActions);
		reCalcNextTime(true);
		printInfo();
	}

	public void startEvent()
	{
		callActions(_onStartActions);
		_listenerList.onStart();
	}

	public void stopEvent()
	{
		callActions(_onStopActions);
		_listenerList.onStop();
	}

	public void printInfo()
	{
		final long startSiegeMillis = startTimeMillis();
		if(startSiegeMillis == 0L)
			this.info(getName() + " time - undefined");
		else
			this.info(getName() + " time - " + TimeUtils.toSimpleFormat(startSiegeMillis));
	}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + "[" + getId() + ";" + getName() + "]";
	}

	protected void callActions(final List<EventAction> actions)
	{
		for(final EventAction action : actions)
			action.call(this);
	}

	public void addOnStartActions(final List<EventAction> start)
	{
		_onStartActions.addAll(start);
	}

	public void addOnStopActions(final List<EventAction> start)
	{
		_onStopActions.addAll(start);
	}

	public void addOnInitActions(final List<EventAction> start)
	{
		_onInitActions.addAll(start);
	}

	public void addOnTimeAction(final int time, final EventAction action)
	{
		final List<EventAction> list = _onTimeActions.get(time);
		if(list != null)
			list.add(action);
		else
		{
			final List<EventAction> actions = new ArrayList<EventAction>(1);
			actions.add(action);
			_onTimeActions.put(time, actions);
		}
	}

	public void addOnTimeActions(final int time, final List<EventAction> actions)
	{
		if(actions.isEmpty())
			return;
		final List<EventAction> list = _onTimeActions.get(time);
		if(list != null)
			list.addAll(actions);
		else
			_onTimeActions.put(time, new ArrayList<EventAction>(actions));
	}

	public void timeActions(final int time)
	{
		final List<EventAction> actions = _onTimeActions.get(time);
		if(actions == null)
		{
			this.info("Undefined time : " + time);
			return;
		}
		callActions(actions);
	}

	public int[] timeActions()
	{
		return _onTimeActions.keySet().toArray();
	}

	public synchronized void registerActions()
	{
		final long t = startTimeMillis();
		if(t == 0L)
			return;
		if(_tasks == null)
			_tasks = new ArrayList<Future<?>>(_onTimeActions.size());
		final long c = System.currentTimeMillis();
		for(final int key : _onTimeActions.keySet().toArray())
		{
			final long time = t + key * 1000L;
			final EventTimeTask wrapper = new EventTimeTask(this, key);
			if(time <= c)
				ThreadPoolManager.getInstance().execute(wrapper);
			else
				_tasks.add(ThreadPoolManager.getInstance().schedule(wrapper, time - c));
		}
	}

	public synchronized void clearActions()
	{
		if(_tasks == null)
			return;
		for(final Future<?> f : _tasks)
			f.cancel(false);
		_tasks.clear();
	}

	//===============================================================================================================
	//												Objects
	//===============================================================================================================

	public boolean containsObjects(Object name)
	{
		return _objects.get(name) != null;
	}

	@SuppressWarnings("unchecked")
	public <O> List<O> getObjects(Object name)
	{
		List<Object> objects = _objects.get(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	@SuppressWarnings("unchecked")
	public <O> O getFirstObject(Object name)
	{
		List<Object> objects = getObjects(name);
		return objects.size() > 0 ? (O) objects.get(0) : null;
	}

	public void addObject(Object name, Object object)
	{
		if(object == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.add(object);
		else
		{
			list = new CopyOnWriteArrayList<Object>();
			list.add(object);
			_objects.put(name, list);
		}
	}

	public void removeObject(Object name, Object o)
	{
		if(o == null)
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.remove(o);
	}

	@SuppressWarnings("unchecked")
	public <O> List<O> removeObjects(Object name)
	{
		List<Object> objects = _objects.remove(name);
		return objects == null ? Collections.<O>emptyList() : (List<O>)objects;
	}

	public void addObjects(Object name, List<?> objects)
	{
		if(objects.isEmpty())
			return;

		List<Object> list = _objects.get(name);
		if(list != null)
			list.addAll(objects);
		else
			_objects.put(name, new CopyOnWriteArrayList<Object>(objects));
	}


	public Map<Object, List<Object>> getObjects()
	{
		return _objects;
	}

	public void spawnAction(final String name, final boolean spawn)
	{
		final List<Serializable> objects = this.getObjects(name);
		if(objects.isEmpty())
		{
			this.info("Undefined objects: " + name);
			return;
		}
		for(final Serializable object : objects)
			if(object instanceof SpawnableObject)
				if(spawn)
					((SpawnableObject) object).spawnObject(this);
				else
					((SpawnableObject) object).despawnObject(this);
	}

	public void doorAction(final String name, final boolean open)
	{
		final List<Serializable> objects = this.getObjects(name);
		if(objects.isEmpty())
		{
			this.info("Undefined objects: " + name);
			return;
		}
		for(final Serializable object : objects)
			if(object instanceof DoorObject)
				if(open)
					((DoorObject) object).open(this);
				else
					((DoorObject) object).close(this);
	}

	public void zoneAction(final String name, final boolean active)
	{
		final List<Serializable> objects = this.getObjects(name);
		if(objects.isEmpty())
		{
			if(!name.equals("bought_zones"))
				this.info("Undefined objects: " + name);
			return;
		}
		for(final Serializable object : objects)
			if(object instanceof ZoneObject)
				((ZoneObject) object).setActive(active, this);
	}

	public void initAction(final String name)
	{
		final List<Serializable> objects = this.getObjects(name);
		if(objects.isEmpty())
		{
			this.info("Undefined objects: " + name);
			return;
		}
		for(final Serializable object : objects)
			if(object instanceof InitableObject)
				((InitableObject) object).initObject(this);
	}

	public void action(final String name, final boolean start)
	{
		if(name.equalsIgnoreCase("event"))
			if(start)
				startEvent();
			else
				stopEvent();
	}

	public void refreshAction(final String name)
	{
		final List<Serializable> objects = this.getObjects(name);
		if(objects.isEmpty())
		{
			this.info("Undefined objects: " + name);
			return;
		}
		for(final Serializable object : objects)
			if(object instanceof SpawnableObject)
				((SpawnableObject) object).refreshObject(this);
	}

	public abstract void reCalcNextTime(final boolean p0);

	protected abstract long startTimeMillis();

	public void broadcastToWorld(final IBroadcastPacket packet)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			if(player != null)
				player.sendPacket(packet);
	}

	public void broadcastToWorld(final L2GameServerPacket packet)
	{
		for(final Player player : GameObjectsStorage.getPlayers())
			if(player != null)
				player.sendPacket(packet);
	}

	public int getId()
	{
		return _id;
	}

	public String getName()
	{
		return _name;
	}

	public GameObject getCenterObject()
	{
		return null;
	}

	public int getRelation(final Player thisPlayer, final Player target, final int oldRelation)
	{
		return oldRelation;
	}

	public int getUserRelation(final Player thisPlayer, final int oldRelation)
	{
		return oldRelation;
	}

	public void checkRestartLocs(final Player player, final Map<RestartType, Boolean> r)
	{}

	public Location getRestartLoc(final Player player, final RestartType type)
	{
		return null;
	}

	public boolean canAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force, final boolean nextAttackCheck)
	{
		return false;
	}

	public SystemMessage checkForAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force)
	{
		return null;
	}

	public boolean isInProgress()
	{
		return false;
	}

	public boolean isParticle(final Player player)
	{
		return false;
	}

	public void announce(final int a)
	{
		throw new UnsupportedOperationException();
	}

	public void teleportPlayers(final String teleportWho)
	{
		throw new UnsupportedOperationException();
	}

	public boolean ifVar(final String name)
	{
		throw new UnsupportedOperationException();
	}

	public List<Player> itemObtainPlayers()
	{
		throw new UnsupportedOperationException();
	}

	public void giveItem(final Player player, final int itemId, final long count)
	{
		Functions.addItem(player, itemId, count);
	}

	public List<Player> broadcastPlayers(final int range)
	{
		throw new UnsupportedOperationException(this.getClass().getName() + " not implemented broadcastPlayers");
	}

	public boolean canResurrect(final Player resurrectPlayer, final Creature creature, final boolean force)
	{
		return true;
	}

	public void onAddEvent(final GameObject o)
	{}

	public void onRemoveEvent(final GameObject o)
	{}

	public void addBanishItem(final ItemInstance item)
	{
		if(_banishedItems.isEmpty())
			_banishedItems = new CHashIntObjectMap<ItemInstance>();
		_banishedItems.put(item.getObjectId(), item);
	}

	public void removeBanishItems()
	{
		Iterator<IntObjectPair<ItemInstance>> iterator = _banishedItems.entrySet().iterator();
		while(iterator.hasNext())
		{
			IntObjectPair<ItemInstance> entry = iterator.next();
			iterator.remove();

			ItemInstance item = ItemInstance.restoreFromDb(entry.getKey(), false);
			if(item != null)
			{
				if(item.getOwnerId() > 0)
				{
					GameObject object = GameObjectsStorage.findObject(item.getOwnerId());
					if(object != null && object.isPlayable())
					{
						((Playable) object).getInventory().destroyItem(item);
						object.getPlayer().sendPacket(SystemMessage.removeItems(item));
					}
				}
			}
			else
				item = entry.getValue();

			item.deleteMe();
		}
	}

	public void addListener(final Listener<GlobalEvent> l)
	{
		_listenerList.add(l);
	}

	public void removeListener(final Listener<GlobalEvent> l)
	{
		_listenerList.remove(l);
	}

	public void cloneTo(final GlobalEvent e)
	{
		for(final EventAction a : _onInitActions)
			e._onInitActions.add(a);
		for(final EventAction a : _onStartActions)
			e._onStartActions.add(a);
		for(final EventAction a : _onStopActions)
			e._onStopActions.add(a);
		for(final IntObjectPair<List<EventAction>> entry : _onTimeActions.entrySet())
			e.addOnTimeActions(entry.getKey(), entry.getValue());
	}

	private class ListenerListImpl extends ListenerList<GlobalEvent>
	{
		public void onStart()
		{
			for(final Listener<GlobalEvent> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStart(GlobalEvent.this);
		}

		public void onStop()
		{
			for(final Listener<GlobalEvent> listener : getListeners())
				if(OnStartStopListener.class.isInstance(listener))
					((OnStartStopListener) listener).onStop(GlobalEvent.this);
		}
	}
}
