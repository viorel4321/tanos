package l2s.gameserver.network.l2.s2c;

public class ExVariationResult extends L2GameServerPacket
{
	private int _stat12;
	private int _stat34;
	private int _unk3;

	public ExVariationResult(final int unk1, final int unk2, final int unk3)
	{
		_stat12 = unk1;
		_stat34 = unk2;
		_unk3 = unk3;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(85);
		writeD(_stat12);
		writeD(_stat34);
		writeD(_unk3);
	}
}
