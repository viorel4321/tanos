package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerOlympiad extends Condition
{
	private final boolean _value;

	public ConditionPlayerOlympiad(final boolean v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.isInOlympiadMode() == _value;
	}
}
