package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.TradeController;
import l2s.gameserver.model.TradeItem;

public class BuyList extends L2GameServerPacket
{
	private int _listId;
	private List<TradeItem> _list;
	private int _money;
	private double _TaxRate;

	public BuyList(final TradeController.NpcTradeList list, final int currentMoney)
	{
		_TaxRate = 0.0;
		_listId = list.getListId();
		_list = cloneAndFilter(list.getItems());
		_money = currentMoney;
	}

	public BuyList(final TradeController.NpcTradeList list, final int currentMoney, final double taxRate)
	{
		_TaxRate = 0.0;
		_listId = list.getListId();
		_list = cloneAndFilter(list.getItems());
		_money = currentMoney;
		_TaxRate = taxRate;
	}

	protected static List<TradeItem> cloneAndFilter(final List<TradeItem> list)
	{
		if(list == null)
			return null;
		final List<TradeItem> ret = new ArrayList<TradeItem>(list.size());
		for(final TradeItem item : list)
		{
			if(item.getCurrentValue() < item.getCount() && item.getLastRechargeTime() + item.getRechargeTime() <= System.currentTimeMillis() / 60000L)
			{
				item.setLastRechargeTime(item.getLastRechargeTime() + item.getRechargeTime());
				item.setCurrentValue(item.getCount());
			}
			if(item.getCurrentValue() == 0 && item.getCount() != 0)
				continue;
			ret.add(item);
		}
		return ret;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(17);
		writeD(_money);
		writeD(_listId);
		if(_list == null)
			writeH(0);
		else
		{
			writeH(_list.size());
			for(final TradeItem item : _list)
			{
				writeH(item.getItem().getType1());
				writeD(item.getObjectId());
				writeD(item.getItemId());
				writeD(item.getCurrentValue());
				writeH(item.getItem().getType2());
				writeH(0);
				writeD(item.getItem().getBodyPart());
				writeH(item.getEnchantLevel());
				writeH(0);
				writeH(0);
				writeD((int) (item.getOwnersPrice() * (1.0 + _TaxRate)));
			}
		}
	}
}
