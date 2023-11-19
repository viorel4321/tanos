package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.skills.Env;

final class ConditionSlotItemType extends ConditionInventory
{
	private final int _mask;

	ConditionSlotItemType(final short slot, final int mask)
	{
		super(slot);
		_mask = mask;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		if(!env.character.isPlayer())
			return false;
		final Inventory inv = ((Player) env.character).getInventory();
		final ItemInstance item = inv.getPaperdollItem(_slot);
		return item != null && (item.getTemplate().getItemMask() & _mask) != 0x0L;
	}
}
