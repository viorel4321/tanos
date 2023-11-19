package l2s.gameserver.skills.effects;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public class EffectDamOverTimeLethal extends Abnormal
{
	public EffectDamOverTimeLethal(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		double damage = calc();
		if(getSkill().isOffensive())
			damage *= 2.0;
		damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());
		_effected.reduceCurrentHp(damage, _effector, getSkill(), 0, false, !_effected.isNpc() && _effected != _effector, _effected != _effector, _effector.isNpc() || _effected == _effector, false, false, true, false);
		return true;
	}
}
