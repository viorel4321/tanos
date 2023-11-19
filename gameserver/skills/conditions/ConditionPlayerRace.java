package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.skills.Env;

public class ConditionPlayerRace extends Condition
{
	private final Race _race;

	public ConditionPlayerRace(final String race)
	{
		_race = Race.valueOf(race.toLowerCase());
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.isPlayer() && ((Player) env.character).getRace() == _race;
	}
}
