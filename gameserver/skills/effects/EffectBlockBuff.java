package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectBlockBuff extends Abnormal
{
	public EffectBlockBuff(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getEffected().setBlockBuff(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		getEffected().setBlockBuff(false);
	}

	@Override
	public boolean onActionTime()
	{
		return true;
	}
}
