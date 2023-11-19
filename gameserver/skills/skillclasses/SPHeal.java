package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class SPHeal extends Skill
{
	public SPHeal(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		return activeChar.isPlayer() && super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
		for(final Creature target : targets)
		{
			if(target == null)
				continue;
			((Player) target).setSp(((Player) target).getSp() + (int) _power);
			((Player) target).sendChanges();
			this.getEffects(activeChar, target, getActivateRate() > 0, false);
			activeChar.sendPacket(new SystemMessage(369).addNumber(Integer.valueOf((int) _power)).addString("SP"));
		}
	}
}
