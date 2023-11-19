package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.DoorInstance;

public class DoorInfo extends L2GameServerPacket
{
	private int obj_id;
	private int door_id;
	private int view_hp;

	public DoorInfo(final DoorInstance door)
	{
		obj_id = door.getObjectId();
		door_id = door.getDoorId();
		view_hp = door.isHPVisible() ? 1 : 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(76);
		writeD(obj_id);
		writeD(door_id);
		writeD(view_hp);
	}
}
