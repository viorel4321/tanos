package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.TradeController;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.instances.CastleChamberlainInstance;
import l2s.gameserver.model.instances.ClanHallManagerInstance;
import l2s.gameserver.model.instances.MercManagerInstance;
import l2s.gameserver.model.instances.MerchantInstance;
import l2s.gameserver.model.instances.NpcFriendInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class RequestBuyItem extends L2GameClientPacket
{
	private static Logger _log;
	private int _listId;
	private int _count;
	private int[] _items;

	@Override
	public void readImpl()
	{
		_listId = readD();
		_count = readD();
		if(_count * 8 > _buf.remaining() || _count > 32767 || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 2];
		for(int i = 0; i < _count; ++i)
		{
			_items[i * 2 + 0] = readD();
			_items[i * 2 + 1] = readD();
			if(_items[i * 2 + 1] < 0)
			{
				_items = null;
				break;
			}
		}
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_items == null || _count == 0)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTrade())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(activeChar.getBuyListId() != _listId)
		{
			activeChar.sendActionFailed();
			return;
		}
		final boolean bbs = activeChar.getPlayerAccess().UseGMShop;
		final NpcInstance npc = activeChar.getLastNpc();
		if(!bbs && (!NpcInstance.canBypassCheck(activeChar, npc) || !activeChar.checkLastNpc()))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALT_GAME_KARMA_PLAYER_CAN_SHOP && !bbs && activeChar.getKarma() > 0 && !activeChar.isGM() && !ArrayUtils.contains(Config.ALT_GAME_KARMA_NPC, npc.getNpcId()))
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALLOW_PVPCB_SHOP_KARMA && activeChar.getKarma() > 0 && bbs && !activeChar.isGM())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(bbs)
			activeChar.setLastNpcId(-3);
		else if(!activeChar.isGM())
		{
			final boolean isValidMerchant = npc instanceof ClanHallManagerInstance || npc instanceof MerchantInstance || npc instanceof MercManagerInstance || npc instanceof CastleChamberlainInstance || npc instanceof NpcFriendInstance;
			if(!isValidMerchant)
			{
				activeChar.sendActionFailed();
				return;
			}
		}
		if(!bbs)
			activeChar.turn(npc, 3000);
		NpcInstance merchant = null;
		if(npc != null && (npc instanceof MerchantInstance || npc instanceof ClanHallManagerInstance))
			merchant = npc;
		final ArrayList<Item> items = new ArrayList<Item>(_count);
		for(int i = 0; i < _count; ++i)
		{
			final short itemId = (short) _items[i * 2 + 0];
			final int cnt = _items[i * 2 + 1];
			if(cnt <= 0)
			{
				activeChar.sendActionFailed();
				return;
			}
			final ItemTemplate temp = ItemTable.getInstance().getTemplate(itemId);
			if(temp == null)
			{
				activeChar.sendActionFailed();
				return;
			}
			if(!temp.isStackable() && cnt != 1)
			{
				activeChar.sendActionFailed();
				return;
			}
			final Item itm = new Item();
			itm.id = itemId;
			itm.count = cnt;
			itm.item = temp;
			items.add(itm);
		}
		long finalLoad = 0L;
		int finalCount = 0;
		int needsSpace = 2;
		int weight = 0;
		final int currentMoney = activeChar.getAdena();
		long subTotal = 0L;
		long tax = 0L;
		double taxRate = 0.0;
		Castle castle = null;
		if(merchant != null)
		{
			castle = merchant.getCastle();
			if(castle != null)
				taxRate = castle.getTaxRate();
		}
		for(int j = 0; j < items.size(); ++j)
		{
			final int itemId2 = items.get(j).id;
			final long cnt2 = items.get(j).count;
			needsSpace = 2;
			if(cnt2 < 0L || cnt2 > Integer.MAX_VALUE)
			{
				this.sendPacket(new SystemMessage(1036));
				activeChar.sendActionFailed();
				return;
			}
			if(items.get(j).item.isStackable())
			{
				needsSpace = 1;
				if(activeChar.getInventory().getItemByItemId(itemId2) != null)
					needsSpace = 0;
			}
			final TradeController.NpcTradeList list = TradeController.getInstance().getBuyList(_listId);
			if(list == null)
			{
				activeChar.sendActionFailed();
				return;
			}
			final TradeItem ti = getItemByItemId(itemId2, list);
			int price = ti == null ? 0 : ti.getOwnersPrice();
			if(ti != null && ti.getItem().isMercTicket())
				price *= (int) Config.RATE_SIEGE_GUARDS_PRICE;
			if(price == 0 && !bbs)
			{
				activeChar.sendMessage("You can't buy with zero price!");
				activeChar.sendActionFailed();
				return;
			}
			if(price < 0)
			{
				RequestBuyItem._log.warn("ERROR, no price found for listId: " + _listId + " itemId: " + itemId2);
				activeChar.sendMessage("You can't buy with negative price!");
				activeChar.sendActionFailed();
				return;
			}
			subTotal += cnt2 * price;
			tax = (long) (subTotal * taxRate);
			if(subTotal + tax < 0L || subTotal + tax > Integer.MAX_VALUE)
			{
				activeChar.sendMessage("You can't buy with incorrect total price!");
				activeChar.sendActionFailed();
				return;
			}
			weight = items.get(j).item.getWeight();
			finalLoad += cnt2 * weight;
			if(finalLoad < 0L || finalLoad > Integer.MAX_VALUE)
			{
				activeChar.sendMessage("You can't buy with incorrect weight!");
				activeChar.sendActionFailed();
				return;
			}
			if(needsSpace == 2)
				finalCount += (int) cnt2;
			else if(needsSpace == 1)
				++finalCount;
		}
		if(subTotal + tax > currentMoney || subTotal < 0L || currentMoney <= 0 && !bbs)
		{
			this.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA, Msg.ActionFail);
			return;
		}
		if(!activeChar.getInventory().validateWeight(finalLoad))
		{
			this.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT, Msg.ActionFail);
			return;
		}
		if(!activeChar.getInventory().validateCapacity(finalCount))
		{
			this.sendPacket(Msg.YOUR_INVENTORY_IS_FULL, Msg.ActionFail);
			return;
		}
		final TradeController.NpcTradeList list2 = TradeController.getInstance().getBuyList(_listId);
		for(int k = 0; k < items.size(); ++k)
		{
			final Item itm2 = items.get(k);
			final int itemId2 = itm2.id;
			final long cnt2 = itm2.count;
			final TradeItem ic = getItemByItemId(itemId2, list2);
			if(ic != null && ic.isCountLimited())
				if(cnt2 > ic.getCurrentValue())
				{
					itm2.count = ic.getCurrentValue();
					final int t = (int) (System.currentTimeMillis() / 60000L);
					if(ic.getLastRechargeTime() + ic.getRechargeTime() <= t)
					{
						ic.setLastRechargeTime(t);
						ic.setCurrentValue(ic.getCount());
					}
				}
				else
					ic.setCurrentValue((int) (ic.getCurrentValue() - cnt2));
		}
		activeChar.reduceAdena(subTotal + tax, true);
		for(final Item item : items)
			activeChar.getInventory().addItem(item.id, item.count);
		if(castle != null && tax > 0L && castle.getOwnerId() > 0)
			castle.addToTreasury((int) tax, true, false);
		this.sendPacket(new ItemList(activeChar, true));
		activeChar.sendChanges();
	}

	private static final TradeItem getItemByItemId(final int itemId, final TradeController.NpcTradeList list)
	{
		for(final TradeItem ti : list.getItems())
			if(ti.getItemId() == itemId)
				return ti;
		return null;
	}

	static
	{
		RequestBuyItem._log = LoggerFactory.getLogger(RequestBuyItem.class);
	}

	private static class Item
	{
		private int id;
		private int count;
		private ItemTemplate item;
	}
}
