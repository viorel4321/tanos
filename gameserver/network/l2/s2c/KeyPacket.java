package l2s.gameserver.network.l2.s2c;

public class KeyPacket extends L2GameServerPacket
{
	private byte[] _data;

	public KeyPacket(final byte[] data)
	{
		_data = data;
	}

	@Override
	public void writeImpl()
	{
		writeC(0);
		if(_data == null)
			writeC(0);
		else
		{
			writeC(1);
			writeB(_data);
			writeD(1);
			writeD(1);
		}
	}
}
