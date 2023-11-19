package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionTargetSelf extends Condition
{
	private final boolean _flag;

	public ConditionTargetSelf(final boolean flag)
	{
		_flag = flag;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.target.getObjectId() == env.character.getObjectId() == _flag;
	}
}
