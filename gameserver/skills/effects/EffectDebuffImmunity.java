package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectDebuffImmunity extends Abnormal
{
	public EffectDebuffImmunity(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().setDebuffImmunity(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().setDebuffImmunity(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
