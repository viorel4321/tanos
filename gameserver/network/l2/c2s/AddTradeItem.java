package l2s.gameserver.network.l2.c2s;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeItem;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.TradeOtherAdd;
import l2s.gameserver.network.l2.s2c.TradeOwnAdd;
import l2s.gameserver.network.l2.s2c.TradeUpdate;

public class AddTradeItem extends L2GameClientPacket
{
	private int _tradeId;
	private int _objectId;
	private int _amount;

	@Override
	protected void readImpl()
	{
		_tradeId = readD();
		_objectId = readD();
		_amount = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _amount < 1)
			return;
		final Transaction transaction = activeChar.getTransaction();
		if(transaction == null)
			return;
		if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.TRADE))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		if(activeChar.isOutOfControl())
		{
			transaction.cancel();
			activeChar.sendActionFailed();
			return;
		}
		final Player requestor = transaction.getOtherPlayer(activeChar);
		if(transaction.isConfirmed(activeChar) || transaction.isConfirmed(requestor))
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_MOVE_ADDITIONAL_ITEMS_BECAUSE_TRADE_HAS_BEEN_CONFIRMED, Msg.ActionFail);
			return;
		}
		if(requestor.isOutOfControl())
		{
			transaction.cancel();
			requestor.sendActionFailed();
			return;
		}
		final ItemInstance InvItem = activeChar.getInventory().getItemByObjectId(_objectId);
		if(InvItem == null || !InvItem.canBeTraded(activeChar))
		{
			activeChar.sendPacket(Msg.THIS_ITEM_CANNOT_BE_TRADED_OR_SOLD);
			return;
		}
		final int InvItemCount = InvItem.getIntegerLimitedCount();
		TradeItem tradeItem = getItem(_objectId, transaction.getExchangeList(activeChar));
		int realCount = Math.min(_amount, InvItemCount);
		int leaveCount = InvItemCount - realCount;
		if(tradeItem == null)
		{
			tradeItem = new TradeItem(InvItem);
			tradeItem.setCount(realCount);
			transaction.getExchangeList(activeChar).add(tradeItem);
		}
		else
		{
			if(!InvItem.canBeTraded(activeChar))
				return;
			final int TradeItemCount = tradeItem.getCount();
			if(InvItemCount == TradeItemCount)
				return;
			try
			{
				if(_amount + TradeItemCount >= InvItemCount)
					realCount = InvItemCount - TradeItemCount;
			}
			catch(ArithmeticException e)
			{
				activeChar.sendPacket(Msg.SYSTEM_ERROR, Msg.ActionFail);
				return;
			}
			tradeItem.setCount(realCount + TradeItemCount);
			leaveCount = InvItemCount - realCount - TradeItemCount;
		}
		activeChar.sendPacket(new TradeOwnAdd(InvItem, realCount), new TradeUpdate(InvItem, leaveCount));
		requestor.sendPacket(new TradeOtherAdd(InvItem, realCount));
	}

	private static TradeItem getItem(final int objId, final ConcurrentLinkedQueue<TradeItem> collection)
	{
		for(final TradeItem item : collection)
			if(item.getObjectId() == objId)
				return item;
		return null;
	}
}
