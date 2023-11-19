package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;
import l2s.gameserver.model.base.Transaction;

public class AnswerJoinPartyRoom extends L2GameClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		if(_buf.hasRemaining())
			_response = readD();
		else
			_response = 0;
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final Transaction transaction = activeChar.getTransaction();
		if(transaction == null)
			return;
		if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.PARTY_ROOM))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		if(!transaction.isInProgress())
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isOutOfControl())
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}
		final Player requestor = transaction.getOtherPlayer(activeChar);
		if(requestor == null)
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			activeChar.sendActionFailed();
			return;
		}
		if(_response == 0)
		{
			transaction.cancel();
			requestor.sendPacket(Msg.THE_PLAYER_DECLINED_TO_JOIN_YOUR_PARTY);
			return;
		}
		if(activeChar.getPartyRoom() != null)
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}
		try
		{
			final PartyRoom room = requestor.getPartyRoom();
			if(room == null)
				return;
			room.addMember(activeChar);
		}
		finally
		{
			transaction.cancel();
		}
	}
}
