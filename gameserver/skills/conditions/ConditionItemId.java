package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public final class ConditionItemId extends Condition
{
	private final short _itemId;

	public ConditionItemId(final short itemId)
	{
		_itemId = itemId;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.item != null && env.item.getItemId() == _itemId;
	}
}
