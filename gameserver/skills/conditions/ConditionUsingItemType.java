package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.skills.Env;

public final class ConditionUsingItemType extends Condition
{
	private final long _mask;

	public ConditionUsingItemType(final long mask)
	{
		_mask = mask;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		if(!env.character.isPlayer())
			return false;
		final Inventory inv = ((Player) env.character).getInventory();
		return (_mask & inv.getWearedMask()) != 0x0L;
	}
}
