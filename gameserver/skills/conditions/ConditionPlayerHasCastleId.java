package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.skills.Env;

public class ConditionPlayerHasCastleId extends Condition
{
	private final int _value;

	public ConditionPlayerHasCastleId(final int id)
	{
		_value = id;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		final Clan clan = env.character.getClan();
		return clan != null && clan.getHasCastle() == _value;
	}
}
