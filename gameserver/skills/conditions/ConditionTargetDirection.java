package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;
import l2s.gameserver.utils.PositionUtils;
import l2s.gameserver.utils.Util;

public class ConditionTargetDirection extends Condition
{
	private final PositionUtils.TargetDirection _dir;

	public ConditionTargetDirection(final PositionUtils.TargetDirection direction)
	{
		_dir = direction;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return Util.getDirectionTo(env.target, env.character) == _dir;
	}
}
