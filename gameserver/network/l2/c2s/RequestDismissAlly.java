package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;

public class RequestDismissAlly extends L2GameClientPacket
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
		final Alliance alliance = clan.getAlliance();
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
		if(alliance.getMembersCount() > 1)
		{
			activeChar.sendPacket(new SystemMessage(524));
			return;
		}
		ClanTable.getInstance().dissolveAlly(activeChar);
	}
}
