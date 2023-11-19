package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionTargetMob extends Condition
{
	private final boolean _isMob;

	public ConditionTargetMob(final boolean isMob)
	{
		_isMob = isMob;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.target.isMonster() == _isMob;
	}
}
