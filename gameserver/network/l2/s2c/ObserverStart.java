package l2s.gameserver.network.l2.s2c;

public class ObserverStart extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;

	public ObserverStart(final int x, final int y, final int z)
	{
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(223);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeC(0);
		writeC(192);
		writeC(0);
	}
}
