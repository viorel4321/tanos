package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.JoinPledge;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListAdd;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListAll;
import l2s.gameserver.network.l2.s2c.PledgeSkillList;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestAnswerJoinPledge extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
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
		answer(activeChar, _response);
	}

	protected static void answer(final Player activeChar, final int response)
	{
		final Transaction transaction = activeChar.getTransaction();
		if(transaction == null)
			return;
		if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.CLAN))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		final Player requestor = transaction.getOtherPlayer(activeChar);
		transaction.cancel();
		if(requestor.getClan() == null || activeChar.getClanId() != 0)
			return;
		if(response == 1)
		{
			if(activeChar.isInOlympiadMode())
			{
				activeChar.sendMessage("You can't do it in Olympiad.");
				return;
			}
			final int pledgeType = activeChar.getPledgeType();
			final Clan clan = requestor.getClan();
			if(clan.getSubPledgeMembersCount(pledgeType) >= clan.getSubPledgeLimit(pledgeType))
			{
				if(pledgeType == 0)
					requestor.sendPacket(new SystemMessage(1835).addString(clan.getName()));
				else
					requestor.sendPacket(new SystemMessage(233));
				activeChar.sendMessage("You can't do it, because the clan " + clan.getName() + " is full.");
				return;
			}
			if(activeChar.canJoinClan())
			{
				activeChar.sendPacket(new JoinPledge(requestor.getClanId()));
				clan.broadcastToOnlineMembers(new SystemMessage(222).addString(activeChar.getName()));
				clan.addClanMember(activeChar);
				activeChar.setClan(clan);
				clan.getClanMember(activeChar.getName()).setPlayerInstance(activeChar);
				if(clan.isAcademy(pledgeType))
					activeChar.setLvlJoinedAcademy(activeChar.getLevel());
				clan.getClanMember(activeChar.getName()).setPowerGrade(clan.getAffiliationRank(pledgeType));
				clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(clan.getClanMember(activeChar.getName())), activeChar);
				clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
				activeChar.sendPacket(new SystemMessage(195), new PledgeShowMemberListAll(clan, activeChar));
				activeChar.setLeaveClanTime(0L);
				activeChar.updatePledgeClass();
				clan.addAndShowSkillsToPlayer(activeChar);
				activeChar.sendPacket(new PledgeSkillList(clan));
				activeChar.sendPacket(new SkillList(activeChar));
				EventHolder.getInstance().findEvent(activeChar);
				activeChar.broadcastUserInfo(true);
				activeChar.broadcastRelationChanged();
			}
			else
			{
				requestor.sendPacket(new SystemMessage(231));
				activeChar.sendPacket(new SystemMessage(232));
				activeChar.setPledgeType(0);
			}
		}
		else
		{
			requestor.sendPacket(new SystemMessage(196).addString(activeChar.getName()));
			activeChar.setPledgeType(0);
		}
		requestor.getClan().block_invite = 0L;
	}
}
