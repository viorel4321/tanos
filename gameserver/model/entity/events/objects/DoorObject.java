package l2s.gameserver.model.entity.events.objects;

import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.tables.DoorTable;

public class DoorObject implements SpawnableObject, InitableObject
{
	private int _id;
	private DoorInstance _door;
	private boolean _weak;

	public DoorObject(final int id)
	{
		_id = id;
	}

	@Override
	public void initObject(final GlobalEvent e)
	{
		_door = DoorTable.getInstance().getDoor(_id);
	}

	@Override
	public void spawnObject(final GlobalEvent event)
	{
		refreshObject(event);
	}

	@Override
	public void despawnObject(final GlobalEvent event)
	{
		refreshObject(event);
	}

	@Override
	public void refreshObject(final GlobalEvent event)
	{
		if(!event.isInProgress())
			_door.removeEvent(event);
		else
			_door.addEvent(event);
		if(_door.getCurrentHp() <= 0.0)
		{
			_door.decayMe();
			_door.spawnMe();
		}
		_door.setCurrentHp(_door.getMaxHp() * (isWeak() ? 0.5 : 1.0), true);
		close(event);
	}

	public void res()
	{
		if(_door.getCurrentHp() < 1.0)
		{
			_door.setCurrentHp(_door.getMaxHp(), false);
			_door.decayMe();
			_door.spawnMe();
		}
		else
			_door.setCurrentHp(_door.getMaxHp(), true);
	}

	public int getUId()
	{
		return _door.getDoorId();
	}

	public int getUpgradeValue()
	{
		return _door.getUpgradeHp();
	}

	public void setUpgradeValue(final GlobalEvent event, final int val)
	{
		_door.setUpgradeHp(val);
		refreshObject(event);
	}

	public void open(final GlobalEvent e)
	{
		_door.openMe();
	}

	public void close(final GlobalEvent e)
	{
		_door.closeMe();
	}

	public DoorInstance getDoor()
	{
		return _door;
	}

	public boolean isWeak()
	{
		return _weak;
	}

	public void setWeak(final boolean weak)
	{
		_weak = weak;
	}
}
