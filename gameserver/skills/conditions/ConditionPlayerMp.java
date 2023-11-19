package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerMp extends Condition
{
	private final float _mp;

	public ConditionPlayerMp(final int mp)
	{
		_mp = mp / 100.0f;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.getCurrentMp() <= _mp * env.character.getMaxMp();
	}
}
