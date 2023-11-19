package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public final class ConditionTargetHasBuffId extends Condition
{
	private final int _id, _level;

	public ConditionTargetHasBuffId(int id, int level)
	{
		_id = id;
		_level = level;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.target != null && env.target.getAbnormalList().getEffectsBySkillId(_id) != null;
	}
}
