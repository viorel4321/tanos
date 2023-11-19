package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.instances.DoorInstance;

public class DoorStatusUpdate extends L2GameServerPacket
{
	private int obj_id;
	private int door_id;
	private int _opened;
	private int dmg;
	private int isenemy;
	private int curHp;
	private int maxHp;

	public DoorStatusUpdate(final DoorInstance door, final boolean enemy)
	{
		obj_id = door.getObjectId();
		door_id = door.getDoorId();
		_opened = door.isOpen() ? 0 : 1;
		dmg = door.getDamage();
		isenemy = enemy ? 1 : 0;
		curHp = (int) door.getCurrentHp();
		maxHp = door.getMaxHp();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(77);
		writeD(obj_id);
		writeD(_opened);
		writeD(dmg);
		writeD(isenemy);
		writeD(door_id);
		writeD(maxHp);
		writeD(curHp);
	}
}
