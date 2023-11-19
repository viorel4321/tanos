package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectInvulnerable extends Abnormal
{
	public EffectInvulnerable(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		if(_effected.isInvul())
			return false;
		final Skill skill = _effected.getCastingSkill();
		return (skill == null || skill.getSkillType() != Skill.SkillType.TAKECASTLE) && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.setIsInvul(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setIsInvul(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
