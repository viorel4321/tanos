package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectCombatPointHealOverTime extends Abnormal
{
	public EffectCombatPointHealOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;
		final double addToCp = calc();
		if(addToCp > 0.0)
			_effected.setCurrentCp(_effected.getCurrentCp() + addToCp);
		return true;
	}
}
