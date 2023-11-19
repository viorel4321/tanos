package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.items.ClanWarehousePool;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Log;

public class SendWareHouseWithDrawList extends L2GameClientPacket
{
	private static Logger _log;
	private int _count;
	private int[] _items;
	private int[] counts;

	@Override
	public void readImpl()
	{
		_count = readD();
		if(_count * 8 > _buf.remaining() || _count > 32767 || _count <= 0)
		{
			_items = null;
			return;
		}
		_items = new int[_count * 2];
		counts = new int[_count];
		for(int i = 0; i < _count; ++i)
		{
			_items[i * 2 + 0] = readD();
			_items[i * 2 + 1] = readD();
			if(_items[i * 2 + 0] < 1 || _items[i * 2 + 1] < 0)
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
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final NpcInstance whkeeper = activeChar.getLastNpc();
		if(whkeeper == null || !activeChar.isInRange(whkeeper.getLoc(), 150L))
		{
			activeChar.sendPacket(Msg.WAREHOUSE_IS_TOO_FAR);
			return;
		}
		boolean canWithdrawCWH = false;
		int clanId = 0;
		if(activeChar.getClan() != null)
		{
			clanId = activeChar.getClan().getClanId();
			if((activeChar.getClanPrivileges() & 0x8) == 0x8 && (Config.ALT_ALLOW_OTHERS_WITHDRAW_FROM_CLAN_WAREHOUSE || activeChar.getClan().getLeaderId() == activeChar.getObjectId()))
				canWithdrawCWH = true;
		}
		if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.CLAN && !canWithdrawCWH)
			return;
		int weight = 0;
		int finalCount = 0;
		final int[] olditems = new int[_count];
		for(int i = 0; i < _count; ++i)
		{
			final int itemObjId = _items[i * 2 + 0];
			int count = _items[i * 2 + 1];
			final ItemInstance oldinst = ItemInstance.restoreFromDb(itemObjId, false);
			if(count < 0)
			{
				activeChar.sendPacket(Msg.INCORRECT_ITEM_COUNT);
				return;
			}
			if(oldinst == null)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.SendWareHouseWithDrawList.Changed"));
				return;
			}
			if(oldinst.getOwnerId() != activeChar.getObjectId())
				if(oldinst.getOwnerId() == clanId)
				{
					if(!canWithdrawCWH)
						continue;
				}
				else if(!activeChar.getAccountChars().containsKey(oldinst.getOwnerId()))
					continue;
			if(oldinst.getIntegerLimitedCount() < count)
				count = oldinst.getIntegerLimitedCount();
			counts[i] = count;
			olditems[i] = oldinst.getObjectId();
			weight += oldinst.getTemplate().getWeight() * count;
			++finalCount;
			if(oldinst.getTemplate().isStackable() && activeChar.getInventory().getItemByItemId(oldinst.getItemId()) != null)
				--finalCount;
		}
		if(!activeChar.getInventory().validateCapacity(finalCount))
		{
			activeChar.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return;
		}
		if(!activeChar.getInventory().validateWeight(weight))
		{
			activeChar.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
			return;
		}
		Warehouse warehouse = null;
		String logType = null;
		if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.PRIVATE)
		{
			warehouse = activeChar.getWarehouse();
			logType = "WarehouseWithdraw";
		}
		else
		{
			if(activeChar.getUsingWarehouseType() == Warehouse.WarehouseType.CLAN)
			{
				ClanWarehousePool.getInstance().AddWork(activeChar, olditems, counts);
				return;
			}
			if(activeChar.getUsingWarehouseType() != Warehouse.WarehouseType.FREIGHT)
			{
				SendWareHouseWithDrawList._log.warn("Error retrieving a warehouse object for char " + activeChar.toString() + " - using warehouse type: " + activeChar.getUsingWarehouseType());
				return;
			}
			warehouse = activeChar.getFreight();
			logType = "FreightWithdraw";
		}
		for(int j = 0; j < olditems.length; ++j)
		{
			final ItemInstance TransferItem = warehouse.takeItemByObj(olditems[j], counts[j]);
			if(TransferItem != null)
			{
				activeChar.getInventory().addItem(TransferItem);
				Log.LogItem(activeChar, logType, TransferItem);
			}
		}
		activeChar.sendChanges();
	}

	static
	{
		SendWareHouseWithDrawList._log = LoggerFactory.getLogger(SendWareHouseWithDrawList.class);
	}
}
