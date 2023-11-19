package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.EventHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.entity.events.EventType;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestDuelAnswerStart extends L2GameClientPacket
{
	private int _response;
	private int _duelType;

	@Override
	protected void readImpl()
	{
		_duelType = readD();
		readD();
		_response = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final Transaction request = activeChar.getTransaction();
		if(request == null)
			return;
		if(!request.isValid() || !request.isTypeOf(Transaction.TransactionType.DUEL) || !request.isInProgress())
		{
			request.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		if(activeChar.isActionsDisabled())
		{
			request.cancel();
			activeChar.sendActionFailed();
			return;
		}
		final Player requestor = request.getOtherPlayer(activeChar);
		if(requestor == null)
		{
			request.cancel();
			activeChar.sendPacket(Msg.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			activeChar.sendActionFailed();
			return;
		}
		final DuelEvent duelEvent = EventHolder.getInstance().getEvent(EventType.PVP_EVENT, _duelType);
		switch(_response)
		{
			case 0:
			{
				request.cancel();
				if(_duelType == 1)
				{
					requestor.sendPacket(Msg.THE_OPPOSING_PARTY_HAS_DECLINED_YOUR_CHALLENGE_TO_A_DUEL);
					break;
				}
				requestor.sendPacket(new SystemMessage(1935).addName(activeChar));
				break;
			}
			case -1:
			{
				request.cancel();
				requestor.sendMessage(activeChar.getName() + " is set to refuse duel requests and cannot receive a duel request.");
				break;
			}
			case 1:
			{
				if(!duelEvent.canDuel(requestor, activeChar, false))
				{
					request.cancel();
					return;
				}
				SystemMessage msg1;
				SystemMessage msg2;
				if(_duelType == 1)
				{
					msg1 = new SystemMessage(1933);
					msg2 = new SystemMessage(1934);
				}
				else
				{
					msg1 = new SystemMessage(1930);
					msg2 = new SystemMessage(1929);
				}
				activeChar.sendPacket(msg1.addName(requestor));
				requestor.sendPacket(msg2.addName(activeChar));
				try
				{
					duelEvent.createDuel(requestor, activeChar);
					requestor.setCanUseSelectedSub(true);
					activeChar.setCanUseSelectedSub(true);
				}
				finally
				{
					request.cancel();
				}
				break;
			}
		}
	}
}
