package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionPlayerRiding extends Condition
{
	private final CheckPlayerRiding _riding;

	public ConditionPlayerRiding(final CheckPlayerRiding riding)
	{
		_riding = riding;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return _riding == CheckPlayerRiding.STRIDER && env.character.isRiding() || _riding == CheckPlayerRiding.WYVERN && env.character.isFlying() || _riding == CheckPlayerRiding.NONE && !env.character.isRiding() && !env.character.isFlying();
	}

	public enum CheckPlayerRiding
	{
		NONE,
		STRIDER,
		WYVERN;
	}
}
