package l2s.gameserver.network.l2.s2c;

public class StartRotation extends L2GameServerPacket
{
	private int _charId;
	private int _degree;
	private int _side;
	private int _speed;

	public StartRotation(final int objId, final int degree, final int side, final int speed)
	{
		_charId = objId;
		_degree = degree;
		_side = side;
		_speed = speed;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(98);
		writeD(_charId);
		writeD(_degree);
		writeD(_side);
		writeD(_speed);
	}
}
