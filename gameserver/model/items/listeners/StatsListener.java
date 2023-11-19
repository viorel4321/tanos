package l2s.gameserver.model.items.listeners;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;

public final class StatsListener implements PaperdollListener
{
	Inventory _inv;

	public StatsListener(final Inventory inv)
	{
		_inv = inv;
	}

	@Override
	public void notifyUnequipped(final int slot, final ItemInstance item)
	{
		_inv.getOwner().removeStatsOwner(item);
		_inv.getOwner().updateStats();
	}

	@Override
	public void notifyEquipped(final int slot, final ItemInstance item)
	{
		_inv.getOwner().addStatFuncs(item.getStatFuncs());
		_inv.getOwner().updateStats();
	}
}
