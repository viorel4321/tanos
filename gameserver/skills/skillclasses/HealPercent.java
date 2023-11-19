package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.SiegeHeadquarterInstance;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class HealPercent extends Skill
{
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		return target != null && !target.isDoor() && !(target instanceof SiegeHeadquarterInstance) && super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	public HealPercent(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
				final double addToHp = _power * target.getMaxHp() / 100.0;
				if(addToHp <= 0.0)
					continue;
				target.setCurrentHp(addToHp + target.getCurrentHp(), false);
				if(!target.isPlayer())
					continue;
				if(activeChar != target)
					target.sendPacket(new SystemMessage(1067).addNumber(Integer.valueOf((int) addToHp)).addString(activeChar.getName()));
				else
					target.sendPacket(new SystemMessage(1066).addNumber(Integer.valueOf((int) addToHp)));
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
