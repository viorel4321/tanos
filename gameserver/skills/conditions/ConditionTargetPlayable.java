package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.skills.Env;

public class ConditionTargetPlayable extends Condition
{
	private final boolean _flag;

	public ConditionTargetPlayable(final boolean flag)
	{
		_flag = flag;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		final Creature target = env.target;
		return target != null && target.isPlayable() == _flag;
	}
}
