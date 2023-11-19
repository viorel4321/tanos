package l2s.gameserver.skills.effects;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;

public class EffectLDManaDamOverTime extends Abnormal
{
	public EffectLDManaDamOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		double manaDam = calc();
		manaDam *= _effected.getLevel() / 2.4;
		if(manaDam > _effected.getCurrentMp() && getSkill().isToggle())
		{
			_effected.sendPacket(Msg.SKILL_WAS_REMOVED_DUE_TO_LACK_OF_MP);
			return false;
		}
		_effected.reduceCurrentMp(manaDam, null);
		return true;
	}
}
