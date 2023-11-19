package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.effects.EffectSeed;
import l2s.gameserver.templates.StatsSet;

public class Seed extends Skill
{
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(target == activeChar)
		{
			activeChar.sendPacket(new SystemMessage(51));
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	public Seed(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isAlikeDead())
					continue;
				final EffectSeed oldEffect = (EffectSeed) target.getAbnormalList().getEffectBySkillId(getId());
				if(oldEffect == null)
					this.getEffects(activeChar, target, false, false);
				else
					oldEffect.increasePower();
			}
		if(isSSPossible())
			activeChar.unChargeShots(true);
	}
}
