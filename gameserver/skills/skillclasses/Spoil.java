package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class Spoil extends Skill
{
	private final boolean _crush;

	public Spoil(final StatsSet set)
	{
		super(set);
		_crush = set.getBool("crush", false);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		final boolean ss = activeChar.getChargedSoulShot();
		if(ss && _crush)
			activeChar.unChargeShots(false);
		for(final Creature target : targets)
			if(target != null && !target.isDead())
			{
				if(target.isMonster())
					if(isSpoilUse(target))
					{
						if(((MonsterInstance) target).isSpoiled())
							activeChar.sendPacket(new SystemMessage(357));
						else
						{
							final MonsterInstance monster = (MonsterInstance) target;
							boolean success;
							if(!Config.ALT_SPOIL_FORMULA)
							{
								final int monsterLevel = monster.getLevel();
								final int modifier = Math.abs(monsterLevel - activeChar.getLevel());
								double rateOfSpoil = Config.BASE_SPOIL_RATE;
								if(modifier > 8)
									rateOfSpoil -= rateOfSpoil * (modifier - 8) * 9.0 / 100.0;
								rateOfSpoil = rateOfSpoil * getMagicLevel() / monsterLevel;
								if(rateOfSpoil < Config.MINIMUM_SPOIL_RATE)
									rateOfSpoil = Config.MINIMUM_SPOIL_RATE;
								else if(rateOfSpoil > 99.0)
									rateOfSpoil = 99.0;
								if(((Player) activeChar).isGM())
									activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Spoil.Chance").addNumber((int) rateOfSpoil));
								success = Rnd.chance(rateOfSpoil);
							}
							else
								success = Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate());
							if(success)
							{
								monster.setSpoiled(true, (Player) activeChar);
								activeChar.sendPacket(new SystemMessage(612));
							}
							else
								activeChar.sendPacket(new SystemMessage(1597).addSkillName(_id, getDisplayLevel()));
						}
					}
					else
						activeChar.sendPacket(new SystemMessage(1597).addSkillName(_id, getDisplayLevel()));
				if(_crush)
				{
					final boolean shld = !getShieldIgnore() && Formulas.calcShldUse(activeChar, target);
					final double damage = Formulas.calcPhysDam(activeChar, target, this, shld, false, false, ss);
					target.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, true, false, false, true);
					target.doCounterAttack(this, activeChar, false);
				}
				this.getEffects(activeChar, target, false, false);
				target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Math.max(_effectPoint, 1));
			}
	}

	private boolean isSpoilUse(final Creature target)
	{
		return getLevel() != 1 || target.getLevel() <= 22 || getId() != 254;
	}
}
