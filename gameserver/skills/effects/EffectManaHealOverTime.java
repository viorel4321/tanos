package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectManaHealOverTime extends Abnormal
{
	public EffectManaHealOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		final double addToMp = calc();
		if(addToMp > 0.0)
			_effected.setCurrentMp(_effected.getCurrentMp() + addToMp);
		return true;
	}
}
