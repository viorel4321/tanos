package l2s.gameserver.skills.conditions;

import l2s.commons.util.Rnd;
import l2s.gameserver.skills.Env;

public class ConditionGameChance extends Condition
{
	private final int _chance;

	ConditionGameChance(final int chance)
	{
		_chance = chance;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return Rnd.chance(_chance);
	}
}
