package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.skills.Env;

public final class ConditionSlotItemId extends ConditionInventory
{
	private final int _itemId;
	private final int _enchantLevel;

	public ConditionSlotItemId(int slot, int itemId, int enchantLevel)
	{
		super((short) slot);
		_itemId = itemId;
		_enchantLevel = enchantLevel;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		if(!env.character.isPlayer())
			return false;
		final Inventory inv = ((Player) env.character).getInventory();
		final ItemInstance item = inv.getPaperdollItem(_slot);
		if(item == null)
			return _itemId == 0;
		return item.getItemId() == _itemId && item.getEnchantLevel() >= _enchantLevel;
	}
}
