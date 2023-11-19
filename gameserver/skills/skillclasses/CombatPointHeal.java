package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class CombatPointHeal extends Skill
{
	public CombatPointHeal(final StatsSet set)
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
				if(_power > 0.0)
				{
					target.setCurrentCp(_power + target.getCurrentCp());
					if(activeChar != target)
						target.sendPacket(new SystemMessage(1406).addString(activeChar.getName()).addNumber(Integer.valueOf((int) _power)));
					else
						target.sendPacket(new SystemMessage(1405).addNumber(Integer.valueOf((int) _power)));
				}
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
