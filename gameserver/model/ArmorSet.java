package l2s.gameserver.model;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;

public final class ArmorSet
{
	private final int _chest;
	private final int _legs;
	private final int _head;
	private final int _gloves;
	private final int _feet;
	private final int _skill_id;
	private final int _shield;
	private final int _shield_skill_id;
	private final int _enchant6skill;

	public ArmorSet(final int chest, final int legs, final int head, final int gloves, final int feet, final int skill_id, final int shield, final int shield_skill_id, final int enchant6skill)
	{
		_chest = chest;
		_legs = legs;
		_head = head;
		_gloves = gloves;
		_feet = feet;
		_skill_id = skill_id;
		_shield = shield;
		_shield_skill_id = shield_skill_id;
		_enchant6skill = enchant6skill;
	}

	public boolean containAll(final Player player)
	{
		final Inventory inv = player.getInventory();
		final ItemInstance legsItem = inv.getPaperdollItem(11);
		final ItemInstance headItem = inv.getPaperdollItem(6);
		final ItemInstance glovesItem = inv.getPaperdollItem(9);
		final ItemInstance feetItem = inv.getPaperdollItem(12);
		int legs = 0;
		int head = 0;
		int gloves = 0;
		int feet = 0;
		if(legsItem != null)
			legs = legsItem.getItemId();
		if(headItem != null)
			head = headItem.getItemId();
		if(glovesItem != null)
			gloves = glovesItem.getItemId();
		if(feetItem != null)
			feet = feetItem.getItemId();
		return containAll(_chest, legs, head, gloves, feet);
	}

	public boolean containAll(final int chest, final int legs, final int head, final int gloves, final int feet)
	{
		return (_chest == 0 || _chest == chest) && (_legs == 0 || _legs == legs) && (_head == 0 || _head == head) && (_gloves == 0 || _gloves == gloves) && (_feet == 0 || _feet == feet);
	}

	public boolean containItem(final int slot, final int itemId)
	{
		switch(slot)
		{
			case 10:
			{
				return _chest == itemId;
			}
			case 11:
			{
				return _legs == itemId;
			}
			case 6:
			{
				return _head == itemId;
			}
			case 9:
			{
				return _gloves == itemId;
			}
			case 12:
			{
				return _feet == itemId;
			}
			default:
			{
				return false;
			}
		}
	}

	public int getSkillId()
	{
		return _skill_id;
	}

	public boolean containShield(final Player player)
	{
		final Inventory inv = player.getInventory();
		final ItemInstance shieldItem = inv.getPaperdollItem(8);
		return shieldItem != null && shieldItem.getItemId() == _shield;
	}

	public boolean containShield(final int shield_id)
	{
		return _shield != 0 && _shield == shield_id;
	}

	public int getShieldSkillId()
	{
		return _shield_skill_id;
	}

	public int getEnchant6skillId()
	{
		return _enchant6skill;
	}

	public int getChestId()
	{
		return _chest;
	}

	public boolean isEnchanted6(final Player player)
	{
		if(!containAll(player))
			return false;
		final Inventory inv = player.getInventory();
		final ItemInstance chestItem = inv.getPaperdollItem(10);
		final ItemInstance legsItem = inv.getPaperdollItem(11);
		final ItemInstance headItem = inv.getPaperdollItem(6);
		final ItemInstance glovesItem = inv.getPaperdollItem(9);
		final ItemInstance feetItem = inv.getPaperdollItem(12);
		return chestItem.getEnchantLevel() >= 6 && (_legs == 0 || legsItem.getEnchantLevel() >= 6) && (_gloves == 0 || glovesItem.getEnchantLevel() >= 6) && (_head == 0 || headItem.getEnchantLevel() >= 6) && (_feet == 0 || feetItem.getEnchantLevel() >= 6);
	}
}
