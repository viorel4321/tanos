package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Env;

public class EffectCPHealPercent extends Abnormal
{
	public EffectCPHealPercent(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public void onStart()
	{
		super.onStart();
		final double addToCp = calc() * _effected.getMaxCp() / 100.0;
		if(addToCp > 0.0)
		{
			_effected.sendPacket(new SystemMessage(1405).addNumber(Integer.valueOf((int) addToCp)));
			_effected.setCurrentCp(addToCp + _effected.getCurrentCp());
		}
	}

	@Override
	public boolean onActionTime()
	{
		return false;
	}
}
