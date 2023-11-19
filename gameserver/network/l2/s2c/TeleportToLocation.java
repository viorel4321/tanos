package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.GameObject;

public class TeleportToLocation extends L2GameServerPacket
{
	private int _targetId;
	private int _x;
	private int _y;
	private int _z;

	public TeleportToLocation(final GameObject cha, final int x, final int y, final int z)
	{
		_targetId = cha.getObjectId();
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(40);
		writeD(_targetId);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
