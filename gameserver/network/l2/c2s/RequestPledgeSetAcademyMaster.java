package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.PledgeReceiveMemberInfo;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestPledgeSetAcademyMaster extends L2GameClientPacket
{
	static Logger _log;
	private int _mode;
	private String _sponsorName;
	private String _apprenticeName;

	@Override
	public void readImpl()
	{
		_mode = readD();
		_sponsorName = readS();
		_apprenticeName = readS();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final Clan clan = activeChar.getClan();
		if(clan == null)
			return;
		if((activeChar.getClanPrivileges() & 0x100) == 0x100)
		{
			final ClanMember sponsor = activeChar.getClan().getClanMember(_sponsorName);
			final ClanMember apprentice = activeChar.getClan().getClanMember(_apprenticeName);
			if(sponsor != null && apprentice != null)
			{
				if(apprentice.getPledgeType() != -1 || sponsor.getPledgeType() == -1)
					return;
				if(_mode == 1)
				{
					if(sponsor.hasApprentice())
					{
						activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestOustAlly.MemberAlreadyHasApprentice"));
						return;
					}
					if(apprentice.hasSponsor())
					{
						activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestOustAlly.ApprenticeAlreadyHasSponsor"));
						return;
					}
					sponsor.setApprentice(apprentice.getObjectId());
					clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
					clan.broadcastToOnlineMembers(new SystemMessage(1755).addString(sponsor.getName()).addString(apprentice.getName()));
				}
				else
				{
					if(!sponsor.hasApprentice())
					{
						activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestOustAlly.MemberHasNoApprentice"));
						return;
					}
					sponsor.setApprentice(0);
					clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(apprentice));
					clan.broadcastToOnlineMembers(new SystemMessage(1763).addString(sponsor.getName()).addString(apprentice.getName()));
				}
				if(apprentice.isOnline())
					apprentice.getPlayer().broadcastUserInfo(true);
				activeChar.sendPacket(new PledgeReceiveMemberInfo(sponsor));
			}
		}
		else
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestOustAlly.NoMasterRights"));
	}

	static
	{
		RequestPledgeSetAcademyMaster._log = LoggerFactory.getLogger(RequestPledgeSetAcademyMaster.class);
	}
}
