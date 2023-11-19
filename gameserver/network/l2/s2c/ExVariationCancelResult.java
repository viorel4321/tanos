package l2s.gameserver.network.l2.s2c;

public class ExVariationCancelResult extends L2GameServerPacket
{
	private int _unk1;
	private int _unk2;

	public ExVariationCancelResult(final int result)
	{
		_unk1 = 1;
		_unk2 = result;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(87);
		writeD(_unk1);
		writeD(_unk2);
	}
}
