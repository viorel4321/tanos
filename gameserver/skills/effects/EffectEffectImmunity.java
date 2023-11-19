package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectEffectImmunity extends Abnormal
{
	public EffectEffectImmunity(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().startEffectImmunity();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().stopEffectImmunity();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
