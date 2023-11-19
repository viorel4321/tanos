package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerHp extends Condition
{
	private final float _hp;

	public ConditionPlayerHp(final int hp)
	{
		_hp = hp / 100.0f;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.getCurrentHp() <= _hp * env.character.getMaxHp();
	}
}
