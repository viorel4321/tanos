package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestExMPCCAcceptJoin extends L2GameClientPacket
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
		final Transaction transaction = activeChar.getTransaction();
		if(transaction == null)
			return;
		if(!transaction.isValid() || !transaction.isTypeOf(Transaction.TransactionType.CHANNEL))
		{
			transaction.cancel();
			activeChar.sendPacket(Msg.TIME_EXPIRED, Msg.ActionFail);
			return;
		}
		final Player requestor = transaction.getOtherPlayer(activeChar);
		transaction.cancel();
		if(!requestor.isInParty() || !activeChar.isInParty() || activeChar.getParty().isInCommandChannel())
		{
			requestor.sendPacket(new SystemMessage(1591));
			return;
		}
		if(_response == 1)
		{
			if(activeChar.isTeleporting())
			{
				activeChar.sendPacket(new SystemMessage(1729));
				requestor.sendPacket(new SystemMessage(1591));
				return;
			}
			if(requestor.getParty().isInCommandChannel())
				requestor.getParty().getCommandChannel().addParty(activeChar.getParty());
			else if(CommandChannel.checkAuthority(requestor))
			{
				final boolean haveSkill = requestor.getSkillLevel(391) > 0;
				final boolean haveItem = activeChar.getInventory().getItemByItemId(8871) != null;
				if(!haveSkill && haveItem)
				{
					requestor.getInventory().destroyItemByItemId(8871, 1L, false);
					requestor.sendPacket(SystemMessage.removeItems(8871, 1L));
				}
				final CommandChannel channel = new CommandChannel(requestor);
				requestor.sendPacket(new SystemMessage(1580));
				channel.addParty(activeChar.getParty());
			}
		}
		else
			requestor.sendPacket(new SystemMessage(1680).addString(activeChar.getName()));
	}
}
