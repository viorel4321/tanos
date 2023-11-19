package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;

public class ExConfirmCancelItem extends L2GameServerPacket
{
	private final int _itemObjId;
	private final int _itemId;
	private final int _itemAug1;
	private final int _itemAug2;
	private final int _price;

	public ExConfirmCancelItem(final ItemInstance item, final int price)
	{
		_itemObjId = item.getObjectId();
		_itemId = item.getItemId();
		_price = price;
		_itemAug1 = item.getVariation1Id();
		_itemAug2 = item.getVariation2Id();
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(86);
		writeD(_itemObjId);
		writeD(_itemId);
		writeD(_itemAug1);
		writeD(_itemAug2);
		writeQ((long) _price);
		writeD(1);
	}
}
