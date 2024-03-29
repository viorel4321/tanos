package l2s.gameserver.network.l2.s2c;

public class ExConfirmVariationItem extends L2GameServerPacket
{
	private int _itemObjId;
	private int _unk1;
	private int _unk2;

	public ExConfirmVariationItem(final int itemObjId)
	{
		_itemObjId = itemObjId;
		_unk1 = 1;
		_unk2 = 1;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(82);
		writeD(_itemObjId);
		writeD(_unk1);
		writeD(_unk2);
	}
}
