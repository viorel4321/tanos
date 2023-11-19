package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public final class ConditionUsingSkill extends Condition
{
	private final int _skillId;

	public ConditionUsingSkill(final int skillId)
	{
		_skillId = skillId;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return env.skill != null && env.skill.getId() == _skillId;
	}
}
