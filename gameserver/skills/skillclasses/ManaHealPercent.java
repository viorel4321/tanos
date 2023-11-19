package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class ManaHealPercent extends Skill
{
	public ManaHealPercent(final StatsSet set)
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
				final double addToMp = _power * target.getMaxMp() / 100.0;
				if(addToMp <= 0.0)
					continue;
				target.setCurrentMp(target.getCurrentMp() + addToMp);
				if(!target.isPlayer())
					continue;
				if(activeChar != target)
					target.sendPacket(new SystemMessage(1069).addNumber(Integer.valueOf((int) addToMp)).addString(activeChar.getName()));
				else
					target.sendPacket(new SystemMessage(1068).addNumber(Integer.valueOf((int) addToMp)));
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
