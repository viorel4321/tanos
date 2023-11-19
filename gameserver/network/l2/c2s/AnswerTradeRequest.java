package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.TradeStart;

public class AnswerTradeRequest extends L2GameClientPacket
{
	private int _response;

	@Override
	public void readImpl()
	{
		_response = readD();
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
		if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.TRADE_REQUEST))
		{
			transaction.cancel();
			if(_response == 1)
				activeChar.sendPacket(Msg.TARGET_IS_NOT_FOUND_IN_THE_GAME, Msg.ActionFail);
			else
				activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		final Player requestor = transaction.getOtherPlayer(activeChar);
		if(_response != 1 || activeChar.getPrivateStoreType() != 0)
		{
			requestor.sendPacket(new SystemMessage(119).addString(activeChar.getName()), Msg.ActionFail);
			transaction.cancel();
			if(activeChar.getPrivateStoreType() != 0)
				activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		transaction.cancel();
		new Transaction(Transaction.TransactionType.TRADE, activeChar, requestor);
		requestor.sendPacket(new SystemMessage(120).addString(activeChar.getName()), new TradeStart(requestor, activeChar));
		activeChar.sendPacket(new SystemMessage(120).addString(requestor.getName()), new TradeStart(activeChar, requestor));
		if(requestor.getTradeList() == null)
			requestor.setTradeList(new TradeList(0));
		if(activeChar.getTradeList() == null)
			activeChar.setTradeList(new TradeList(0));
	}
}
