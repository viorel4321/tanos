package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectAntiSummon extends Abnormal
{
	public EffectAntiSummon(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		return _effected.isSummon() && super.checkCondition();
	}

	@Override
	public boolean onActionTime()
	{
		((Servitor) _effected).unSummon();
		return true;
	}
}
