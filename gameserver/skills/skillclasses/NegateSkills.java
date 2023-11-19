package l2s.gameserver.skills.skillclasses;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Util;

public class NegateSkills extends Skill
{
	private final int[] _negateSkills;

	public NegateSkills(final StatsSet set)
	{
		super(set);
		_negateSkills = Util.parseCommaSeparatedIntegerArray(set.getString("negateSkills", ""));
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				final List<Abnormal> effects = target.getAbnormalList().values();
				for(final Abnormal e : effects)
					if(ArrayUtils.contains(_negateSkills, e.getSkill().getId()))
					{
						target.sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
						e.exit();
					}
				this.getEffects(activeChar, target, getActivateRate() > 0, false);
			}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
