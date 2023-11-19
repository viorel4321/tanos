package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class PDam extends Skill
{
	public PDam(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		final boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		boolean reflected = false;
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				if(Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0.0, activeChar, this)))
				{
					if(activeChar.isPlayer())
						activeChar.sendPacket(new SystemMessage(43));
					target.sendPacket(new SystemMessage(42).addName(activeChar));
				}
				else
				{
					reflected = target.checkReflectSkill(activeChar, this);
					final Creature realTarget = reflected ? activeChar : target;
					final boolean shld = !getShieldIgnore() && Formulas.calcShldUse(activeChar, realTarget);
					final double damage = Formulas.calcPhysDam(activeChar, realTarget, this, shld, false, false, ss);
					if(damage >= 1.0)
						realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, true, false, false, true);
					if(!reflected)
						realTarget.doCounterAttack(this, activeChar, false);
				}
				this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}
		if(_numCharges > 0)
			activeChar.setIncreasedForce(activeChar.getIncreasedForce() - _numCharges);
		if(isSuicideAttack())
		{
			activeChar.doDie(null);
			activeChar.onDecay();
		}
		else if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
