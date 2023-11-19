package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.RecipeController;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.templates.StatsSet;

public class Craft extends Skill
{
	private final boolean _dwarven;

	public Craft(final StatsSet set)
	{
		super(set);
		_dwarven = set.getBool("isDwarven");
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		return activeChar.isPlayer() && !activeChar.isOutOfControl() && !activeChar.getPlayer().isInDuel();
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		RecipeController.getInstance().requestBookOpen(activeChar.getPlayer(), _dwarven);
	}
}
