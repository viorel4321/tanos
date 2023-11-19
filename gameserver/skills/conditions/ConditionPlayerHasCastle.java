package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.Env;

public class ConditionPlayerHasCastle extends Condition
{
	private final boolean _value;

	public ConditionPlayerHasCastle(final boolean v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		final Clan clan = env.character.getClan();
		return clan != null && clan.getHasCastle() > 0 == _value;
	}
}
