package l2s.gameserver.network.l2.s2c;

public class ExPledgeCrestLarge extends L2GameServerPacket
{
	private int _crestId;
	private byte[] _data;

	public ExPledgeCrestLarge(final int crestId, final byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(40);
		writeD(0);
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}
}
