package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListUpdate;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestPledgeReorganizeMember extends L2GameClientPacket
{
	int _replace;
	String _subjectName;
	int _targetUnit;
	String _replaceName;

	@Override
	public void readImpl()
	{
		_replace = readD();
		_subjectName = readS();
		_targetUnit = readD();
		if(_replace > 0)
			_replaceName = readS();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final Clan clan = activeChar.getClan();
		if(clan == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.isClanLeader())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.ChangeAffiliations"));
			activeChar.sendActionFailed();
			return;
		}
		final ClanMember subject = clan.getClanMember(_subjectName);
		if(subject == null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.NotInYourClan"));
			activeChar.sendActionFailed();
			return;
		}
		if(subject.getPledgeType() == _targetUnit)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.AlreadyInThatCombatUnit"));
			activeChar.sendActionFailed();
			return;
		}
		if(_targetUnit != 0 && clan.getSubPledge(_targetUnit) == null)
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.NoSuchCombatUnit"));
			activeChar.sendActionFailed();
			return;
		}
		if(clan.isAcademy(_targetUnit))
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.AcademyViaInvitation"));
			activeChar.sendActionFailed();
			return;
		}
		if(clan.isAcademy(subject.getPledgeType()))
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.CantMoveAcademyMember"));
			activeChar.sendActionFailed();
			return;
		}
		ClanMember replacement = null;
		if(_replace > 0)
		{
			replacement = clan.getClanMember(_replaceName);
			if(replacement == null)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.CharacterNotBelongClan"));
				activeChar.sendActionFailed();
				return;
			}
			if(replacement.getPledgeType() != _targetUnit)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.CharacterNotBelongCombatUnit"));
				activeChar.sendActionFailed();
				return;
			}
			if(replacement.isSubLeader() != 0)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.CharacterLeaderAnotherCombatUnit"));
				activeChar.sendActionFailed();
				return;
			}
		}
		else
		{
			if(clan.getSubPledgeMembersCount(_targetUnit) >= clan.getSubPledgeLimit(_targetUnit))
			{
				if(_targetUnit == 0)
					activeChar.sendPacket(new SystemMessage(1835).addString(clan.getName()));
				else
					activeChar.sendPacket(new SystemMessage(233));
				activeChar.sendActionFailed();
				return;
			}
			if(subject.isSubLeader() != 0)
			{
				activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestPledgeReorganizeMember.MemberLeaderAnotherUnit"));
				activeChar.sendActionFailed();
				return;
			}
		}
		if(replacement != null)
		{
			replacement.setPledgeType(subject.getPledgeType());
			if(replacement.getPowerGrade() > 5)
				replacement.setPowerGrade(clan.getAffiliationRank(replacement.getPledgeType()));
			clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(replacement));
			if(replacement.isOnline())
			{
				replacement.getPlayer().updatePledgeClass();
				replacement.getPlayer().broadcastUserInfo(true);
			}
		}
		subject.setPledgeType(_targetUnit);
		if(subject.getPowerGrade() > 5)
			subject.setPowerGrade(clan.getAffiliationRank(subject.getPledgeType()));
		clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subject));
		if(subject.isOnline())
		{
			subject.getPlayer().updatePledgeClass();
			subject.getPlayer().broadcastUserInfo(true);
		}
	}
}
