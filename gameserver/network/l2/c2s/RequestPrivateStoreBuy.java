package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestPrivateStoreBuy extends L2GameClientPacket
{
	private int _sellerID;
	private int _count;
	private int[] _items;

	@Override
	public void readImpl()
	{
		_sellerID = readD();
		_count = readD();
		if(_count * 12 > _buf.remaining() || _count > 32767 || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 3];
		for(int i = 0; i < _count; ++i)
		{
			_items[i * 3 + 0] = readD();
			_items[i * 3 + 1] = readD();
			_items[i * 3 + 2] = readD();
			if(_items[i * 3 + 1] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		if(_items == null)
			return;
		final Player buyer = getClient().getActiveChar();
		if(buyer == null)
			return;
		if(buyer.isActionsDisabled())
		{
			buyer.sendActionFailed();
			return;
		}
		if(!Config.ALLOW_PRIVATE_STORE)
		{
			buyer.sendMessage(buyer.isLangRus() ? "\u041f\u0440\u0438\u0432\u0430\u0442\u043d\u0430\u044f \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "Private store disabled.");
			return;
		}
		if(!buyer.getPlayerAccess().UseTrade)
		{
			buyer.sendMessage("You can't use private store.");
			return;
		}
		ConcurrentLinkedQueue<TradeItem> buyerlist = new ConcurrentLinkedQueue<TradeItem>();
		final Player seller = (Player) buyer.getVisibleObject(_sellerID);
		if(seller == null || seller.getPrivateStoreType() != 1 && seller.getPrivateStoreType() != 8 || seller.getDistance3D(buyer) > 150.0)
		{
			buyer.sendActionFailed();
			return;
		}
		if(seller.getTradeList() == null)
		{
			TradeList.cancelStore(seller);
			return;
		}
		final ConcurrentLinkedQueue<TradeItem> sellerlist = seller.getSellList();
		int cost = 0;
		if(seller.getPrivateStoreType() == 8)
		{
			buyerlist = new ConcurrentLinkedQueue<TradeItem>();
			buyerlist.addAll(sellerlist);
			for(final TradeItem ti : buyerlist)
				cost += ti.getOwnersPrice() * ti.getCount();
		}
		else
		{
			for(int i = 0; i < _count; ++i)
			{
				final int objectId = _items[i * 3 + 0];
				final int count = _items[i * 3 + 1];
				final int price = _items[i * 3 + 2];
				for(final TradeItem si : sellerlist)
					if(si.getObjectId() == objectId)
					{
						if(count > si.getCount() || price != si.getOwnersPrice())
						{
							buyer.sendActionFailed();
							return;
						}
						final ItemInstance sellerItem = seller.getInventory().getItemByObjectId(objectId);
						if(sellerItem == null || sellerItem.getIntegerLimitedCount() < count)
						{
							buyer.sendActionFailed();
							return;
						}
						final TradeItem temp = new TradeItem();
						temp.setObjectId(si.getObjectId());
						temp.setItemId(sellerItem.getItemId());
						temp.setCount(count);
						temp.setOwnersPrice(si.getOwnersPrice());
						cost += temp.getOwnersPrice() * temp.getCount();
						buyerlist.add(temp);
					}
			}
		}

		if(cost > Integer.MAX_VALUE || cost < 0)
		{
			buyer.sendActionFailed();
			return;
		}

		int currecyId = seller.getPrivateStoreCurrecy();
		if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
		{
			if(buyer.getAdena() < cost)
			{
				buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
				buyer.sendActionFailed();
				return;
			}
		}
		else
		{
			if(Functions.getItemCount(buyer, currecyId) < cost)
			{
				buyer.sendPacket(Msg.INCORRECT_ITEM_COUNT);
				buyer.sendActionFailed();
				return;
			}
		}

		seller.getTradeList().buySellItems(seller, buyer, buyerlist, seller, sellerlist);
		buyer.sendChanges();
		seller.saveTradeList();
		if(seller.getSellList().isEmpty())
			TradeList.cancelStore(seller);
		seller.sendChanges();
		buyer.sendActionFailed();
	}
}
