package l2s.gameserver.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.math.SafeMath;
import l2s.gameserver.Config;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.scripts.Functions;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Log;
import l2s.gameserver.utils.Stat;

public class TradeList
{
	private static Logger _log = LoggerFactory.getLogger(TradeList.class);

	private final List<ItemInstance> _items;
	private int _listId;
	private boolean _confirmed;
	private boolean _spamSell;
	private boolean _spamBuy;
	private String _buyStoreName;
	private String _sellStoreName;

	public TradeList(final int listId)
	{
		_items = new ArrayList<ItemInstance>();
		_listId = listId;
		_confirmed = false;
		_spamSell = false;
		_spamBuy = false;
	}

	public TradeList()
	{
		this(0);
	}

	public void addItem(final ItemInstance item)
	{
		synchronized (_items)
		{
			_items.add(item);
		}
	}

	public void removeAll()
	{
		_items.clear();
	}

	public int getListId()
	{
		return _listId;
	}

	public void setSellStoreName(final String name)
	{
		_sellStoreName = name;
		_spamSell = Config.SPAM_PS_WORK && name != null && Config.containsSpamWord(Config.SPAM_SKIP_SYMBOLS ? name.replaceAll("[^0-9a-zA-Z\u0410-\u042f\u0430-\u044f]", "") : name);
	}

	public String getSellStoreName()
	{
		return _sellStoreName;
	}

	public void setBuyStoreName(final String name)
	{
		_buyStoreName = name;
		_spamBuy = Config.SPAM_PS_WORK && name != null && Config.containsSpamWord(Config.SPAM_SKIP_SYMBOLS ? name.replaceAll("[^0-9a-zA-Z\u0410-\u042f\u0430-\u044f]", "") : name);
	}

	public String getBuyStoreName()
	{
		return _buyStoreName;
	}

	public List<ItemInstance> getItems()
	{
		return _items;
	}

	public ItemInstance getItemByItemId(final int itemId)
	{
		synchronized (_items)
		{
			for(final ItemInstance item : _items)
				if(item.getItemId() == itemId)
					return item;
		}
		return null;
	}

	public ItemInstance getItem(final int ObjectId)
	{
		synchronized (_items)
		{
			for(final ItemInstance item : _items)
				if(item.getObjectId() == ObjectId)
					return item;
		}
		return null;
	}

	public void setConfirmedTrade(final boolean x)
	{
		_confirmed = x;
	}

	public boolean hasConfirmed()
	{
		return _confirmed;
	}

	public boolean isSpamBuy()
	{
		return _spamBuy;
	}

	public boolean isSpamSell()
	{
		return _spamSell;
	}

	public boolean contains(final int objId)
	{
		synchronized (_items)
		{
			for(final ItemInstance item : _items)
				if(item.getObjectId() == objId)
					return true;
		}
		return false;
	}

	public static boolean validateTrade(final Player player, final Collection<TradeItem> items, final Player taker)
	{
		final Inventory playerInv = player.getInventory();
		final Inventory takerInv = taker.getInventory();
		int slots = 0;
		int weight = 0;
		synchronized (items)
		{
			for(final TradeItem item : items)
			{
				final ItemInstance playerItem = playerInv.getItemByObjectId(item.getObjectId());
				if(playerItem == null || playerItem.getCount() < item.getCount() || playerItem.getEnchantLevel() != item.getEnchantLevel() || !playerItem.canBeTraded(player))
					return false;
				weight = SafeMath.addAndCheck(weight, SafeMath.mulAndCheck(item.getCount(), item.getItem().getWeight()));
				if(item.getItem().isStackable() && takerInv.getItemByItemId(item.getItemId()) != null)
					continue;
				++slots;
			}
		}
		if(!taker.getInventory().validateWeight(weight))
		{
			taker.sendPacket(new SystemMessage(422));
			return false;
		}
		if(!taker.getInventory().validateCapacity(slots))
		{
			taker.sendPacket(new SystemMessage(129));
			return false;
		}
		return true;
	}

