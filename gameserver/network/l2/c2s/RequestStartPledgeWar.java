package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;

public class RequestStartPledgeWar extends L2GameClientPacket
{
	String _pledgeName;
	Clan _clan;

	@Override
	public void readImpl()
	{
		_pledgeName = readS(32);
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		_clan = activeChar.getClan();
		if(_clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if((activeChar.getClanPrivileges() & 0x20) != 0x20)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(_clan.getWarsCount() >= Config.AltClanWarMax)
		{
			activeChar.sendPacket(new SystemMessage(1570));
			activeChar.sendActionFailed();
			return;
		}
		if(_clan.getLevel() < Config.AltMinClanLvlForWar || _clan.getMembersCount() < Config.AltClanMembersForWar)
		{
			activeChar.sendPacket(new SystemMessage(1564));
			activeChar.sendActionFailed();
			return;
		}
		final Clan clan = ClanTable.getInstance().getClanByName(_pledgeName);
		if(clan == null)
		{
			activeChar.sendPacket(new SystemMessage(1565));
			activeChar.sendActionFailed();
			return;
		}
		if(_clan.equals(clan))
		{
			activeChar.sendPacket(new SystemMessage(1610));
			activeChar.sendActionFailed();
			return;
		}
		if(_clan.isAtWarWith(clan.getClanId()))
		{
			activeChar.sendPacket(new SystemMessage(1609));
			activeChar.sendActionFailed();
			return;
		}
		if(_clan.getAllyId() == clan.getAllyId() && _clan.getAllyId() != 0)
		{
			activeChar.sendPacket(new SystemMessage(1569));
			activeChar.sendActionFailed();
			return;
		}
		if(clan.getLevel() < Config.AltMinClanLvlForWar || clan.getMembersCount() < Config.AltClanMembersForWar)
		{
			activeChar.sendPacket(new SystemMessage(1564));
			activeChar.sendActionFailed();
			return;
		}
		ClanTable.getInstance().startClanWar(activeChar.getClan(), clan);
	}
}
