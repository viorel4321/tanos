package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class CombatPointHealPercent extends Skill
{
	public CombatPointHealPercent(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null && !target.isDead())
			{
				if(!target.isPlayer())
					continue;
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
				final double addToCp = _power * target.getMaxCp() / 100.0;
				if(addToCp <= 0.0)
					continue;
				target.setCurrentCp(target.getCurrentCp() + addToCp);
				if(activeChar != target)
					target.sendPacket(new SystemMessage(1406).addString(activeChar.getName()).addNumber(Integer.valueOf((int) addToCp)));
				else
					target.sendPacket(new SystemMessage(1405).addNumber(Integer.valueOf((int) addToCp)));
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
