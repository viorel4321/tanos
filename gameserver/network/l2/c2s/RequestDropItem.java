package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Log;

public class RequestDropItem extends L2GameClientPacket
{
	private static Logger _log;
	private int _objectId;
	private long _count;
	private Location _loc;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_count = readD();
		_loc = new Location(readD(), readD(), readD());
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.isDead())
			return;
		if(_count < 1L || _loc == null || _loc.isNull())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!Config.ALLOW_DISCARDITEM && (!Config.ALLOW_DISCARDITEM_GM || !activeChar.isGM()))
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestDropItem.Disallowed"));
			return;
		}
		if(activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isInTransaction())
		{
			this.sendPacket(Msg.NOTHING_HAPPENED);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(activeChar.isActionsDisabled() || activeChar.isSitting() || activeChar.isDropDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.isInRangeSq(_loc, 22500L) || Math.abs(_loc.z - activeChar.getZ()) > 50)
		{
			activeChar.sendPacket(Msg.TOO_FAR_TO_DISCARD);
			return;
		}
		final ItemInstance oldItem = activeChar.getInventory().getItemByObjectId(_objectId);
		if(oldItem == null)
		{
			RequestDropItem._log.warn(activeChar.getName() + " tried to drop an item that is not in the inventory ?! itemObjectId:" + _objectId);
			return;
		}
		if(!oldItem.canBeDropped(activeChar))
		{
			activeChar.sendPacket(Msg.THAT_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		final int oldCount = oldItem.getIntegerLimitedCount();
		if(oldCount < _count)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(oldItem.isEquipped() && (!oldItem.isArrow() || oldItem.getCount() <= _count))
		{
			if(activeChar.recording)
				activeChar.recBot(3, oldItem.getBodyPart(), 1, 0, 0, 0, 0);
			activeChar.getInventory().unEquipItemInBodySlotAndNotify(oldItem.getBodyPart(), oldItem);
		}
		oldItem.setWhFlag(true);
		final ItemInstance dropedItem = activeChar.getInventory().dropItem(_objectId, _count);
		oldItem.setWhFlag(false);
		if(dropedItem == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		dropedItem.dropToTheGround(activeChar, _loc);
		activeChar.disableDrop(1000);
		Log.LogItem(activeChar, "Drop", dropedItem);
		activeChar.updateStats();
	}

	static
	{
		RequestDropItem._log = LoggerFactory.getLogger(RequestDropItem.class);
	}
}
