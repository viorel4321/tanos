package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.tables.FriendsTable;

public class RequestAnswerFriendInvite extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		_response = _buf.hasRemaining() ? readD() : 0;
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
		final Transaction transaction = activeChar.getTransaction();
		if(transaction == null)
			return;
		if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.FRIEND))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		final Player requestor = transaction.getOtherPlayer(activeChar);
		transaction.cancel();
		if(_response == 1 && !FriendsTable.getInstance().checkIsFriends(requestor.getObjectId(), activeChar.getObjectId()))
			FriendsTable.getInstance().addFriend(requestor, activeChar);
		else
			requestor.sendPacket(Msg.YOU_HAVE_FAILED_TO_INVITE_A_FRIEND);
	}
}
