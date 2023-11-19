package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionTargetLevel extends Condition
{
	private final int _level;

	public ConditionTargetLevel(final int level)
	{
		_level = level;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.target != null && env.target.getLevel() >= _level;
	}
}
