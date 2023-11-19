package l2s.gameserver.network.l2.c2s;

import java.util.HashMap;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.PcFreight;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.utils.Log;

public class RequestPackageSend extends L2GameClientPacket
{
	private int _objectID;
	private HashMap<Integer, Integer> _items;
	private static int _FREIGHT_FEE = 1000;

	@Override
	public void readImpl()
	{
		_objectID = readD();
		final int itemsCount = readD();
		if(itemsCount * 8 > _buf.remaining() || itemsCount > 32767 || itemsCount <= 0)
		{
			_items = null;
			return;
		}
		_items = new HashMap<Integer, Integer>(itemsCount + 1, 0.999f);
		for(int i = 0; i < itemsCount; ++i)
		{
			final int obj_id = readD();
			final int itemQuantity = readD();
			if(itemQuantity < 0)
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
		if(activeChar == null || _items == null || !activeChar.getPlayerAccess().UseWarehouse)
			return;
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
		final PcInventory inventory = activeChar.getInventory();
		long adenaDeposit = 0L;
		final ItemInstance adena = inventory.getItemByItemId(57);
		int adenaObjId;
		if(adena != null)
			adenaObjId = adena.getObjectId();
		else
			adenaObjId = -1;
		for(final Integer itemObjectId : _items.keySet())
		{
			final ItemInstance item = inventory.getItemByObjectId(itemObjectId);
			if(item == null || item.isEquipped())
				return;
			if(_items.get(itemObjectId) < 0)
				return;
			if(itemObjectId != adenaObjId)
				continue;
			adenaDeposit = _items.get(itemObjectId);
		}
		final NpcInstance freighter = activeChar.getLastNpc();
		if(freighter == null || !activeChar.isInRange(freighter.getLoc(), 150L))
		{
			activeChar.sendPacket(Msg.YOU_FAILED_AT_SENDING_THE_PACKAGE_BECAUSE_YOU_ARE_TOO_FAR_FROM_THE_WAREHOUSE);
			return;
		}
		int fee = _items.size() * RequestPackageSend._FREIGHT_FEE;
		if(fee + adenaDeposit > activeChar.getAdena())
		{
			activeChar.sendPacket(Msg.YOU_LACK_THE_FUNDS_NEEDED_TO_PAY_FOR_THIS_TRANSACTION);
			return;
		}
		final Warehouse warehouse = new PcFreight(_objectID);
		if(_items.size() + warehouse.listItems(ItemClass.ALL).length > activeChar.getFreightLimit())
		{
			activeChar.sendPacket(Msg.THE_CAPACITY_OF_THE_WAREHOUSE_HAS_BEEN_EXCEEDED);
			return;
		}
		for(final Integer itemObjectId2 : _items.keySet())
		{
			final ItemInstance found = inventory.getItemByObjectId(itemObjectId2);
			if(found == null || !found.canBeDropped(activeChar) || !found.canBeStored(activeChar, false))
				fee -= RequestPackageSend._FREIGHT_FEE;
			else
			{
				final ItemInstance item2 = inventory.dropItem(found, _items.get(itemObjectId2), false);
				warehouse.addItem(item2);
				Log.LogItem(activeChar, "FreightDeposit", item2);
			}
		}
		if(fee <= 0)
			return;
		activeChar.reduceAdena(fee, true);
		activeChar.updateStats();
		activeChar.sendPacket(Msg.THE_TRANSACTION_IS_COMPLETE);
	}
}
