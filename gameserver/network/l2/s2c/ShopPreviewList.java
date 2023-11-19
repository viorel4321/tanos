package l2s.gameserver.network.l2.s2c;

import java.util.List;

import l2s.gameserver.TradeController;
import l2s.gameserver.model.TradeItem;

public class ShopPreviewList extends L2GameServerPacket
{
	private int _listId;
	private TradeItem[] _list;
	private int _money;
	private int _expertise;

	public ShopPreviewList(final TradeController.NpcTradeList list, final int currentMoney, final int expertiseIndex)
	{
		_listId = list.getListId();
		final List<TradeItem> lst = list.getItems();
		_list = (TradeItem[]) lst.toArray((Object[]) new TradeItem[lst.size()]);
		_money = currentMoney;
		_expertise = expertiseIndex;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(239);
		writeC(192);
		writeC(19);
		writeC(0);
		writeC(0);
		writeD(_money);
		writeD(_listId);
		int newlength = 0;
		for(final TradeItem item : _list)
			if(item.getItem().getItemGrade().ordinal() <= _expertise && item.getItem().isEquipable())
				++newlength;
		writeH(newlength);
		for(final TradeItem item : _list)
			if(item.getItem().getItemGrade().ordinal() <= _expertise && item.getItem().isEquipable())
			{
				writeD(item.getItemId());
				writeH(item.getItem().getType2());
				writeH(item.getItem().getType1() != 4 ? item.getItem().getBodyPart() : 0);
				writeD(10);
			}
	}
}
