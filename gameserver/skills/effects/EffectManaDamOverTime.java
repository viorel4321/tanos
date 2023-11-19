package l2s.gameserver.skills.effects;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectManaDamOverTime extends Abnormal
{
	public EffectManaDamOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		final double manaDam = calc();
		if(manaDam > _effected.getCurrentMp() && getSkill().isToggle())
		{
			_effected.sendPacket(Msg.SKILL_WAS_REMOVED_DUE_TO_LACK_OF_MP);
			return false;
		}
		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}
