package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class CPDam extends Skill
{
	public CPDam(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		final boolean ss = activeChar.getChargedSoulShot() && isSSPossible();
		if(ss)
			activeChar.unChargeShots(false);
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				final boolean reflected = target.checkReflectSkill(activeChar, this);
				final Creature realTarget = reflected ? activeChar : target;
				if(realTarget.getCurrentCp() < 1.0)
					continue;
				double damage = _power * realTarget.getCurrentCp();
				if(damage < 1.0)
					damage = 1.0;
				realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, true, false, false, true);
				if(!reflected)
					realTarget.doCounterAttack(this, activeChar, false);
				this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}
	}
}
