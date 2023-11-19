package l2s.gameserver.tables;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.GameTimeController;
import l2s.gameserver.data.xml.holder.DoorHolder;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.data.xml.parser.DoorParser;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.listener.actor.door.impl.MasterOnOpenCloseListenerImpl;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.templates.DoorTemplate;

public class DoorTable
{
	private static final Logger _log = LoggerFactory.getLogger(DoorTable.class);

	private final IntObjectMap<DoorInstance> _filledDoors = new HashIntObjectMap<DoorInstance>();
	private final List<DoorInstance> _instanceDoors = new ArrayList<DoorInstance>();

	private static DoorTable _instance;

	public static DoorTable getInstance()
	{
		if(_instance == null)
			_instance = new DoorTable();
		return _instance;
	}

	public DoorTable()
	{
		//
	}

	public void respawn()
	{
		for(final DoorInstance door : _filledDoors.valueCollection())
		{
			if(door != null)
				door.deleteMe();
		}
		fillDoors();
	}

	public void fillDoors()
	{
		for(DoorTemplate template : DoorHolder.getInstance().getDoors().valueCollection())
		{
			DoorInstance door = new DoorInstance(IdFactory.getInstance().getNextId(), template);
			door.setIsInvul(true);
			door.setXYZInvisible(template.getLoc().x, template.getLoc().y, template.getLoc().z);
			door.spawnMe(door.getLoc());

			if(template.isOpened())
				door.openMe();

			putDoor(door);
		}

		for(DoorInstance door : _filledDoors.valueCollection())
		{
			if(door.getTemplate().getMasterDoor() > 0)
			{
				DoorInstance masterDoor = getDoor(door.getTemplate().getMasterDoor());
				masterDoor.addListener(new MasterOnOpenCloseListenerImpl(door));
			}
		}

		_log.info("DoorTable: Filled " + _filledDoors.size() + " doors.");
	}

	public DoorInstance getDoor(int id)
	{
		return _filledDoors.get(id);
	}

	public void putDoor(final DoorInstance door)
	{
		_filledDoors.put(door.getDoorId(), door);
	}

	public void putInstanceDoor(final DoorInstance door)
	{
		_instanceDoors.add(door);
	}

	public void removeDoor(int id)
	{
		_filledDoors.remove(id);
	}

	public DoorInstance[] getDoors()
	{
		return _filledDoors.values(new DoorInstance[_filledDoors.size()]);
	}

	public void reloadDoors()
	{
		for(final DoorInstance door : _filledDoors.valueCollection())
		{
			if(door != null)
				door.deleteMe();
		}
		_filledDoors.clear();
		DoorParser.getInstance().reload();
		fillDoors();
	}
}
