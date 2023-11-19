package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDelete;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAll;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestWithdrawalPledge extends L2GameClientPacket
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
		if(activeChar.getClanId() == 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(1116));
			return;
		}
		final Clan clan = activeChar.getClan();
		if(clan == null)
			return;
		final ClanMember member = clan.getClanMember(Integer.valueOf(activeChar.getObjectId()));
		if(member == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		clan.removeClanMember(activeChar.getObjectId());
		clan.broadcastToOnlineMembers(new SystemMessage(223).addString(activeChar.getName()));
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(activeChar.getName()));
		activeChar.sendPacket(new SystemMessage(199));
		activeChar.setClan(null);
		if(!activeChar.isNoble())
			activeChar.setTitle("");
		activeChar.setLeaveClanCurTime();
		activeChar.broadcastUserInfo(true);
		activeChar.sendPacket(new PledgeShowMemberListDeleteAll());
	}
}
