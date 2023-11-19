package l2s.gameserver.skills.effects;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public final class EffectStun extends Abnormal
{
	public EffectStun(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		setPeriod(Rnd.get(1000L, getPeriod()));
		_effected.startStunning();
	}

	@Override
	public void onExit()
	{
		super.onExit();
		_effected.stopStunning();
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
