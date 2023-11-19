package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class Toggle extends Skill
{
	public Toggle(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(activeChar.getAbnormalList().getEffectsBySkillId(_id) != null)
		{
			activeChar.getAbnormalList().stop(_id);
			activeChar.sendActionFailed();
			return;
		}
		this.getEffects(activeChar, activeChar, getActivateRate() > 0, false);
	}
}
