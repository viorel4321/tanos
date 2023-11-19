package l2s.gameserver.network.l2.c2s;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.TradeList;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.SendTradeDone;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class TradeDone extends L2GameClientPacket
{
	private static Logger _log = LoggerFactory.getLogger(TradeDone.class);

	private int _response;

	@Override
	public void readImpl()
	{
		_response = readD();
	}

	@Override
	public void runImpl()
	{
		synchronized(getClient())
		{
			final Player activeChar = getClient().getActiveChar();
			if(activeChar == null)
				return;

			final Transaction transaction = activeChar.getTransaction();
			final Player requestor;
			if(transaction == null || (requestor = transaction.getOtherPlayer(activeChar)) == null)
			{
				if(transaction != null)
					transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				return;
			}
			if(activeChar.isOutOfControl())
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				return;
			}
			if(activeChar.isInStoreMode() || requestor.isInStoreMode())
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail);
				activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				requestor.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
				return;
			}
			if(!transaction.isTypeOf(Transaction.TransactionType.TRADE))
			{
				transaction.cancel();
				activeChar.sendPacket(SendTradeDone.Fail, Msg.ActionFail, new SystemMessage("Something wrong. Maybe, cheater?"));
				requestor.sendPacket(SendTradeDone.Fail, Msg.ActionFail, new SystemMessage("Something wrong. Maybe, cheater?"));
				return;
			}
			if(_response == 1)
			{
				if(!activeChar.isInRangeZ(requestor, 200L))
				{
					activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
					return;
				}
				transaction.confirm(activeChar);
				requestor.sendPacket(new SystemMessage(121).addString(activeChar.getName()), Msg.TradePressOtherOk);
				if(!transaction.isConfirmed(activeChar) || !transaction.isConfirmed(requestor))
				{
					activeChar.sendActionFailed();
					return;
				}
				activeChar.getInventory().writeLock();
				requestor.getInventory().writeLock();
				try
				{
					final boolean trade1Valid = TradeList.validateTrade(activeChar, transaction.getExchangeList(activeChar), requestor);
					final boolean trade2Valid = TradeList.validateTrade(requestor, transaction.getExchangeList(requestor), activeChar);
					if(trade1Valid && trade2Valid)
					{
						transaction.tradeItems();
						requestor.sendPacket(Msg.TRADE_HAS_BEEN_SUCCESSFUL, SendTradeDone.Success);
						activeChar.sendPacket(Msg.TRADE_HAS_BEEN_SUCCESSFUL, SendTradeDone.Success);
					}
					else
					{
						activeChar.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED, SendTradeDone.Fail);
						requestor.sendPacket(Msg.THE_ATTEMPT_TO_TRADE_HAS_FAILED, SendTradeDone.Fail);
					}
				}
				finally
				{
					requestor.getInventory().writeUnlock();
					activeChar.getInventory().writeUnlock();
				}
			}
			else
			{
				activeChar.sendPacket(SendTradeDone.Fail);
				requestor.sendPacket(SendTradeDone.Fail, new SystemMessage(124).addString(activeChar.getName()));
			}
			transaction.cancel();
		}
	}
}
