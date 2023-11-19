package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public abstract class ConditionInventory extends Condition implements ConditionListener
{
	protected final short _slot;

	public ConditionInventory(final short slot)
	{
		_slot = slot;
	}

	@Override
	protected abstract boolean testImpl(final Env p0);
}
