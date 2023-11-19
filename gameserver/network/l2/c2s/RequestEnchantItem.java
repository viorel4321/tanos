package l2s.gameserver.network.l2.c2s;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.PcInventory;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.EnchantResult;
import l2s.gameserver.network.l2.s2c.InventoryUpdate;
import l2s.gameserver.network.l2.s2c.ItemList;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Log;

public class RequestEnchantItem extends L2GameClientPacket
{
	protected static Logger _log;
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
//		if(activeChar.isInCombat()) //Админ попросил отклучать
//		{
//			activeChar.sendMessage("You can't enchant while in combat");
//			activeChar.sendActionFailed();
//			return;
//		}
//		if(activeChar.isOutOfControl() || activeChar.isActionsDisabled()) //Админ попросил отклучать
//		{
//			activeChar.sendMessage("You can't enchant now.");
//			activeChar.sendActionFailed();
//			return;
//		}
		if(activeChar.inEvent())
		{
			activeChar.sendMessage("You can't enchant in event.");
			activeChar.sendActionFailed();
			return;
		}
		final PcInventory inventory = activeChar.getInventory();
		final ItemInstance item = inventory.getItemByObjectId(_objectId);
		ItemInstance scroll = activeChar.getEnchantScroll();
		activeChar.setEnchantScroll(null);
		if(item == null || scroll == null)
		{
			activeChar.sendMessage("You can't enchant at moment");
			activeChar.sendActionFailed();
			return;
		}
		final boolean blessed = scroll.isBlessedEnchantScroll();
		if(!item.canBeEnchanted() || Config.ENCHANT_BLESSED_HERO_WEAPON && item.isHeroWeapon() && !blessed)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			activeChar.sendMessage("This item can't be enchantrd");
			return;
		}
		if(item.getLocation() != ItemInstance.ItemLocation.INVENTORY && item.getLocation() != ItemInstance.ItemLocation.PAPERDOLL)
		{
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendPacket(Msg.YOU_CANNOT_PRACTICE_ENCHANTING_WHILE_OPERATING_A_PRIVATE_STORE_OR_PRIVATE_MANUFACTURING_WORKSHOP);
			activeChar.sendActionFailed();
			return;
		}
		if((scroll = inventory.getItemByObjectId(scroll.getObjectId())) == null)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendActionFailed();
			return;
		}
		final short crystalId = item.getEnchantCrystalId(scroll);
		if(crystalId == 0)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}
		final int itemType = item.getTemplate().getType2();
		if(itemType == 0 && item.getEnchantLevel() >= Config.ENCHANT_MAX_WEAPON)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestEnchantItem.MaxLevel"));
			activeChar.sendActionFailed();
			return;
		}
		if(itemType == 2 && item.getEnchantLevel() >= Config.ENCHANT_MAX_ACCESSORY)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestEnchantItem.MaxLevel"));
			activeChar.sendActionFailed();
			return;
		}
		if(itemType == 1 && item.getEnchantLevel() >= Config.ENCHANT_MAX_ARMOR)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestEnchantItem.MaxLevel"));
			activeChar.sendActionFailed();
			return;
		}
		if(item.getOwnerId() != activeChar.getObjectId())
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendPacket(Msg.INAPPROPRIATE_ENCHANT_CONDITIONS);
			activeChar.sendActionFailed();
			return;
		}
		final ItemInstance removedScroll;
		synchronized (inventory)
		{
			removedScroll = inventory.destroyItem(scroll.getObjectId(), 1L, true);
		}
		if(removedScroll == null)
		{
			activeChar.sendPacket(EnchantResult.CANCELLED);
			activeChar.sendActionFailed();
			return;
		}
		final boolean full = item.getTemplate().getBodyPart() == 32768;
		final boolean safeEnchant = full ? item.getEnchantLevel() < Config.SAFE_ENCHANT_FULL_BODY : item.getEnchantLevel() < Config.SAFE_ENCHANT_COMMON;
		double chance;
		if(safeEnchant)
			chance = 100.0;
		else if(!removedScroll.isCrystallEnchantScroll() && ArrayUtils.contains(Config.ENCHANT_LIST, item.getItemId()))
			chance = Config.USE_ALT_ENCHANT ? item.getEnchantLevel() >= Config.ALT_ENCHANT_LIST.size() ? Config.ALT_ENCHANT_LIST.get(Config.ALT_ENCHANT_LIST.size() - 1) : (double) Config.ALT_ENCHANT_LIST.get(item.getEnchantLevel()) : Config.ENCHANT_CHANCE_LIST;
		else if(itemType == 0)
			chance = removedScroll.isCrystallEnchantScroll() ? Config.ENCHANT_CHANCE_CRYSTAL_WEAPON : Config.USE_ALT_ENCHANT ? item.getEnchantLevel() >= Config.ALT_ENCHANT_WEAPON.size() ? Config.ALT_ENCHANT_WEAPON.get(Config.ALT_ENCHANT_WEAPON.size() - 1) : (double) Config.ALT_ENCHANT_WEAPON.get(item.getEnchantLevel()) : Config.ENCHANT_CHANCE_WEAPON;
		else if(itemType == 1)
			chance = removedScroll.isCrystallEnchantScroll() ? Config.ENCHANT_CHANCE_CRYSTAL_ARMOR : Config.USE_ALT_ENCHANT ? item.getEnchantLevel() >= Config.ALT_ENCHANT_ARMOR.size() ? Config.ALT_ENCHANT_ARMOR.get(Config.ALT_ENCHANT_ARMOR.size() - 1) : (double) Config.ALT_ENCHANT_ARMOR.get(item.getEnchantLevel()) : Config.ENCHANT_CHANCE_ARMOR;
		else
		{
			if(itemType != 2)
			{
				System.out.println("WTF? Request to enchant " + item.getItemId());
				activeChar.sendPacket(EnchantResult.CANCELLED);
				activeChar.sendActionFailed();
				activeChar.sendPacket(Msg.SYSTEM_ERROR);
				inventory.addItem(removedScroll);
				return;
			}
			chance = removedScroll.isCrystallEnchantScroll() ? Config.ENCHANT_CHANCE_CRYSTAL_ACCESSORY : Config.USE_ALT_ENCHANT ? item.getEnchantLevel() >= Config.ALT_ENCHANT_JEWELRY.size() ? Config.ALT_ENCHANT_JEWELRY.get(Config.ALT_ENCHANT_JEWELRY.size() - 1) : (double) Config.ALT_ENCHANT_JEWELRY.get(item.getEnchantLevel()) : Config.ENCHANT_CHANCE_ACCESSORY;
		}
		if(Config.SERVICES_RATE_BONUS_E_ENABLED && !safeEnchant && activeChar.isPremium())
			chance += itemType == 0 ? Config.SERVICES_RATE_BONUS_E_W : itemType == 1 ? Config.SERVICES_RATE_BONUS_E_A : Config.SERVICES_RATE_BONUS_E_J;
		final boolean chanceOk = Rnd.chance(chance);
		final int se = item.getEnchantLevel();
		if(chanceOk)
		{
			if(item.getEnchantLevel() == 0)
				activeChar.sendPacket(new SystemMessage(62).addItemName(Integer.valueOf(item.getItemId())));
			else
			{
				final SystemMessage sm = new SystemMessage(63);
				sm.addNumber(Integer.valueOf(item.getEnchantLevel()));
				sm.addItemName(Integer.valueOf(item.getItemId()));
				activeChar.sendPacket(sm);
			}
			item.setEnchantLevel(Config.SET_SAFE_ENCHANT && safeEnchant ? full ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON : item.getEnchantLevel() + 1);
			item.updateDatabase();
			activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
			activeChar.sendPacket(EnchantResult.SUCCESS);
			Log.addLog(activeChar.toString() + "|Successfully enchanted|" + item.getItemId() + "|to+" + item.getEnchantLevel() + "|chance: " + chance, "enchants");
			if(activeChar.recording && item.isEquipped() && !ArrayUtils.contains(Config.BOTS_RT_EQUIP, item.getItemId()))
				activeChar.recBot(4, item.getItemId(), item.getEnchantLevel(), 0, 0, 0, 0);
		}
		else
		{
			Log.addLog(activeChar.toString() + "|Failed to enchant|" + item.getItemId() + "|+" + item.getEnchantLevel() + "|chance: " + chance, "enchants");
			if(blessed || Config.CRYSTAL_BLESSED && scroll.isCrystallEnchantScroll() || Config.ENCHANT_HERO_WEAPON && item.isHeroWeapon())
			{
				item.setEnchantLevel(Config.SAFE_ENCHANT ? full ? Config.SAFE_ENCHANT_FULL_BODY : Config.SAFE_ENCHANT_COMMON : 0);
				activeChar.sendPacket(new InventoryUpdate().addModifiedItem(item));
				activeChar.sendPacket(Msg.FAILED_IN_BLESSED_ENCHANT_THE_ENCHANT_VALUE_OF_THE_ITEM_BECAME_0);
				activeChar.sendPacket(EnchantResult.BLESSED_FAILED);
				if(activeChar.recording && item.isEquipped() && !ArrayUtils.contains(Config.BOTS_RT_EQUIP, item.getItemId()))
					activeChar.recBot(4, item.getItemId(), item.getEnchantLevel(), 0, 0, 0, 0);
			}
			else
			{
				final int enchantLevel = item.getEnchantLevel();
				final int itemId = item.getItemId();
				final ItemInstance destroyedItem = inventory.destroyItem(item.getObjectId(), 1L, true);
				if(destroyedItem == null)
				{
					RequestEnchantItem._log.warn("failed to destroy " + item.getObjectId() + " after unsuccessful enchant attempt by char " + activeChar.toString());
					activeChar.sendPacket(EnchantResult.CANCELLED);
					activeChar.sendActionFailed();
					return;
				}
				if(enchantLevel == 0)
					activeChar.sendPacket(new SystemMessage(64).addItemName(Integer.valueOf(itemId)));
				else
					activeChar.sendPacket(new SystemMessage(65).addNumber(Integer.valueOf(enchantLevel)).addItemName(Integer.valueOf(itemId)));
				Log.LogItem(activeChar, "EnchantFail", item);
				if(crystalId > 0 && item.getTemplate().getCrystalCount() > 0)
				{
					final ItemInstance crystalsToAdd = ItemTable.getInstance().createItem(crystalId);
					int count = (int) (item.getTemplate().getCrystalCount() * 0.87);
					if(destroyedItem.getEnchantLevel() > 3)
						count += (int) (item.getTemplate().getCrystalCount() * 0.25 * (destroyedItem.getEnchantLevel() - 3));
					if(count < 1)
						count = 1;
					crystalsToAdd.setCount(count);
					inventory.addItem(crystalsToAdd);
					activeChar.sendPacket(EnchantResult.FAILED);
					activeChar.sendPacket(new SystemMessage(53).addItemName(Integer.valueOf(crystalsToAdd.getItemId())).addNumber(Integer.valueOf(count)));
				}
				else
					activeChar.sendPacket(EnchantResult.FAILED_NO_CRYSTALS);
				activeChar.sendPacket(new StatusUpdate(activeChar.getObjectId()).addAttribute(14, activeChar.getCurrentLoad()));
				activeChar.sendPacket(new ItemList(activeChar, false));
			}
		}
		if(item.isWeapon() && item.isEquipped())
		{
			final Skill sk = ((WeaponTemplate) item.getTemplate()).getEnchant4Skill();
			if(sk != null)
			{
				boolean need = false;
				if(se > 3 && item.getEnchantLevel() < 4)
				{
					activeChar.removeSkill(sk, false);
					need = true;
				}
				else if(se < 4 && item.getEnchantLevel() > 3)
				{
					activeChar.addSkill(sk, false);
					need = true;
				}
				if(need)
					activeChar.sendPacket(new SkillList(activeChar));
			}
		}
		activeChar.setEnchantScroll(null);
		activeChar.broadcastUserInfo(true);
	}

	static
	{
		RequestEnchantItem._log = LoggerFactory.getLogger(RequestEnchantItem.class);
	}
}
