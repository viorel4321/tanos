package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.ExRegenMax;
import l2s.gameserver.skills.Env;

public class EffectHealOverTime extends Abnormal
{
	public EffectHealOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getEffected().isPlayer() && getCount() > 0 && getPeriod() > 0L)
			getEffected().sendPacket(new ExRegenMax(calc(), (int) (getCount() * getPeriod() / 1000L), Math.round(getPeriod() / 1000L)));
	}

	@Override
	public boolean onActionTime()
	{
		if(getEffected().isDead())
			return false;
		final double addToHp = calc();
		if(addToHp > 0.0)
			getEffected().setCurrentHp(_effected.getCurrentHp() + addToHp, false);
		return true;
	}
}
