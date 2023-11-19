package l2s.gameserver.skills.effects;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectDestroySummon extends Abnormal
{
	public EffectDestroySummon(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean checkCondition()
	{
		return _effected.isSummon() && super.checkCondition();
	}

	@Override
	public void onStart()
	{
		super.onStart();
		((Servitor) _effected).unSummon();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
