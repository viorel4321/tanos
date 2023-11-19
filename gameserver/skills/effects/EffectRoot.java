package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectRoot extends Abnormal
{
	public EffectRoot(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.startRooted();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopRooting();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
