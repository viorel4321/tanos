package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class Balance extends Skill
{
	public Balance(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		double summaryCurrentHp = 0.0;
		int summaryMaximumHp = 0;
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isAlikeDead())
					continue;
				summaryCurrentHp += target.getCurrentHp();
				summaryMaximumHp += target.getMaxHp();
			}
		final double percent = summaryCurrentHp / summaryMaximumHp;
		for(final Creature target2 : targets)
			if(target2 != null)
			{
				if(target2.isAlikeDead())
					continue;
				final double hp = target2.getMaxHp() * percent;
				if(hp > target2.getCurrentHp())
					target2.setCurrentHp(hp, false);
				else
					target2.setCurrentHp(Math.max(1.01, hp), false);
				this.getEffects(activeChar, target2, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
