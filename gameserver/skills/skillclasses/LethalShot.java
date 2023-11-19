package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class LethalShot extends Skill
{
	public LethalShot(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		final boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
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
					final boolean reflected = target.checkReflectSkill(activeChar, this);
					final Creature realTarget = reflected ? activeChar : target;
					final boolean shld = Formulas.calcShldUse(activeChar, realTarget);
					double damage = Formulas.calcPhysDam(activeChar, realTarget, this, shld, false, false, ss);
					if(damage != 1.0)
					{
						final double mult = 0.01 * realTarget.calcStat(Stats.DEATH_VULNERABILITY, activeChar, this);
						final double lethal1 = _lethal1 * mult;
						final double lethal2 = _lethal2 * mult;
						if(lethal1 > 0.0 && Rnd.chance(lethal1))
						{
							if(realTarget.isPlayer())
							{
								damage += realTarget.getCurrentCp();
								realTarget.sendPacket(Msg.YOU_HAVE_BEEN_STRUCK_BY_THE_INSTANT_KILL_SKILL);
								activeChar.sendPacket(Msg.INSTANT_KILL);
							}
							else if(realTarget.isNpc())
							{
								if(realTarget.isLethalImmune())
									damage *= 2.0;
								else
									damage += realTarget.getCurrentHp() / 2.0;
								activeChar.sendPacket(Msg.INSTANT_KILL);
							}
						}
						else if(lethal2 > 0.0 && Rnd.chance(lethal2))
							if(realTarget.isPlayer())
							{
								damage = realTarget.getCurrentHp() + realTarget.getCurrentCp() - 1.0 >= damage ? realTarget.getCurrentHp() + realTarget.getCurrentCp() - 1.1 : damage;
								realTarget.sendPacket(Msg.YOU_HAVE_BEEN_STRUCK_BY_THE_INSTANT_KILL_SKILL);
								activeChar.sendPacket(Msg.INSTANT_KILL);
							}
							else if(realTarget.isNpc())
							{
								if(realTarget.isLethalImmune())
									damage *= 3.0;
								else
									damage = realTarget.getCurrentHp() - 1.0 > damage ? realTarget.getCurrentHp() - 1.0 : damage;
								activeChar.sendPacket(Msg.INSTANT_KILL);
							}
					}
					if(damage < 1.0)
						damage = 1.0;
					realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, false, false, false, true);
					this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
				}
			}
		if(ss)
			activeChar.unChargeShots(false);
	}
}
