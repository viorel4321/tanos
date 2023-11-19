package l2s.gameserver.network.l2.s2c;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class PrivateStoreListSell extends L2GameServerPacket
{
	private int seller_id;
	private int buyer_adena;
	private final boolean _package;
	private ConcurrentLinkedQueue<TradeItem> _sellList;

	public PrivateStoreListSell(final Player buyer, final Player seller)
	{
		seller_id = seller.getObjectId();

		int currecyId = seller.getPrivateStoreCurrecy();
		if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
			buyer_adena = buyer.getAdena();
		else
			buyer_adena = (int) Functions.getItemCount(buyer, currecyId);

		_package = seller.getPrivateStoreType() == 8;
		_sellList = seller.getSellList();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(155);
		writeD(seller_id);
		writeD(_package ? 1 : 0);
		writeD(buyer_adena);
		writeD(_sellList.size());
		for(final TradeItem ti : _sellList)
		{
			final ItemTemplate tempItem = ItemTable.getInstance().getTemplate(ti.getItemId());
			writeD(tempItem.getType2ForPackets());
			writeD(ti.getObjectId());
			writeD(ti.getItemId());
			writeD(ti.getCount());
			writeH(0);
			writeH(ti.getEnchantLevel());
			writeH(0);
			writeD(tempItem.getBodyPart());
			writeD(ti.getOwnersPrice());
			writeD(ti.getStorePrice());
		}
	}
}
