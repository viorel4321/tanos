package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class AllyLeave extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		final Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(470));
			return;
		}
		if(clan.getAlliance() == null)
		{
			activeChar.sendPacket(new SystemMessage(465));
			return;
		}
		if(clan.equals(clan.getAlliance().getLeader()))
		{
			activeChar.sendPacket(new SystemMessage(471));
			return;
		}
		clan.broadcastToOnlineMembers(new SystemMessage(519));
		clan.broadcastToOnlineMembers(new SystemMessage(468));
		final Alliance alliance = clan.getAlliance();
		clan.setAllyId(0);
		clan.setLeavedAlly();
		clan.broadcastClanStatus(true, true, true);
		alliance.removeAllyMember(clan.getClanId());
	}
}
