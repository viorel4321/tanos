package l2s.gameserver.network.l2.s2c;

public class StopRotation extends L2GameServerPacket
{
	private int _charId;
	private int _degree;
	private int _speed;

	public StopRotation(final int objId, final int degree, final int speed)
	{
		_charId = objId;
		_degree = degree;
		_speed = speed;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(99);
		writeD(_charId);
		writeD(_degree);
		writeD(_speed);
		writeC(0);
	}
}
