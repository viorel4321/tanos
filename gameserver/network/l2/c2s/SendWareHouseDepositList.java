package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.utils.Log;

public class SendWareHouseDepositList extends L2GameClientPacket
{
	private static final int _WAREHOUSE_FEE = 30;
	private HashMap<Integer, Integer> _items;

	@Override
	public void readImpl()
	{
		final int itemsCount = readD();
		if(itemsCount * 8 > _buf.remaining() || itemsCount > 32767 || itemsCount < 0)
		{
			_items = null;
			return;
		}
		_items = new HashMap<Integer, Integer>(itemsCount + 1, 0.999f);
		for(int i = 0; i < itemsCount; ++i)
		{
			final int obj_id = readD();
			final int itemQuantity = readD();
			if(obj_id < 1 || itemQuantity < 0)
			{
				_items = null;
				return;
			}
			_items.put(obj_id, itemQuantity);
		}
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _items == null)
			return;
		final NpcInstance whkeeper = activeChar.getLastNpc();
		if(whkeeper == null || !activeChar.isInRange(whkeeper.getLoc(), 150L))
		{
			activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
			return;
		}
		if(activeChar.isInStoreMode())
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		final PcInventory inventory = activeChar.getInventory();
		final boolean privatewh = activeChar.getUsingWarehouseType() != Warehouse.WarehouseType.CLAN;
		int slotsleft = 0;
		int adenaDeposit = 0;
		Warehouse warehouse;
		ItemInstance[] itemsOnWarehouse;
		if(privatewh)
		{
			warehouse = activeChar.getWarehouse();
			itemsOnWarehouse = warehouse.listItems(ItemClass.ALL);
			slotsleft = activeChar.getWarehouseLimit() - itemsOnWarehouse.length;
		}
		else
		{
			if(activeChar.getClan() == null)
				return;
			warehouse = activeChar.getClan().getWarehouse();
			itemsOnWarehouse = warehouse.listItems(ItemClass.ALL);
			slotsleft = Config.WAREHOUSE_SLOTS_CLAN - itemsOnWarehouse.length;
		}
		final HashMap<Integer, Integer> stackableList = new HashMap<Integer, Integer>();
		for(final ItemInstance i : itemsOnWarehouse)
			if(i.isStackable())
				stackableList.put(i.getItemId(), i.getIntegerLimitedCount());
		final List<ItemInstance> itemsToStoreList = new ArrayList<ItemInstance>(_items.size() + 1);
		for(final Integer itemObjectId : _items.keySet())
		{
			final ItemInstance item = inventory.getItemByObjectId(itemObjectId);
			if(item != null)
			{
				if(!item.canBeStored(activeChar, privatewh))
					continue;
				if(!item.isStackable() || !stackableList.containsKey(item.getItemId()))
				{
					if(slotsleft <= 0)
						continue;
					--slotsleft;
				}
				if(item.isStackable())
				{
					final int cn = _items.get(itemObjectId);
					if(item.getItemId() == 57)
						adenaDeposit = cn;
					if(cn > Integer.MAX_VALUE)
						continue;
					if(stackableList.containsKey(item.getItemId()) && stackableList.get(item.getItemId()) + cn > Integer.MAX_VALUE)
						continue;
				}
				itemsToStoreList.add(item);
			}
		}
		final int fee = itemsToStoreList.size() * 30;
		if(fee + adenaDeposit > activeChar.getAdena())
		{
			activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
			return;
		}
		if(slotsleft <= 0)
			activeChar.sendPacket(Msg.YOUR_WAREHOUSE_IS_FULL);
		final int clanId = activeChar.getClanId();
		for(final ItemInstance itemToStore : itemsToStoreList)
		{
			final ItemInstance item2 = inventory.dropItem(itemToStore, _items.get(itemToStore.getObjectId()), true);
			if(item2 == null)
				continue;
			warehouse.addItem(item2);
			Log.LogItem(activeChar, privatewh ? "WarehouseDeposit" : "ClanWarehouseDeposit", item2);
		}
		activeChar.reduceAdena(fee, true);
		activeChar.updateStats();
	}
}
