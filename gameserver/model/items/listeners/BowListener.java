package l2s.gameserver.model.items.listeners;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.templates.item.WeaponTemplate;

public final class BowListener implements PaperdollListener
{
	Inventory _inv;

	public BowListener(final Inventory inv)
	{
		_inv = inv;
	}

	@Override
	public void notifyUnequipped(final int slot, final ItemInstance item)
	{
		if(slot != 7 || _inv.isRefreshingListeners() || !item.isEquipable())
			return;
		if(item.getItemType() == WeaponTemplate.WeaponType.BOW || item.getItemType() == WeaponTemplate.WeaponType.ROD)
			_inv.unEquipItemInBodySlotAndNotify(256, null);
	}

	@Override
	public void notifyEquipped(final int slot, final ItemInstance item)
	{
		if(slot != 7 || _inv.isRefreshingListeners() || !item.isEquipable())
			return;
		if(item.getItemType() == WeaponTemplate.WeaponType.BOW)
		{
			final ItemInstance arrow = _inv.findArrowForBow(item.getTemplate());
			if(arrow != null)
				_inv.setPaperdollItem(8, arrow);
		}
		if(item.getItemType() == WeaponTemplate.WeaponType.ROD)
		{
			final ItemInstance bait = _inv.findEquippedLure();
			if(bait != null)
				_inv.setPaperdollItem(8, bait);
		}
	}
}
