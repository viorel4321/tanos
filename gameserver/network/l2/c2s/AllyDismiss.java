package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;

public class AllyDismiss extends L2GameClientPacket
{
	String _clanName;

	@Override
	public void readImpl()
	{
		_clanName = readS();
	}

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
		final Clan leaderClan = activeChar.getClan();
		if(leaderClan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		final Alliance alliance = leaderClan.getAlliance();
		if(alliance == null)
		{
			activeChar.sendPacket(new SystemMessage(465));
			return;
		}
		if(!activeChar.isAllyLeader())
		{
			activeChar.sendPacket(new SystemMessage(464));
			return;
		}
		if(_clanName == null)
			return;
		final Clan clan = ClanTable.getInstance().getClanByName(_clanName);
		if(clan != null)
		{
			if(!alliance.isMember(clan.getClanId()))
			{
				activeChar.sendActionFailed();
				return;
			}
			if(alliance.getLeader().equals(clan))
			{
				activeChar.sendPacket(new SystemMessage(520));
				return;
			}
			clan.broadcastToOnlineMembers(new SystemMessage(2010).addString("Your clan has been expelled from " + alliance.getAllyName() + " alliance."));
			clan.broadcastToOnlineMembers(new SystemMessage(468));
			clan.setAllyId(0);
			clan.setLeavedAlly();
			clan.broadcastClanStatus(true, true, true);
			alliance.removeAllyMember(clan.getClanId());
			alliance.setExpelledMember();
			activeChar.sendMessage(clan.getName() + " has been dismissed from " + alliance.getAllyName());
		}
	}
}
