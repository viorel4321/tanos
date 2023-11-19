package l2s.gameserver.skills.effects;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public class EffectDamOverTime extends Abnormal
{
	private static int[] bleed;
	private static int[] poison;

	public EffectDamOverTime(final Env env, final EffectTemplate template)
	{
		super(env, template);
	}

	@Override
	public boolean onActionTime()
	{
		if(_effected.isDead())
			return false;
		double damage = calc();
		final int stack = (int) getStackOrder();
		if(damage < 2.0 && stack != -1)
			switch(getEffectType())
			{
				case Poison:
				{
					damage = EffectDamOverTime.poison[stack - 1] * getPeriod() / 1000L;
					break;
				}
				case Bleed:
				{
					damage = EffectDamOverTime.bleed[stack - 1] * getPeriod() / 1000L;
					break;
				}
			}
		damage = _effector.calcStat(getSkill().isMagic() ? Stats.MAGIC_DAMAGE : Stats.PHYSICAL_DAMAGE, damage, _effected, getSkill());
		if(damage > _effected.getCurrentHp() - 1.0 && !_effected.isNpc())
		{
			if(!getSkill().isOffensive())
				_effected.sendPacket(Msg.YOUR_SKILL_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_HP);
			return false;
		}
		if(getSkill().getAbsorbPart() > 0.0 && !_effected.isDoor())
			_effector.setCurrentHp(getSkill().getAbsorbPart() * Math.min(_effected.getCurrentHp(), damage) + _effector.getCurrentHp(), false);
		_effected.reduceCurrentHp(damage, _effector, getSkill(), 0, false, !_effected.isNpc() && _effected != _effector, _effected != _effector, _effector.isNpc() || _effected == _effector, false, false, true, false);
		return true;
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if(getSkill().getAbsorbPartStatic() > 0 && !_effected.isDoor())
			_effector.setCurrentHp(getSkill().getAbsorbPartStatic() + _effector.getCurrentHp(), false);
	}

	static
	{
		EffectDamOverTime.bleed = new int[] { 12, 17, 25, 34, 44, 54, 62, 67, 72, 77, 82, 87 };
		EffectDamOverTime.poison = new int[] { 11, 16, 24, 32, 41, 50, 58, 63, 68, 72, 77, 82 };
	}
}
