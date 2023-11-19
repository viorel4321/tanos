package l2s.gameserver.skills.skillclasses;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.templates.StatsSet;

public class NegateStats extends Skill
{
	private final List<Stats> _negateStats;
	private final boolean _negateOffensive;

	public NegateStats(final StatsSet set)
	{
		super(set);
		final String[] negateStats = set.getString("negateStats", "").split(" ");
		_negateStats = new ArrayList<Stats>(negateStats.length);
		for(final String stat : negateStats)
			_negateStats.add(Stats.valueOfXml(stat));
		_negateOffensive = set.getBool("negateDebuffs");
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
		{
			if(target == null)
				continue;
			if(!_negateOffensive && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
				continue;
			final List<Abnormal> effects = target.getAbnormalList().values();
			for(final Stats stat : _negateStats)
				for(final Abnormal e : effects)
				{
					final Skill skill = e.getSkill();
					if(skill.isOffensive() == _negateOffensive && e.containsStat(stat) && skill.isCancelable())
					{
						target.sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
						e.exit();
					}
				}
			this.getEffects(activeChar, target, getActivateRate() > 0, false);
		}
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	@Override
	public boolean isOffensive()
	{
		return !_negateOffensive;
	}

	public List<Stats> getNegateStats()
	{
		return _negateStats;
	}
}
