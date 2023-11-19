package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.pledge.Alliance;

public class RequestAnswerJoinAlly extends L2GameClientPacket
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
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar != null)
		{
			final Transaction transaction = activeChar.getTransaction();
			if(transaction == null)
				return;
			if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.ALLY))
			{
				transaction.cancel();
				activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
				return;
			}
			final Player requestor = transaction.getOtherPlayer(activeChar);
			transaction.cancel();
			if(requestor.getAlliance() == null)
				return;
			if(_response == 1)
			{
				final Alliance ally = requestor.getAlliance();
				activeChar.sendPacket(Msg.YOU_HAVE_ACCEPTED_THE_ALLIANCE);
				activeChar.getClan().setAllyId(requestor.getAllyId());
				activeChar.getClan().updateClanInDB();
				ally.addAllyMember(activeChar.getClan(), true);
				ally.broadcastAllyStatus(true);
			}
			else
				requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_CLAN_INTO_THE_ALLIANCE);
		}
	}
}
