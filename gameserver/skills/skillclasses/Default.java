package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.templates.StatsSet;

public class Default extends Skill
{
	public Default(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		activeChar.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Default.NotImplemented").addNumber(getId()).addString("" + getSkillType()));
		activeChar.sendActionFailed();
	}
}
