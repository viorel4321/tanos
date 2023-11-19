package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Env;

public class ConditionPlayerMaxPK extends Condition
{
	private final int _pk;

	public ConditionPlayerMaxPK(final int pk)
	{
		_pk = pk;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.isPlayer() && ((Player) env.character).getPkKills() <= _pk;
	}
}
