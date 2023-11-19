package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectInterrupt extends Abnormal
{
	public EffectInterrupt(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(!getEffected().isRaid())
			getEffected().abortCast(true, true);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
