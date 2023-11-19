package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.s2c.ExAskJoinMPCC;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestExMPCCAskJoin extends L2GameClientPacket
{
	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!activeChar.isInParty())
		{
			activeChar.sendPacket(new SystemMessage(1593));
			return;
		}
		Player target = World.getPlayer(_name);
		if(target == null)
		{
			activeChar.sendPacket(new SystemMessage(161));
			return;
		}
		if(activeChar == target)
		{
			activeChar.sendActionFailed();
			return;
		}
		for(final Player member : activeChar.getParty().getPartyMembers())
			if(member == target)
				return;
		if(!target.isInParty())
		{
			activeChar.sendPacket(new SystemMessage(152));
			return;
		}
		if(target.isInParty() && !target.getParty().isLeader(target))
			target = target.getParty().getPartyLeader();
		if(target == null)
		{
			activeChar.sendPacket(new SystemMessage(161));
			return;
		}
		if(target.getParty().isInCommandChannel())
		{
			activeChar.sendPacket(new SystemMessage(1594).addString(target.getName()));
			return;
		}
		if(target.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(153).addString(target.getName()));
			return;
		}
		final Party activeParty = activeChar.getParty();
		if(activeParty.isInCommandChannel())
		{
			if(activeParty.getCommandChannel().getChannelLeader() != activeChar)
			{
				activeChar.sendPacket(new SystemMessage(1593));
				return;
			}
			sendInvite(activeChar, target);
		}
		else if(CommandChannel.checkAuthority(activeChar))
			sendInvite(activeChar, target);
	}

	private void sendInvite(final Player requestor, final Player target)
	{
		new Transaction(Transaction.TransactionType.CHANNEL, requestor, target, 30000L);
		target.sendPacket(new ExAskJoinMPCC(requestor.getName()));
		requestor.sendMessage("You invited " + target.getName() + " to your Command Channel.");
	}
}
