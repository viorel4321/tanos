package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestPrivateStoreSell extends L2GameClientPacket
{
	private int _buyerId;
	private int _count;
	private int _itemId;
	private int[] _items;
	private int[] _itemQ;
	private int[] _itemP;
	private int[] _itemE;

	@Override
	public void readImpl()
	{
		_buyerId = readD();
		_count = readD();
		final Player _seller = getClient().getActiveChar();
		if(_seller == null)
			return;
		if(_count * 20 > _buf.remaining() || _count > 32767 || _count < 1)
		{
			_count = 0;
			return;
		}
		_items = new int[_count];
		_itemQ = new int[_count];
		_itemP = new int[_count];
		_itemE = new int[_count];
		for(int i = 0; i < _count; ++i)
		{
			readD();
			_itemId = readD();
			_itemE[i] = readH();
			readH();
			_itemQ[i] = readD();
			_itemP[i] = readD();
			for(final ItemInstance itm : _seller.getInventory().getAllItemsById(_itemId))
				if(!ArrayUtils.contains(_items, itm.getObjectId()) && _itemE[i] == itm.getEnchantLevel())
				{
					_items[i] = itm.getObjectId();
					break;
				}
			if(_itemQ[i] < 1 || _itemP[i] < 1 || ArrayUtils.indexOf(_items, _items[i]) < i)
			{
				_count = 0;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		final Player seller = getClient().getActiveChar();
		if(seller == null || _count == 0)
			return;
		if(seller.isActionsDisabled())
		{
			seller.sendActionFailed();
			return;
		}
		if(!Config.ALLOW_PRIVATE_STORE)
		{
			seller.sendMessage(seller.isLangRus() ? "\u041f\u0440\u0438\u0432\u0430\u0442\u043d\u0430\u044f \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044f \u043e\u0442\u043a\u043b\u044e\u0447\u0435\u043d\u0430." : "Private store disabled.");
			return;
		}
		if(seller.isInStoreMode())
		{
			seller.sendPacket(new SystemMessage(1065));
			return;
		}
		if(seller.isInTrade())
		{
			seller.sendActionFailed();
			return;
		}
		if(seller.isFishing())
		{
			seller.sendPacket(new SystemMessage(1471));
			return;
		}
		if(!seller.getPlayerAccess().UseTrade)
		{
			seller.sendMessage("You can't use private store.");
			return;
		}
		final Player buyer = (Player) seller.getVisibleObject(_buyerId);
		if(buyer == null || buyer.getPrivateStoreType() != 3 || !seller.isInRangeZ(buyer, 150L))
		{
			seller.sendPacket(new SystemMessage(1801));
			seller.sendActionFailed();
			return;
		}
		final ConcurrentLinkedQueue<TradeItem> buyList = buyer.getBuyList();
		if(buyList.isEmpty())
		{
			seller.sendPacket(new SystemMessage(1801));
			seller.sendActionFailed();
			return;
		}
		final int currecyId = buyer.getPrivateStoreCurrecy();
		final ConcurrentLinkedQueue<TradeItem> sellList = new ConcurrentLinkedQueue<TradeItem>();
		int totalCost = 0;
		int slots = 0;
		int weight = 0;
		buyer.getInventory().writeLock();
		seller.getInventory().writeLock();
		try
		{
			loop: for(int i = 0; i < _count; ++i)
			{
				final int objectId = _items[i];
				final int count = _itemQ[i];
				final int price = _itemP[i];
				final int enchant = _itemE[i];
				final ItemInstance item = seller.getInventory().getItemByObjectId(objectId);
				if(item == null || item.getCount() < count)
					break;
				if(!item.canBeTraded(seller))
					break;
				TradeItem si = null;
				for(final TradeItem bi : buyList)
				{
					if(bi.getItemId() == item.getItemId() && bi.getEnchantLevel() == item.getEnchantLevel() && bi.getOwnersPrice() == price)
					{
						if(count > bi.getCount())
							break loop;
						totalCost = SafeMath.addAndCheck(totalCost, SafeMath.mulAndCheck(count, price));
						weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(count, item.getTemplate().getWeight()));
						if(!item.isStackable() || buyer.getInventory().getItemByItemId(item.getItemId()) == null)
							++slots;
						si = new TradeItem();
						si.setObjectId(objectId);
						si.setItemId(item.getItemId());
						si.setCount(count);
						si.setOwnersPrice(price);
						si.setEnchantLevel(item.getEnchantLevel());
						sellList.add(si);
						break;
					}
				}
			}
		}
		catch(ArithmeticException ae)
		{
			sellList.clear();
			this.sendPacket(new SystemMessage(1036));
			try
			{
				if(sellList.size() != _count)
				{
					seller.sendPacket(new SystemMessage(1801));
					seller.sendActionFailed();
					return;
				}
				if(!buyer.getInventory().validateWeight(weight))
				{
					buyer.sendPacket(new SystemMessage(422));
					seller.sendPacket(new SystemMessage(1801));
					seller.sendActionFailed();
					return;
				}
				if(!buyer.getInventory().validateCapacity(slots))
				{
					buyer.sendPacket(new SystemMessage(129));
					seller.sendPacket(new SystemMessage(1801));
					seller.sendActionFailed();
					return;
				}
				if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
				{
					if(buyer.getAdena() < totalCost)
					{
						buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
						seller.sendActionFailed();
						return;
					}
				}
				else
				{
					if(Functions.getItemCount(buyer, currecyId) < totalCost)
					{
						buyer.sendPacket(Msg.INCORRECT_ITEM_COUNT);
						seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
						seller.sendActionFailed();
						return;
					}
				}
				buyer.getTradeList().buySellItems(buyer, buyer, buyList, seller, sellList);
				buyer.saveTradeList();
			}
			finally
			{
				seller.getInventory().writeUnlock();
				buyer.getInventory().writeUnlock();
			}
			return;
		}
		finally
		{
			try
			{
				if(sellList.size() != _count)
				{
					seller.sendPacket(new SystemMessage(1801));
					seller.sendActionFailed();
					return;
				}
				if(!buyer.getInventory().validateWeight(weight))
				{
					buyer.sendPacket(new SystemMessage(422));
					seller.sendPacket(new SystemMessage(1801));
					seller.sendActionFailed();
					return;
				}
				if(!buyer.getInventory().validateCapacity(slots))
				{
					buyer.sendPacket(new SystemMessage(129));
					seller.sendPacket(new SystemMessage(1801));
					seller.sendActionFailed();
					return;
				}
				if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
				{
					if(buyer.getAdena() < totalCost)
					{
						buyer.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
						seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
						seller.sendActionFailed();
						return;
					}
				}
				else
				{
					if(Functions.getItemCount(buyer, currecyId) < totalCost)
					{
						buyer.sendPacket(Msg.INCORRECT_ITEM_COUNT);
						seller.sendPacket(new SystemMessage(SystemMessage.THE_ATTEMPT_TO_SELL_HAS_FAILED));
						seller.sendActionFailed();
						return;
					}
				}
				buyer.getTradeList().buySellItems(buyer, buyer, buyList, seller, sellList);
				buyer.saveTradeList();
			}
			finally
			{
				seller.getInventory().writeUnlock();
				buyer.getInventory().writeUnlock();
			}
		}
		if(buyer.getBuyList().isEmpty())
			TradeList.cancelStore(buyer);
		seller.sendChanges();
		buyer.sendChanges();
		seller.sendActionFailed();
	}
}
