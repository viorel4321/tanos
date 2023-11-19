package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class PrivateStoreManageListBuy extends L2GameServerPacket
{
	private List<BuyItemInfo> buylist;
	private int buyer_id;
	private int buyer_adena;
	private TradeList _list;

	public PrivateStoreManageListBuy(final Player buyer)
	{
		buylist = new ArrayList<BuyItemInfo>();
		buyer_id = buyer.getObjectId();

		int currecyId = buyer.getPrivateStoreCurrecy();
		if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
			buyer_adena = buyer.getAdena();
		else
			buyer_adena = (int) Functions.getItemCount(buyer, currecyId);
	
		for(final TradeItem e : buyer.getBuyList())
		{
			final int _id = e.getItemId();
			final ItemTemplate tempItem = ItemTable.getInstance().getTemplate(_id);
			if(tempItem == null)
				continue;
			final int count = e.getCount();
			final int store_price = e.getStorePrice();
			final int body_part = tempItem.getBodyPart();
			final int type2 = tempItem.getType2();
			final int owner_price = e.getOwnersPrice();
			final int enchant = e.getEnchantLevel();
			buylist.add(new BuyItemInfo(_id, count, store_price, body_part, type2, owner_price, enchant));
		}
		_list = new TradeList(0);
		for(final ItemInstance item : buyer.getInventory().getItems())
			if(item != null && item.canBeTraded(buyer) && item.getTemplate().getType2() != 4)
			{
				for(final TradeItem ti : buyer.getBuyList())
					if(ti.getItemId() == item.getItemId() && ti.getEnchantLevel() == item.getEnchantLevel())
						continue;
				_list.addItem(item);
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(183);
		writeD(buyer_id);
		writeD(buyer_adena);
		writeD(_list.getItems().size());
		for(final ItemInstance temp : _list.getItems())
		{
			writeD(temp.getItemId());
			writeH(temp.getEnchantLevel());
			writeD(temp.getIntegerLimitedCount());
			writeD(temp.getPriceToSell());
			writeH(0);
			writeD(temp.getBodyPart());
			writeH(temp.getTemplate().getType2());
		}
		writeD(buylist.size());
		for(final BuyItemInfo e : buylist)
		{
			writeD(e._id);
			writeH(e.enchant);
			writeD(e.count);
			writeD(e.store_price);
			writeH(0);
			writeD(e.body_part);
			writeH(e.type2);
			writeD(e.owner_price);
			writeD(e.store_price);
		}
	}

	static class BuyItemInfo
	{
		public int _id;
		public int count;
		public int store_price;
		public int body_part;
		public int type2;
		public int owner_price;
		public int enchant;

		public BuyItemInfo(final int __id, final int _count, final int _store_price, final int _body_part, final int _type2, final int _owner_price, int _enchant)
		{
			_id = __id;
			count = _count;
			store_price = _store_price;
			body_part = _body_part;
			type2 = _type2;
			owner_price = _owner_price;
			_enchant = enchant;
		}
	}
}