	public synchronized void tradeItems(final Player player, final Player receiver)
	{
		final Inventory playersInv = player.getInventory();
		final Inventory receiverInv = receiver.getInventory();
		for(final ItemInstance temp : _items)
		{
			if(player.getEnchantScroll() != null && temp.getObjectId() == player.getEnchantScroll().getObjectId())
				player.setEnchantScroll(null);
			final ItemInstance oldItem = playersInv.getItemByObjectId(temp.getObjectId());
			if(oldItem == null)
				continue;
			oldItem.setWhFlag(true);
			final ItemInstance transferItem = playersInv.dropItem(temp.getObjectId(), temp.getIntegerLimitedCount());
			oldItem.setWhFlag(false);
			if(transferItem == null)
				continue;
			Log.LogItem(player, "TradeSell", transferItem);
			temp.setLastChange((byte) oldItem.getLastChange());
			final ItemInstance receiverItem = receiverInv.addItem(transferItem);
			transferItem.setWhFlag(false);
			Log.LogItem(receiver, "TradeBuy", receiverItem);
		}
		player.sendChanges();
		receiver.sendChanges();
	}

	public void updateSellList(final Player player, final ConcurrentLinkedQueue<TradeItem> list)
	{
		final Inventory playersInv = player.getInventory();
		for(final ItemInstance temp : _items)
		{
			final ItemInstance item = playersInv.getItemByObjectId(temp.getObjectId());
			if(item == null || item.getCount() <= 0L)
			{
				for(final TradeItem i : list)
					if(i.getObjectId() == temp.getItemId())
					{
						list.remove(i);
						break;
					}
			}
			else
			{
				if(item.getCount() >= temp.getCount())
					continue;
				temp.setCount(item.getCount());
			}
		}
	}

