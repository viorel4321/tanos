package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public final class ConditionHasSkill extends Condition
{
	private final int _id;
	private final int _level;

	public ConditionHasSkill(int id, int level)
	{
		_id = id;
		_level = level;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.skill != null && env.character.getSkillLevel(_id) >= _level;
	}
}
