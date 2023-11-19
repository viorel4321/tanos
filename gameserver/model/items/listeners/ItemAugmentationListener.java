package l2s.gameserver.model.items.listeners;

import l2s.gameserver.data.xml.holder.OptionDataHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.templates.OptionDataTemplate;

public final class ItemAugmentationListener implements PaperdollListener
{
	private Inventory _inv;

	public ItemAugmentationListener(final Inventory inv)
	{
		_inv = inv;
	}

	@Override
	public void notifyUnequipped(int slot, ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;

		if(!item.isAugmented())
			return;

		Player player = _inv.getOwner().getPlayer();

		boolean updateStats = false;
		boolean sendSkillList = false;

		int[] stats = { item.getVariation1Id(), item.getVariation2Id() };
		for(int i : stats)
		{
			OptionDataTemplate template = player.removeOptionData(i);
			if(template == null)
				continue;

			updateStats = true;

			if(!template.getSkills().isEmpty())
				sendSkillList = true;
		}

		if(updateStats)
		{
			if(sendSkillList)
				player.sendPacket(new SkillList(player));

			player.updateStats();
		}
	}

	@Override
	public void notifyEquipped(final int slot, final ItemInstance item)
	{
		if(_inv.getOwner() == null || !_inv.getOwner().isPlayer() || !item.isEquipable())
			return;
		if(!item.isAugmented())
			return;

		Player player = _inv.getOwner().getPlayer();

		// При несоотвествии грейда аугмент не применяется
		if(player.getExpertisePenalty(item) > 0)
			return;

		boolean updateStats = false;
		boolean sendSkillList = false;

		int[] stats = { item.getVariation1Id(), item.getVariation2Id() };
		for(int i : stats)
		{
			OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(i);
			if(template == null)
				continue;

			if(player.addOptionData(template) == template)
				continue;

			updateStats = true;

			if(!template.getSkills().isEmpty())
				sendSkillList = true;
		}

		if(updateStats)
		{
			if(sendSkillList)
				player.sendPacket(new SkillList(player));

			player.sendChanges();
		}
	}
}
