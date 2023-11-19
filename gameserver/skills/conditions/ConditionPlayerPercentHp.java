package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerPercentHp extends Condition
{
	private final float _hp;

	public ConditionPlayerPercentHp(final int hp)
	{
		_hp = hp / 100.0f;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.getCurrentHpRatio() <= _hp;
	}
}
