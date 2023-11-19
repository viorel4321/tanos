package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerMinLevel extends Condition
{
	private final int _level;

	public ConditionPlayerMinLevel(final int level)
	{
		_level = level;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.getLevel() >= _level;
	}
}
