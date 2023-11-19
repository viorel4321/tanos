package l2s.gameserver.utils;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ItemInstance.ItemLocation;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.PetDataTable;
import l2s.gameserver.templates.item.ItemTemplate;

public final class ItemFunctions
{
	public static ItemInstance createItem(int itemId)
	{
		ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
		item.setLocation(ItemLocation.VOID);
		item.setCount(1L);

		return item;
	}

	public static boolean checkUseItem(Player player, ItemInstance item, boolean sendMsg)
	{
		if(player.isInTrade())
		{
			if(sendMsg)
				player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_PICK_UP_OR_USE_ITEMS_WHILE_TRADING));
			return false;
		}

		int itemId = item.getItemId();
		if(player.isInStoreMode() && itemId != 728)
		{
			if(sendMsg)
			{
				if(PetDataTable.isPetControlItem(item))
					player.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
				else
					player.sendPacket(Msg.YOU_MAY_NOT_USE_ITEMS_IN_A_PRIVATE_STORE_OR_PRIVATE_WORK_SHOP);
			}
			return false;
		}

		if(itemId == ItemTemplate.ITEM_ID_ADENA)
		{
			if(sendMsg)
				player.sendActionFailed();
			return false;
		}

		if(player.isFishing() && (itemId < 6535 || itemId > 6540))
		{
			if(sendMsg)
				player.sendPacket(Msg.YOU_CANNOT_DO_ANYTHING_ELSE_WHILE_FISHING);
			return false;
		}

		if(player.isDead())
		{
			if(sendMsg)
				player.sendPacket(new SystemMessage(SystemMessage.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(Integer.valueOf(itemId)));
			return false;
		}

		if(item.getTemplate().isForPet())
		{
			if(sendMsg)
				player.sendPacket(new SystemMessage(SystemMessage.YOU_CANNOT_EQUIP_A_PET_ITEM).addItemName(Integer.valueOf(itemId)));
			return false;
		}

		if(player.isInOlympiadMode() && ArrayUtils.contains(Config.OLY_RESTRICTED_ITEMS, itemId))
		{
			if(sendMsg)
			{
				if(item.isEquipable())
					player.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_CANT_BE_EQUIPPED_FOR_THE_OLYMPIAD_EVENT));
				else
					player.sendPacket(new SystemMessage(SystemMessage.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT));
			}
			return false;
		}

		if(player.inEvent && ArrayUtils.contains(Config.EVENT_RESTRICTED_ITEMS, itemId) || player.inLH && ArrayUtils.contains(Config.LH_RESTRICTED_ITEMS, itemId))
		{
			if(sendMsg)
			{
				if(item.isEquipable())
					player.sendMessage(player.isLangRus() ? "Этот предмет нельзя экипировать в ивенте." : "This item can't be equipped in event.");
				else
					player.sendMessage(player.isLangRus() ? "Этот предмет недоступен в ивенте." : "This item is not available in event.");
			}
			return false;
		}
		return true;
	}
}
