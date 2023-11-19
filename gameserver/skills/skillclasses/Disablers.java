package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class Disablers extends Skill
{
	public Disablers(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null && !target.isDead())
			{
				final boolean reflected = target.checkReflectSkill(activeChar, this);
				this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
