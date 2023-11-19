package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class MDam extends Skill
{
	public MDam(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(Creature activeChar, Set<Creature> targets)
	{
		final int sps = isSSPossible() ? isMagic() ? activeChar.getChargedSpiritShot() : activeChar.getChargedSoulShot() ? 2 : 0 : 0;
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				final boolean reflected = target.checkReflectSkill(activeChar, this);
				final Creature realTarget = reflected ? activeChar : target;
				final double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
				if(damage >= 1.0)
					realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, false, false, false, true);
				this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}
		if(isSuicideAttack())
		{
			activeChar.doDie(null);
			activeChar.onDecay();
		}
		else if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
