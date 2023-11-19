package l2s.gameserver.model.items.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.ArmorSet;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.tables.ArmorSetsTable;
import l2s.gameserver.tables.SkillTable;

public final class ArmorSetListener implements PaperdollListener
{
	public static short SET_COMMON_SKILL_ID;
	protected static final Logger _log;
	Inventory _inv;

	public ArmorSetListener(final Inventory inv)
	{
		_inv = inv;
	}

	@Override
	public void notifyEquipped(final int slot, final ItemInstance item)
	{
		if(!_inv.getOwner().isPlayer())
			return;
		final Player player = (Player) _inv.getOwner();
		final ItemInstance chestItem = _inv.getPaperdollItem(10);
		if(chestItem == null)
			return;
		final ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
		if(armorSet == null)
			return;
		boolean update = false;
		if(armorSet.containItem(slot, item.getItemId()))
		{
			if(armorSet.containAll(player))
			{
				final Skill skill = SkillTable.getInstance().getInfo(armorSet.getSkillId(), 1);
				final Skill commonSetSkill = SkillTable.getInstance().getInfo(ArmorSetListener.SET_COMMON_SKILL_ID, 1);
				if(skill != null)
				{
					player.addSkill(skill, false);
					player.addSkill(commonSetSkill, false);
					update = true;
				}
				else
					ArmorSetListener._log.warn("ArmorSetListener: Incorrect set skill: " + armorSet.getSkillId() + " chest: " + armorSet.getChestId());
				if(armorSet.containShield(player))
				{
					final Skill skills = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
					if(skills != null)
					{
						player.addSkill(skills, false);
						update = true;
					}
					else
						ArmorSetListener._log.warn("ArmorSetListener: Incorrect shield skill: " + armorSet.getShieldSkillId() + " chest: " + armorSet.getChestId());
				}
				if(armorSet.isEnchanted6(player))
				{
					final int skillId = armorSet.getEnchant6skillId();
					if(skillId > 0)
					{
						final Skill skille = SkillTable.getInstance().getInfo(skillId, 1);
						if(skille != null)
						{
							player.addSkill(skille, false);
							update = true;
						}
						else
							ArmorSetListener._log.warn("ArmorSetListener: Incorrect enchant6 skill: " + armorSet.getEnchant6skillId() + " chest: " + armorSet.getChestId());
					}
				}
			}
		}
		else if(armorSet.containShield(item.getItemId()) && armorSet.containAll(player))
		{
			final Skill skills2 = SkillTable.getInstance().getInfo(armorSet.getShieldSkillId(), 1);
			if(skills2 != null)
			{
				player.addSkill(skills2, false);
				update = true;
			}
			else
				ArmorSetListener._log.warn("ArmorSetListener: Incorrect shield skill: " + armorSet.getShieldSkillId() + " chest: " + armorSet.getChestId());
		}
		if(update)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}

	@Override
	public void notifyUnequipped(final int slot, final ItemInstance item)
	{
		boolean remove = false;
		int removeSkillId1 = 0;
		int removeSkillId2 = 0;
		int removeSkillId3 = 0;
		if(slot == 10)
		{
			final ArmorSet armorSet = ArmorSetsTable.getInstance().getSet(item.getItemId());
			if(armorSet == null)
				return;
			remove = true;
			removeSkillId1 = armorSet.getSkillId();
			removeSkillId2 = armorSet.getShieldSkillId();
			removeSkillId3 = armorSet.getEnchant6skillId();
		}
		else
		{
			final ItemInstance chestItem = _inv.getPaperdollItem(10);
			if(chestItem == null)
				return;
			final ArmorSet armorSet2 = ArmorSetsTable.getInstance().getSet(chestItem.getItemId());
			if(armorSet2 == null)
				return;
			if(armorSet2.containItem(slot, item.getItemId()))
			{
				remove = true;
				removeSkillId1 = armorSet2.getSkillId();
				removeSkillId2 = armorSet2.getShieldSkillId();
				removeSkillId3 = armorSet2.getEnchant6skillId();
			}
			else if(armorSet2.containShield(item.getItemId()))
			{
				remove = true;
				removeSkillId2 = armorSet2.getShieldSkillId();
			}
		}
		boolean update = false;
		if(remove)
		{
			if(removeSkillId1 != 0)
			{
				final Skill skill = SkillTable.getInstance().getInfo(removeSkillId1, 1);
				final Skill commonSetSkill = SkillTable.getInstance().getInfo(ArmorSetListener.SET_COMMON_SKILL_ID, 1);
				if(skill != null)
				{
					((Player) _inv.getOwner()).removeSkill(skill, false);
					((Player) _inv.getOwner()).removeSkill(commonSetSkill, false);
					update = true;
				}
				else
					ArmorSetListener._log.warn("ArmorSetListener: Incorrect remove1 skill: " + removeSkillId1 + ".");
			}
			if(removeSkillId2 != 0)
			{
				final Skill skill = SkillTable.getInstance().getInfo(removeSkillId2, 1);
				if(skill != null)
				{
					_inv.getOwner().removeSkill(skill);
					update = true;
				}
				else
					ArmorSetListener._log.warn("ArmorSetListener: Incorrect remove2 skill: " + removeSkillId2 + ".");
			}
			if(removeSkillId3 != 0)
			{
				final Skill skill = SkillTable.getInstance().getInfo(removeSkillId3, 1);
				if(skill != null)
				{
					_inv.getOwner().removeSkill(skill);
					update = true;
				}
				else
					ArmorSetListener._log.warn("ArmorSetListener: Incorrect remove3 skill: " + removeSkillId3 + ".");
			}
		}
		if(update)
		{
			_inv.getOwner().sendPacket(new SkillList((Player) _inv.getOwner()));
			_inv.getOwner().updateStats();
		}
	}

	static
	{
		ArmorSetListener.SET_COMMON_SKILL_ID = 3006;
		_log = LoggerFactory.getLogger(ArmorSetListener.class);
	}
}
