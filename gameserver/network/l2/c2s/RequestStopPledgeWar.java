package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;

public class RequestStopPledgeWar extends L2GameClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		_pledgeName = readS(32);
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final Clan playerClan = activeChar.getClan();
		if(playerClan == null)
			return;
		if((activeChar.getClanPrivileges() & 0x20) != 0x20)
		{
			activeChar.sendPacket(new SystemMessage(794));
			activeChar.sendActionFailed();
			return;
		}
		final Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if(clan == null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestStopPledgeWar.NoSuchClan"));
			activeChar.sendActionFailed();
			return;
		}
		if(!playerClan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendPacket(new SystemMessage(1678));
			activeChar.sendActionFailed();
			return;
		}
		if(Config.NO_COMBAT_STOP_CLAN_WAR)
			for(final ClanMember mbr : playerClan.getMembers())
				if(mbr.isOnline() && mbr.getPlayer().isInCombat())
				{
					activeChar.sendPacket(new SystemMessage(1677));
					activeChar.sendActionFailed();
					return;
				}
		ClanTable.getInstance().stopClanWar(playerClan, clan);
	}
}
