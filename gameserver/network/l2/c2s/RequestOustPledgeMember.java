package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDelete;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAll;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestOustPledgeMember extends L2GameClientPacket
{
	private String _target;

	@Override
	public void readImpl()
	{
		_target = readS();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || (activeChar.getClanPrivileges() & 0x40) != 0x40)
			return;
		final Clan clan = activeChar.getClan();
		final ClanMember member = clan.getClanMember(_target);
		if(member == null)
		{
			activeChar.sendPacket(new SystemMessage(234));
			return;
		}
		if(member.isOnline() && member.getPlayer().isInCombat())
		{
			activeChar.sendPacket(new SystemMessage(1117));
			return;
		}
		clan.removeClanMember(_target);
		clan.broadcastToOnlineMembers(new SystemMessage(191).addString(_target));
		clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(_target));
		clan.setExpelledMember();
		final Player player = member.getPlayer();
		if(player != null)
		{
			player.setClan(null);
			if(!player.isNoble())
				player.setTitle("");
			player.sendPacket(new SystemMessage(199));
			if(Config.PENALTY_BY_CLAN_DISMISS)
				player.setLeaveClanCurTime();
			player.broadcastUserInfo(true);
			player.sendPacket(new PledgeShowMemberListDeleteAll());
		}
	}
}
