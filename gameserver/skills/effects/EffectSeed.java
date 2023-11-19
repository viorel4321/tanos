package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectSeed extends Abnormal
{
	private int _power;

	public EffectSeed(final Env env, final EffectTemplate template)
	{
		super(env, template);
		_power = 1;
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}

	public int getPower()
	{
		return _power;
	}

	public void increasePower()
	{
		++_power;
	}
}
