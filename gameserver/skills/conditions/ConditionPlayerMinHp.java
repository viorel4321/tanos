package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerMinHp extends Condition
{
	private final float _hp;

	public ConditionPlayerMinHp(final int hp)
	{
		_hp = hp;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.getCurrentHp() > _hp;
	}
}
