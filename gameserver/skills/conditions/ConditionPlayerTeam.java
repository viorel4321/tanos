package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerTeam extends Condition
{
	private final int _value;

	public ConditionPlayerTeam(final int v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.getTeam() == _value;
	}
}
