package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionTargetPercentHp extends Condition
{
	private final double _hp;

	public ConditionTargetPercentHp(final int hp)
	{
		_hp = hp / 100.0;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.target != null && env.target.getCurrentHpRatio() <= _hp;
	}
}
