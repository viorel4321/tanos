package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Env;

public class EffectHealPercent extends Abnormal
{
	public EffectHealPercent(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		final double addToHp = calc() * _effected.getMaxHp() / 100.0;
		if(addToHp > 0.0)
		{
			_effected.sendPacket(new SystemMessage(1066).addNumber(Integer.valueOf((int) addToHp)));
			_effected.setCurrentHp(addToHp + _effected.getCurrentHp(), false);
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
