package l2s.gameserver.network.l2.s2c;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class PrivateStoreManageList extends L2GameServerPacket
{
	private int seller_id;
	private int seller_adena;
	private boolean _package;
	private ConcurrentLinkedQueue<TradeItem> _sellList;
	private ConcurrentLinkedQueue<TradeItem> _haveList;

	public PrivateStoreManageList(final Player seller, final boolean pkg)
	{
		_package = false;
		seller_id = seller.getObjectId();

		int currecyId = seller.getPrivateStoreCurrecy();
		if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
			seller_adena = seller.getAdena();
		else
			seller_adena = (int) Functions.getItemCount(seller, currecyId);

		_package = pkg;
		_sellList = new ConcurrentLinkedQueue<TradeItem>();
		for(final TradeItem i : seller.getSellList())
		{
			final ItemInstance inst = seller.getInventory().getItemByObjectId(i.getObjectId());
			if(i.getCount() > 0 && inst != null)
			{
				if(inst.isWear())
					continue;
				if(inst.getIntegerLimitedCount() < i.getCount())
					i.setCount(inst.getIntegerLimitedCount());
				_sellList.add(i);
			}
		}
		final TradeList _list = new TradeList(0);
		for(final ItemInstance item : seller.getInventory().getItemsList())
			if(item != null && item.canBeTraded(seller) && item.getTemplate().getType2() != 4)
				_list.addItem(item);
		_haveList = new ConcurrentLinkedQueue<TradeItem>();
		for(final ItemInstance item : _list.getItems())
		{
			final TradeItem ti = new TradeItem();
			ti.setObjectId(item.getObjectId());
			ti.setItemId(item.getItemId());
			ti.setCount(item.getIntegerLimitedCount());
			ti.setEnchantLevel(item.getEnchantLevel());
			_haveList.add(ti);
		}
		if(_sellList.size() > 0)
			for(final TradeItem itemOnSell : _sellList)
			{
				_haveList.remove(itemOnSell);
				boolean added = false;
				for(final TradeItem itemInInv : _haveList)
					if(itemInInv.getObjectId() == itemOnSell.getObjectId())
					{
						added = true;
						itemOnSell.setCount(Math.min(itemOnSell.getCount(), itemInInv.getCount()));
						if(itemOnSell.getCount() == itemInInv.getCount())
						{
							_haveList.remove(itemInInv);
							break;
						}
						if(itemOnSell.getCount() > 0)
						{
							itemInInv.setCount(itemInInv.getCount() - itemOnSell.getCount());
							break;
						}
						_sellList.remove(itemOnSell);
						break;
					}
				if(!added)
					_sellList.remove(itemOnSell);
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(154);
		writeD(seller_id);
		writeD(_package ? 1 : 0);
		writeD(seller_adena);
		writeD(_haveList.size());
		for(final TradeItem temp : _haveList)
		{
			final ItemTemplate tempItem = ItemTable.getInstance().getTemplate(temp.getItemId());
			writeD(tempItem.getType2());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(0);
			writeH(temp.getEnchantLevel());
			writeH(0);
			writeD(tempItem.getBodyPart());
			writeD(tempItem.getReferencePrice());
		}
		writeD(_sellList.size());
		for(final TradeItem temp2 : _sellList)
		{
			final ItemTemplate tempItem = ItemTable.getInstance().getTemplate(temp2.getItemId());
			writeD(tempItem.getType2());
			writeD(temp2.getObjectId());
			writeD(temp2.getItemId());
			writeD(temp2.getCount());
			writeH(0);
			writeH(temp2.getEnchantLevel());
			writeH(0);
			writeD(tempItem.getBodyPart());
			writeD(temp2.getOwnersPrice());
			writeD(temp2.getStorePrice());
		}
	}
}
