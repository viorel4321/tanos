package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class MoveToPawn extends L2GameServerPacket
{
	private int _charObjId;
	private int _targetId;
	private int _distance;
	private int _x;
	private int _y;
	private int _z;

	public MoveToPawn(final Creature cha, final Creature target, final int distance)
	{
		if(cha == target)
		{
			_charObjId = 0;
			return;
		}
		if(target == null)
		{
			_charObjId = 0;
			return;
		}
		_charObjId = cha.getObjectId();
		_targetId = target.getObjectId();
		_distance = distance;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		if(_charObjId == 0)
			return;
		writeC(96);
		writeD(_charObjId);
		writeD(_targetId);
		writeD(_distance);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