	public synchronized void buySellItems(final Player privateStoreOwner, final Player buyer, final ConcurrentLinkedQueue<TradeItem> listToBuy, final Player seller, final ConcurrentLinkedQueue<TradeItem> listToSell)
	{
		final int currecyId = privateStoreOwner.getPrivateStoreCurrecy();
		final Inventory sellerInv = seller.getInventory();
		final Inventory buyerInv = buyer.getInventory();
		TradeItem sellerTradeItem = null;
		ItemInstance sellerInventoryItem = null;
		final ConcurrentLinkedQueue<TradeItem> unsold = new ConcurrentLinkedQueue<TradeItem>();
		unsold.addAll(listToSell);
		int cost = 0;
		int amount = 0;
		for(final TradeItem buyerTradeItem : listToBuy)
		{
			sellerTradeItem = null;
			for(final TradeItem unsoldItem : unsold)
				if(unsoldItem.getItemId() == buyerTradeItem.getItemId() && unsoldItem.getOwnersPrice() == buyerTradeItem.getOwnersPrice())
				{
					sellerTradeItem = unsoldItem;
					break;
				}
			if(sellerTradeItem == null)
				continue;
			sellerInventoryItem = sellerInv.getItemByObjectId(sellerTradeItem.getObjectId());
			unsold.remove(sellerTradeItem);
			if(sellerInventoryItem == null)
				continue;
			int buyerItemCount = buyerTradeItem.getCount();
			int sellerItemCount = sellerTradeItem.getCount();
			if(sellerItemCount > sellerInventoryItem.getIntegerLimitedCount())
				sellerItemCount = sellerInventoryItem.getIntegerLimitedCount();
			if(seller.getPrivateStoreType() == Player.STORE_PRIVATE_SELL || seller.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE)
			{
				if(buyerItemCount > sellerItemCount)
					buyerTradeItem.setCount(sellerItemCount);
				if(buyerItemCount > sellerInventoryItem.getIntegerLimitedCount())
					buyerTradeItem.setCount(sellerInventoryItem.getIntegerLimitedCount());
				buyerItemCount = amount = buyerTradeItem.getCount();
				cost = amount * sellerTradeItem.getOwnersPrice();
			}
			if(buyer.getPrivateStoreType() == Player.STORE_PRIVATE_BUY)
			{
				if(sellerItemCount > buyerItemCount)
					sellerTradeItem.setCount(buyerItemCount);
				if(sellerItemCount > sellerInventoryItem.getIntegerLimitedCount())
					sellerTradeItem.setCount(sellerInventoryItem.getIntegerLimitedCount());
				sellerItemCount = amount = sellerTradeItem.getCount();
				cost = amount * buyerTradeItem.getOwnersPrice();
			}
			int sum = buyerItemCount * buyerTradeItem.getOwnersPrice();
			if(sum > Integer.MAX_VALUE)
			{
				_log.warn("Integer Overflow on Cost. Possible Exploit attempt between " + buyer.getName() + " and " + seller.getName() + ".");
				_log.warn(buyer.getName() + " try to use exploit, ban thisPlayer!");
				seller.sendMessage(new CustomMessage("l2s.gameserver.model.TradeList.BuyerExploit"));
				return;
			}
			sum = sellerItemCount * sellerTradeItem.getOwnersPrice();
			if(sum > Integer.MAX_VALUE)
			{
				_log.warn("Integer Overflow on Cost. Possible Exploit attempt between " + buyer.getName() + " and " + seller.getName() + ".");
				_log.warn(seller.getName() + " try to use exploit, ban thisPlayer!");
				buyer.sendMessage(new CustomMessage("l2s.gameserver.model.TradeList.SellerExploit"));
				return;
			}

			if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
			{
				if(buyer.getAdena() < cost)
				{
					_log.warn("buy item without full Adena sum " + buyer.getName() + " and " + seller.getName() + ".");
					return;
				}
			}
			else
			{
				if(Functions.getItemCount(buyer, currecyId) < cost)
				{
					_log.warn("buy item without full ID[" + currecyId + "] sum " + buyer.getName() + " and " + seller.getName() + ".");
					return;
				}
			}

			ItemInstance transferItem = sellerInv.dropItem(sellerInventoryItem.getObjectId(), amount);
			Log.LogItem(seller, "PrivateStoreSell", transferItem);

			if(currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA)
			{
				buyer.reduceAdena(cost, false);
				seller.addAdena(cost);

				int tax = (int) (cost * Config.SERVICES_TRADE_TAX / 100.0f);
				if(ZoneManager.getInstance().checkIfInZone(Zone.ZoneType.offshore, seller.getX(), seller.getY()))
					tax = (int) (cost * Config.SERVICES_OFFSHORE_TRADE_TAX / 100.0f);
				if(Config.SERVICES_TRADE_TAX_ONLY_OFFLINE && !seller.isInOfflineMode())
					tax = 0;
				if(tax > 0)
				{
					seller.reduceAdena(tax, true);
					Stat.addTax(tax);
					seller.sendMessage(new CustomMessage("trade.HavePaidTax").addNumber(tax));
				}
			}
			else
			{
				Functions.removeItem(buyer, currecyId, cost);
				Functions.addItem(seller, currecyId, cost);
			}

			buyerInv.addItem(transferItem);
			Log.LogItem(buyer, "PrivateStoreBuy", transferItem);
			if(!transferItem.isStackable())
			{
				if(transferItem.getEnchantLevel() > 0)
				{
					seller.sendPacket(new SystemMessage(1155).addString(buyer.getName()).addNumber(Integer.valueOf(transferItem.getEnchantLevel())).addItemName(Integer.valueOf(sellerInventoryItem.getItemId())).addNumber(Integer.valueOf(cost)));
					buyer.sendPacket(new SystemMessage(1156).addString(seller.getName()).addNumber(Integer.valueOf(transferItem.getEnchantLevel())).addItemName(Integer.valueOf(sellerInventoryItem.getItemId())).addNumber(Integer.valueOf(cost)));
				}
				else
				{
					seller.sendPacket(new SystemMessage(1151).addString(buyer.getName()).addItemName(Integer.valueOf(sellerInventoryItem.getItemId())).addNumber(Integer.valueOf(cost)));
					buyer.sendPacket(new SystemMessage(1153).addString(seller.getName()).addItemName(Integer.valueOf(sellerInventoryItem.getItemId())).addNumber(Integer.valueOf(cost)));
				}
			}
			else
			{
				seller.sendPacket(new SystemMessage(1152).addString(buyer.getName()).addNumber(Integer.valueOf(amount)).addItemName(Integer.valueOf(sellerInventoryItem.getItemId())).addNumber(Integer.valueOf(cost)));
				buyer.sendPacket(new SystemMessage(1154).addString(seller.getName()).addItemName(Integer.valueOf(sellerInventoryItem.getItemId())).addNumber(Integer.valueOf(amount)).addNumber(Integer.valueOf(cost)));
			}
			sellerInventoryItem = null;
		}
		seller.sendChanges();
		buyer.sendChanges();
		if(seller.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE)
			seller.setSellList(null);
		if(seller.getPrivateStoreType() == Player.STORE_PRIVATE_SELL)
		{
			final HashSet<TradeItem> tmp = new HashSet<TradeItem>();
			tmp.addAll(seller.getSellList());
			for(final TradeItem sl : listToSell)
				for(final TradeItem bl : listToBuy)
					if(sl.getItemId() == bl.getItemId() && sl.getOwnersPrice() == bl.getOwnersPrice())
					{
						final ItemInstance inst = seller.getInventory().getItemByObjectId(sl.getObjectId());
						if(inst == null || inst.getIntegerLimitedCount() <= 0)
							tmp.remove(sl);
						else
						{
							if(!inst.isStackable())
								continue;
							sl.setCount(sl.getCount() - bl.getCount());
							if(sl.getCount() <= 0)
								tmp.remove(sl);
							else
							{
								if(inst.getIntegerLimitedCount() >= sl.getCount())
									continue;
								sl.setCount(inst.getIntegerLimitedCount());
							}
						}
					}
			final ConcurrentLinkedQueue<TradeItem> newlist = new ConcurrentLinkedQueue<TradeItem>();
			newlist.addAll(tmp);
			seller.setSellList(newlist);
		}
		if(buyer.getPrivateStoreType() == Player.STORE_PRIVATE_BUY)
		{
			final HashSet<TradeItem> tmp = new HashSet<TradeItem>();
			tmp.addAll(buyer.getBuyList());
			for(final TradeItem bl2 : listToBuy)
				for(final TradeItem sl2 : listToSell)
					if(sl2.getItemId() == bl2.getItemId() && sl2.getOwnersPrice() == bl2.getOwnersPrice() && ItemTable.getInstance().getTemplate(bl2.getItemId()).isStackable())
					{
						bl2.setCount(bl2.getCount() - sl2.getCount());
						if(bl2.getCount() > 0)
							continue;
						tmp.remove(bl2);
					}
			for(final TradeItem sl : listToSell)
				for(final TradeItem bl : listToBuy)
					if(sl.getItemId() == bl.getItemId() && sl.getOwnersPrice() == bl.getOwnersPrice() && !ItemTable.getInstance().getTemplate(sl.getItemId()).isStackable())
						tmp.remove(bl);
			final ConcurrentLinkedQueue<TradeItem> newlist = new ConcurrentLinkedQueue<TradeItem>();
			newlist.addAll(tmp);
			buyer.setBuyList(newlist);
		}
	}

