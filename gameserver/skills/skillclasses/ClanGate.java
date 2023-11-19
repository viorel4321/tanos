package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.Location;

public class ClanGate extends Skill
{
	public ClanGate(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!activeChar.isPlayer())
			return false;
		final Player player = (Player) activeChar;
		if(!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(236));
			return false;
		}
		final SystemMessage msg = Call.canSummonHere(player);
		if(msg != null)
		{
			activeChar.sendPacket(msg);
			return false;
		}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		if(!activeChar.isPlayer())
			return;
		final Player player = (Player) activeChar;
		final Clan clan = player.getClan();
		clan.broadcastToOtherOnlineMembers(new SystemMessage(1923), player);
		final boolean ss = player.isIn7sDungeon();
		final Player[] onlineMembers;
		final Player[] ms = onlineMembers = clan.getOnlineMembers(player.getObjectId());
		for(final Player pl : onlineMembers)
			if(pl != null)
				if(Call.canBeSummoned(pl, ss) == null)
					pl.summonCharacterRequest(player, Location.findAroundPosition(player.getLoc(), 100, 150, player.getGeoIndex()), 5);
		this.getEffects(activeChar, activeChar, getActivateRate() > 0, true);
	}
}
