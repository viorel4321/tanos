package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class Continuous extends Skill
{
	public Continuous(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(_numCharges > 0)
			activeChar.setIncreasedForce(activeChar.getIncreasedForce() - _numCharges);
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				if(getSkillType() == SkillType.BUFF && target != activeChar)
				{
					if(target.isCursedWeaponEquipped())
						continue;
					if(activeChar.isCursedWeaponEquipped())
						continue;
				}
				final boolean reflected = target.checkReflectSkill(activeChar, this);
				final Creature realTarget = reflected ? activeChar : target;
				final double mult = 0.01 * realTarget.calcStat(Stats.DEATH_VULNERABILITY, activeChar, this);
				final double lethal1 = _lethal1 * mult;
				final double lethal2 = _lethal2 * mult;
				if(lethal1 > 0.0 && Rnd.chance(lethal1))
				{
					if(realTarget.isPlayer())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentCp(), activeChar, this, 0, false, true, true, false, false, false, false, true);
						realTarget.sendPacket(Msg.YOU_HAVE_BEEN_STRUCK_BY_THE_INSTANT_KILL_SKILL);
						activeChar.sendPacket(Msg.INSTANT_KILL);
					}
					else if(realTarget.isNpc() && !realTarget.isLethalImmune())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentHp() / 2.0, activeChar, this, 0, false, true, true, false, false, false, false, true);
						activeChar.sendPacket(Msg.INSTANT_KILL);
					}
				}
				else if(lethal2 > 0.0 && Rnd.chance(lethal2))
					if(realTarget.isPlayer())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentHp() + realTarget.getCurrentCp() - 1.0, activeChar, this, 0, false, true, true, false, false, false, false, true);
						realTarget.sendPacket(Msg.YOU_HAVE_BEEN_STRUCK_BY_THE_INSTANT_KILL_SKILL);
						activeChar.sendPacket(Msg.INSTANT_KILL);
					}
					else if(realTarget.isNpc() && !realTarget.isLethalImmune())
					{
						realTarget.reduceCurrentHp(realTarget.getCurrentHp() - 1.0, activeChar, this, 0, false, true, true, false, false, false, false, true);
						activeChar.sendPacket(Msg.INSTANT_KILL);
					}
				this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}
		if(isSSPossible() && (!Config.SAVING_SPS || _skillType != SkillType.BUFF))
			activeChar.unChargeShots(isMagic());
	}
}
