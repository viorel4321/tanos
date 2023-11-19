package l2s.gameserver.network.l2.s2c;

public class ExConfirmVariationGemstone extends L2GameServerPacket
{
	private int _gemstoneObjId;
	private int _unk1;
	private int _gemstoneCount;
	private int _unk2;
	private int _unk3;

	public ExConfirmVariationGemstone(final int gemstoneObjId, final int count)
	{
		_gemstoneObjId = gemstoneObjId;
		_unk1 = 1;
		_gemstoneCount = count;
		_unk2 = 1;
		_unk3 = 1;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(84);
		writeD(_gemstoneObjId);
		writeD(_unk1);
		writeD(_gemstoneCount);
		writeD(_unk2);
		writeD(_unk3);
	}
}
