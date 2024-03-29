package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class StopMove extends L2GameServerPacket
{
	private final int _objectId;
	private final int _x;
	private final int _y;
	private final int _z;
	private final int _heading;

	public StopMove(final Creature cha)
	{
		_objectId = cha.getObjectId();
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_heading = cha.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(71);
		writeD(_objectId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(_heading);
	}
}
