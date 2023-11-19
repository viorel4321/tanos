package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.instances.DoorInstance;
import l2s.gameserver.skills.Env;

public class ConditionTargetCastleDoor extends Condition
{
	private final boolean _isCastleDoor;

	public ConditionTargetCastleDoor(final boolean isCastleDoor)
	{
		_isCastleDoor = isCastleDoor;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.target instanceof DoorInstance == _isCastleDoor;
	}
}
