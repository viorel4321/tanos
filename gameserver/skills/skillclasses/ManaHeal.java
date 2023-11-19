package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class ManaHeal extends Skill
{
	public ManaHeal(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!isHandler() && getTargetType() == SkillTargetType.TARGET_ONE && (target == null || target.getSkillLevel(_id) > 0))
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		double mp = _power;
		final int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		if(sps > 0 && Config.MANAHEAL_SPS_BONUS)
			mp *= sps == 2 ? 1.5 : 1.3;
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				double addToMp = 0.0;
				if(isHandler())
					addToMp = _power;
				else
				{
					addToMp = mp * target.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, null, null) / 100.0;
					final int diff = target.getLevel() - getMagicLevel();
					if(getId() == 1013 && diff > 5)
						if(diff < 20)
							addToMp = addToMp / 100.0 * (100 - diff * 5);
						else
							addToMp = 0.0;
					if(addToMp == 0.0)
					{
						activeChar.sendPacket(new SystemMessage(1597).addSkillName(_id, getDisplayLevel()));
						this.getEffects(activeChar, target, getActivateRate() > 0, false);
						continue;
					}
				}
				if(addToMp > 0.0)
				{
					target.setCurrentMp(addToMp + target.getCurrentMp());
					if(target.isPlayer())
						if(activeChar != target)
							target.sendPacket(new SystemMessage(1069).addNumber(Integer.valueOf((int) addToMp)).addString(activeChar.getName()));
						else
							target.sendPacket(new SystemMessage(1068).addNumber(Integer.valueOf((int) addToMp)));
				}
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
