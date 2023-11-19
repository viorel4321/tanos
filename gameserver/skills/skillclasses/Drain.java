package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class Drain extends Skill
{
	private float _absorbPart;
	private float _absorbAbs;

	public Drain(final StatsSet set)
	{
		super(set);
		_absorbPart = set.getFloat("absorbPart", 0.0f);
		_absorbAbs = set.getFloat("absorbAbs", 0.0f);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		final int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		final boolean corpseSkill = _targetType == SkillTargetType.TARGET_CORPSE;
		for(final Creature target : targets)
			if(target != null)
			{
				final boolean reflected = !corpseSkill && target.checkReflectSkill(activeChar, this);
				final Creature realTarget = reflected ? activeChar : target;
				if(this.getPower() > 0.0 || _absorbAbs > 0.0f)
				{
					if(realTarget.isDead() && !corpseSkill)
						continue;
					double hp = 0.0;
					final double targetHp = realTarget.getCurrentHp();
					if(!corpseSkill)
					{
						double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
						final double targetCP = realTarget.getCurrentCp();
						if(damage > targetCP || !realTarget.isPlayer())
							hp = (damage - targetCP) * _absorbPart;
						if(damage < 1.0)
							damage = 1.0;
						realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, false, false, false, true);
					}
					if(_absorbAbs == 0.0f && _absorbPart == 0.0f)
						continue;
					hp += _absorbAbs;
					if(hp > targetHp && !corpseSkill)
						hp = targetHp;
					if(hp > 0.0 && !realTarget.isDoor() && !realTarget.isInvul())
						activeChar.setCurrentHp(activeChar.getCurrentHp() + hp, false);
					if(realTarget.isDead() && corpseSkill && realTarget.isNpc())
					{
						activeChar.getAI().setAttackTarget(null);
						((NpcInstance) realTarget).endDecayTask();
					}
				}
				this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}
		if(sps != 0)
			activeChar.unChargeShots(true);
	}
}
