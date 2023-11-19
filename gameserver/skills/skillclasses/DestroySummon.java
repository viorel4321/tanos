package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class DestroySummon extends Skill
{
	public DestroySummon(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null && target.isSummon())
				if(getActivateRate() > 0 && !Rnd.chance(getActivateRate()))
					activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getDisplayLevel()));
				else
				{
					((Servitor) target).unSummon();
					this.getEffects(activeChar, target, getActivateRate() > 0, false);
				}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
