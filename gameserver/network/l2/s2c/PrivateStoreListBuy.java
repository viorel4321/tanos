package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class PrivateStoreListBuy extends L2GameServerPacket
{
	private int buyer_id;
	private int seller_adena;
	private ConcurrentLinkedQueue<TradeItem> _buyerslist;

	public PrivateStoreListBuy(final Player seller, final Player storePlayer)
	{
		int currecyId = storePlayer.getPrivateStoreCurrecy();
		if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
			seller_adena = seller.getAdena();
		else
			seller_adena = (int) Functions.getItemCount(seller, currecyId);

		buyer_id = storePlayer.getObjectId();
		final ConcurrentLinkedQueue<ItemInstance> sellerItems = seller.getInventory().getItemsList();
		(_buyerslist = new ConcurrentLinkedQueue<TradeItem>()).addAll(storePlayer.getBuyList());
		final List<Integer> ids = new ArrayList<Integer>();
		for(final TradeItem buyListItem : _buyerslist)
		{
			buyListItem.setCurrentValue(0);
			for(final ItemInstance sellerItem : sellerItems)
			{
				if(sellerItem.getItemId() == buyListItem.getItemId() && !ids.contains(sellerItem.getObjectId()) && sellerItem.canBeTraded(seller))
				{
					buyListItem.setCurrentValue(Math.min(buyListItem.getCount(), sellerItem.getIntegerLimitedCount()));
					ids.add(sellerItem.getObjectId());
					break;
				}
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(184);
		writeD(buyer_id);
		writeD(seller_adena);
		writeD(_buyerslist.size());
		for(final TradeItem buyersitem : _buyerslist)
		{
			final ItemTemplate tmp = ItemTable.getInstance().getTemplate(buyersitem.getItemId());
			writeD(buyersitem.getObjectId());
			writeD(buyersitem.getItemId());
			writeH(buyersitem.getEnchantLevel());
			writeD(buyersitem.getCurrentValue());
			writeD(tmp.getReferencePrice());
			writeH(0);
			writeD(tmp.getBodyPart());
			writeH(tmp.getType2ForPackets());
			writeD(buyersitem.getOwnersPrice());
			writeD(buyersitem.getCount());
		}
	}
}
