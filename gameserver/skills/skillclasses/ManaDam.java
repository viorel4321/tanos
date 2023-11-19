package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class ManaDam extends Skill
{
	private final int _critChance;
	private final double _critDam;
	private final boolean _ignoreInvul;

	public ManaDam(final StatsSet set)
	{
		super(set);
		_critChance = set.getInteger("manaDamCritChance", 0);
		_critDam = set.getDouble("manaDamCritDam", 2.0);
		_ignoreInvul = set.getBool("ignoreInvul", true);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		final int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		for(final Creature target : targets)
		{
			if(getActivateRate() > 0 && !Rnd.chance(getActivateRate()))
				continue;
			if(target == null || target.isDead())
				continue;
			if(!_ignoreInvul && target.isInvul())
				continue;
			final boolean reflected = target.checkReflectSkill(activeChar, this);
			final Creature realTarget = reflected ? activeChar : target;
			double damage = Formulas.calcManaDam(activeChar, realTarget, this, sps);
			if(damage >= 1.0)
			{
				if(Rnd.chance(_critChance))
				{
					damage *= _critDam;
					activeChar.sendPacket(new SystemMessage(1280));
				}
				realTarget.reduceCurrentMp(damage, activeChar);
				if(activeChar != realTarget)
					activeChar.sendPacket(new SystemMessage(1867).addNumber(Integer.valueOf((int) damage)));
				realTarget.sendPacket(new SystemMessage(1866).addNumber(Integer.valueOf((int) damage)));
			}
			this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
		}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
