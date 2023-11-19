package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.model.instances.StaticObjectInstance;

public class StaticObject extends L2GameServerPacket
{
	private int _id;
	private int obj_id;

	public StaticObject(StaticObjectInstance staticObject)
	{
		_id = staticObject.getUId();
		obj_id = staticObject.getObjectId();
	}

	public StaticObject(final DoorInstance door)
	{
		_id = door.getDoorId();
		obj_id = door.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(153);
		writeD(_id);
		writeD(obj_id);
	}
}
