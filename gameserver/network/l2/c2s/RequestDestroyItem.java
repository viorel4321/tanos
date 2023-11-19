package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.utils.Log;

public class RequestDestroyItem extends L2GameClientPacket
{
	private int _objectId;
	private int _count;

	@Override
	public void readImpl()
	{
		_objectId = readD();
		_count = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		int count = _count;
		final ItemInstance itemToRemove = activeChar.getInventory().getItemByObjectId(_objectId);
		if(itemToRemove == null || itemToRemove.isWear())
			return;
		if(count < 1)
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DESTROY_IT_BECAUSE_THE_NUMBER_IS_INCORRECT);
			return;
		}
		if(itemToRemove.isHeroWeapon())
		{
			activeChar.sendPacket(Msg.HERO_WEAPONS_CANNOT_BE_DESTROYED);
			return;
		}
		if(!itemToRemove.canBeDestroyed(activeChar))
		{
			activeChar.sendPacket(Msg.THIS_ITEM_CANNOT_BE_DISCARDED);
			return;
		}
		if(activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		if(activeChar.getServitor() != null && activeChar.getServitor().getControlItemId() == itemToRemove.getObjectId())
		{
			activeChar.sendPacket(Msg.THE_PET_HAS_BEEN_SUMMONED_AND_CANNOT_BE_DELETED);
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(count > itemToRemove.getIntegerLimitedCount())
			count = itemToRemove.getIntegerLimitedCount();
		itemToRemove.setWhFlag(false);
		final boolean broadcast = itemToRemove.isEquipped() && !itemToRemove.isArrow();
		if(itemToRemove.canBeCrystallized(activeChar, false))
		{
			final int level = activeChar.getSkillLevel(248);
			if(level >= 1 && itemToRemove.getTemplate().getItemGrade().ordinal() <= level)
			{
				activeChar.getInventory().destroyItem(itemToRemove, 1L, true);
				final int crystalAmount = itemToRemove.getTemplate().getCrystalCount();
				final short crystalId = RequestCrystallizeItem._crystalId[itemToRemove.getTemplate().getItemGrade().ordinal()];
				final ItemInstance createditem = ItemTable.getInstance().createItem(crystalId);
				createditem.setCount(crystalAmount);
				final ItemInstance addedItem = activeChar.getInventory().addItem(createditem);
				if(broadcast)
					activeChar.sendDisarmMessage(itemToRemove);
				activeChar.sendPacket(Msg.THE_ITEM_HAS_BEEN_SUCCESSFULLY_CRYSTALLIZED);
				activeChar.sendPacket(new SystemMessage(29).addItemName(Short.valueOf(crystalId)).addNumber(Integer.valueOf(crystalAmount)));
				Log.LogItem(activeChar, "Crystalize", itemToRemove);
				activeChar.updateStats();
				return;
			}
		}
		final ItemInstance removedItem = activeChar.getInventory().destroyItem(_objectId, count, false);
		Log.LogItem(activeChar, "Delete", removedItem, count);
		if(!broadcast)
			activeChar.sendPacket(SystemMessage.removeItems(removedItem.getItemId(), count, removedItem.getEnchantLevel()));
		if(broadcast)
			activeChar.sendDisarmMessage(removedItem);
		activeChar.updateStats();
	}
}
