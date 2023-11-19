package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Env;

public class EffectManaHealPercent extends Abnormal
{
	public EffectManaHealPercent(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		final double addToMp = calc() * _effected.getMaxMp() / 100.0;
		if(addToMp > 0.0)
		{
			_effected.sendPacket(new SystemMessage(1068).addNumber(Integer.valueOf((int) addToMp)));
			_effected.setCurrentMp(addToMp + _effected.getCurrentMp());
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