	public void updateBuyList(final Player player, final ConcurrentLinkedQueue<TradeItem> list)
	{
		final Inventory playersInv = player.getInventory();
		for(final ItemInstance temp : _items)
			if(playersInv.findItemByItemId(temp.getItemId()) == null)
				list.remove(temp);
			else
			{
				if(temp.getCount() != 0L)
					continue;
				list.remove(temp);
			}
	}

	public static boolean validateList(final Player player)
	{
		if(player.getPrivateStoreType() == Player.STORE_PRIVATE_SELL || player.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE)
		{
			final ConcurrentLinkedQueue<TradeItem> selllist = player.getSellList();
			for(final TradeItem tl : selllist)
			{
				final ItemInstance inst = player.getInventory().getItemByObjectId(tl.getObjectId());
				if(inst == null || inst.getCount() <= 0L)
				{
					if(player.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE)
					{
						cancelStore(player);
						return false;
					}
					selllist.remove(tl);
				}
				else
				{
					if(!inst.isStackable() || inst.getCount() >= tl.getCount())
						continue;
					if(player.getPrivateStoreType() == Player.STORE_PRIVATE_SELL_PACKAGE)
					{
						cancelStore(player);
						return false;
					}
					tl.setCount((int) inst.getCount());
				}
			}
		}
		else if(player.getPrivateStoreType() == Player.STORE_PRIVATE_BUY)
		{
			final ConcurrentLinkedQueue<TradeItem> buylist = player.getBuyList();
			int sum = 0;
			for(final TradeItem tl2 : buylist)
				sum += tl2.getOwnersPrice() * tl2.getCount();
			int currecyId = player.getPrivateStoreCurrecy();
			if((currecyId == 0 || currecyId == ItemTemplate.ITEM_ID_ADENA) && player.getAdena() < sum || currecyId > 0 && Functions.getItemCount(player, currecyId) < sum)
			{
				cancelStore(player);
				return false;
			}
		}
		return true;
	}

	public static void cancelStore(final Player activeChar)
	{
		activeChar.setPrivateStoreType((short) 0);
		activeChar.getBuyList().clear();
		if(activeChar.isInOfflineMode() && Config.SERVICES_OFFLINE_TRADE_KICK_NOT_TRADING)
		{
			activeChar.setOfflineMode(false);
			activeChar.kick(false);
		}
		else
			activeChar.broadcastUserInfo(true);
	}
}
