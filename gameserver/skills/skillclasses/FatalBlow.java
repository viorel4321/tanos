package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.PlaySound;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class FatalBlow extends Skill
{
	private final boolean _onCrit;
	private final boolean _directHp;

	public FatalBlow(final StatsSet set)
	{
		super(set);
		_onCrit = set.getBool("onCrit", false);
		_directHp = set.getBool("directHp", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(activeChar.isParalyzed())
			return;
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
					final double mult = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, activeChar, this);
					double lethal1;
					double lethal2;
					if(activeChar.isPlayer() && target.isPlayer())
					{
						lethal1 = _lethalPvP1 * mult;
						lethal2 = _lethalPvP2;
					}
					else
					{
						lethal1 = _lethal1 * mult;
						lethal2 = _lethal2 * mult;
					}
					if(_onCrit && !Formulas.calcBlow(activeChar, target, this))
						activeChar.sendPacket(Msg.MISSED_TARGET);
					else
					{
						final boolean reflected = target.checkReflectSkill(activeChar, this);
						final Creature realTarget = reflected ? activeChar : target;
						final boolean shld = Formulas.calcShldUse(activeChar, realTarget);
						boolean lethal3 = false;
						double damage = Formulas.calcBlowDamage(activeChar, realTarget, this, shld, ss);
						if(lethal1 > 0.0 && Rnd.chance(lethal1))
						{
							if(realTarget.isPlayer())
							{
								lethal3 = true;
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
								lethal3 = true;
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
						if(damage < 1.0)
							damage = 1.0;
						activeChar.displayGiveDamageMessage(realTarget, true, false, false);
						if(_onCrit)
							activeChar.broadcastPacket(new PlaySound("SkillSound.Critical_Hit_02"));
						realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, !lethal3 && _directHp, !lethal3, false, false, true);
						if(!reflected)
							realTarget.doCounterAttack(this, activeChar, true);
						this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
					}
				}
			}
		if(ss)
			activeChar.unChargeShots(false);
	}
}
