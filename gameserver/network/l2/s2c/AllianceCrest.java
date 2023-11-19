package l2s.gameserver.network.l2.s2c;

public class AllianceCrest extends L2GameServerPacket
{
	private int _crestId;
	private byte[] _data;

	public AllianceCrest(final int crestId, final byte[] data)
	{
		_crestId = crestId;
		_data = data;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(174);
		writeD(_crestId);
		writeD(_data.length);
		writeB(_data);
	}
}
