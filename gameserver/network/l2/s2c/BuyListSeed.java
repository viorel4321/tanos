package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.TradeController;
import l2s.gameserver.model.TradeItem;

public final class BuyListSeed extends L2GameServerPacket
{
	private int _manorId;
	private List<TradeItem> _list;
	private int _money;

	public BuyListSeed(final TradeController.NpcTradeList list, final int manorId, final int currentMoney)
	{
		_list = new ArrayList<TradeItem>();
		_money = currentMoney;
		_manorId = manorId;
		_list = list.getItems();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(232);
		writeD(_money);
		writeD(_manorId);
		if(_list != null && !_list.isEmpty())
		{
			writeH(_list.size());
			for(final TradeItem item : _list)
			{
				writeH(4);
				writeD(0);
				writeD(item.getItemId());
				writeD(item.getCount());
				writeH(4);
				writeH(0);
				writeD(item.getOwnersPrice());
			}
		}
	}
}
