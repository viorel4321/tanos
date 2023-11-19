package l2s.gameserver.skills.conditions;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Env;

public class ConditionPlayerClasses extends Condition
{
	private final int[] _value;

	public ConditionPlayerClasses(final int[] v)
	{
		_value = v;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.character.isPlayer() && ((Player) env.character).getActiveClass() != null && ArrayUtils.contains(_value, ((Player) env.character).getActiveClassId());
	}
}
