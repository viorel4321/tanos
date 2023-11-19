package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.instances.FeedableBeastInstance;
import l2s.gameserver.templates.StatsSet;

public class BeastFeed extends Skill
{
	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!(target instanceof FeedableBeastInstance))
		{
			activeChar.sendPacket(Msg.INCORRECT_TARGET);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	public BeastFeed(final StatsSet set)
	{
		super(set);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
				((FeedableBeastInstance) target).onSkillUse((Player) activeChar, _id);
	}
}
