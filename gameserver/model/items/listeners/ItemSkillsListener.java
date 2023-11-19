package l2s.gameserver.model.items.listeners;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.s2c.SkillCoolTime;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;

public final class ItemSkillsListener implements PaperdollListener
{
	Inventory _inv;

	public ItemSkillsListener(final Inventory inv)
	{
		_inv = inv;
	}

	@Override
	public void notifyUnequipped(final int slot, final ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer())
			return;

		final Player player = _inv.getOwner().getPlayer();
		Skill[] itemSkills = null;
		Skill enchant4Skill = null;
		ItemTemplate template = item.getTemplate();

		player.removeTriggers(template);

		itemSkills = template.getAttachedSkills();

		if(template instanceof WeaponTemplate)
			enchant4Skill = ((WeaponTemplate) template).getEnchant4Skill();

		if(itemSkills != null) {
			for (final Skill itemSkill : itemSkills)
				player.removeSkill(itemSkill, false);
		}

		if(enchant4Skill != null)
			player.removeSkill(enchant4Skill, false);

		if(itemSkills != null || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
		}
	}

	@Override
	public void notifyEquipped(final int slot, final ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer())
			return;

		final Player player = _inv.getOwner().getPlayer();
		if(item.getTemplate().getType2() == 0 && player.getExpertisePenalty(item) > 0)
			return;

		Skill[] itemSkills = null;
		Skill enchant4Skill = null;
		final ItemTemplate template = item.getTemplate();

		player.addTriggers(template);

		itemSkills = template.getAttachedSkills();

		if(template instanceof WeaponTemplate && item.getEnchantLevel() >= 4)
			enchant4Skill = ((WeaponTemplate) template).getEnchant4Skill();

		boolean needSendInfo = false;
		if(itemSkills != null && itemSkills.length > 0) {
			for (final Skill itemSkill : itemSkills) {
				player.addSkill(itemSkill, false);
				if (itemSkill.isActive()) {
					long reuseDelay = Formulas.calcSkillReuseDelay(player, itemSkill);
					reuseDelay = Math.min(reuseDelay, 30000L);
					if (reuseDelay > 0L && !player.isSkillDisabled(itemSkill)) {
						player.disableSkill(itemSkill, reuseDelay);
						needSendInfo = true;
					}
				}
			}
		}

		if(enchant4Skill != null)
			player.addSkill(enchant4Skill, false);

		if(itemSkills != null || enchant4Skill != null)
		{
			player.sendPacket(new SkillList(player));
			player.updateStats();
			if(needSendInfo)
				player.sendPacket(new SkillCoolTime(player));
		}
	}
}
