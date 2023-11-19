package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectImobileBuff extends Abnormal
{
	public EffectImobileBuff(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		_effected.setImmobilized(true);
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.setImmobilized(false);
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
