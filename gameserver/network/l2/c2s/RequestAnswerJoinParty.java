package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.s2c.JoinParty;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestAnswerJoinParty extends L2GameClientPacket
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
		if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.PARTY))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		final Player requestor = transaction.getOtherPlayer(activeChar);
		final Party party = requestor.getParty();
		transaction.cancel();
		if(party == null || party.getPartyLeader() == null)
		{
			activeChar.sendPacket(Msg.ActionFail);
			requestor.sendPacket(new JoinParty(0));
			return;
		}
		final SystemMessage problem = activeChar.canJoinParty(requestor);
		if(problem != null)
		{
			activeChar.sendPacket(problem, Msg.ActionFail);
			requestor.sendPacket(new JoinParty(0));
			return;
		}
		requestor.sendPacket(new JoinParty(response));
		if(response == 1)
		{
			if(activeChar.isInZone(Zone.ZoneType.OlympiadStadia))
			{
				activeChar.sendMessage("A party cannot be formed in this area.");
				requestor.sendMessage("A party cannot be formed in this area.");
				return;
			}
			if(party.getMemberCount() >= 9)
			{
				activeChar.sendPacket(Msg.PARTY_IS_FULL);
				requestor.sendPacket(Msg.PARTY_IS_FULL);
				return;
			}
			activeChar.joinParty(party, false);
		}
		else
		{
			requestor.sendPacket(new SystemMessage(305));
			if(party != null && party.getMemberCount() == 1)
				requestor.setParty(null);
		}
	}
}
