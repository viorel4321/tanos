package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayer extends Condition
{
	private final boolean _flag;

	public ConditionPlayer(final boolean flag)
	{
		_flag = flag;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.isPlayer() == _flag;
	}
}
