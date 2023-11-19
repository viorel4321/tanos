package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.AskJoinAlliance;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestJoinAlly extends L2GameClientPacket
{
	private int _id;

	@Override
	public void readImpl()
	{
		_id = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getClan() == null || activeChar.getAlliance() == null)
			return;
		if(activeChar.isOutOfControl())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getAlliance().getMembersCount() >= Config.ALT_MAX_ALLY_SIZE)
		{
			activeChar.sendPacket(new SystemMessage(518));
			return;
		}
		final Player target = GameObjectsStorage.getPlayer(_id);
		if(target == null)
		{
			activeChar.sendPacket(Msg.TARGET_IS_NOT_FOUND_IN_THE_GAME);
			return;
		}
		if(target.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.isAllyLeader())
		{
			activeChar.sendPacket(new SystemMessage(464));
			return;
		}
		if(target.getAlliance() != null || activeChar.getAlliance().isMember(target.getClan().getClanId()))
		{
			final SystemMessage sm = new SystemMessage(691);
			sm.addString(target.getClan().getName());
			sm.addString(target.getAlliance().getAllyName());
			activeChar.sendPacket(sm);
			return;
		}
		if(!target.isClanLeader())
		{
			activeChar.sendPacket(new SystemMessage(9).addString(target.getName()));
			return;
		}
		if(activeChar.isAtWarWith(target.getClanId()) > 0)
		{
			activeChar.sendPacket(new SystemMessage(469));
			return;
		}
		if(!target.getClan().canJoinAlly())
		{
			final SystemMessage sm = new SystemMessage(761);
			sm.addString(target.getClan().getName());
			activeChar.sendPacket(sm);
			return;
		}
		if(!activeChar.getAlliance().canInvite())
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestJoinAlly.InvitePenalty"));
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		if(target.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(153).addString(target.getName()));
			return;
		}
		new Transaction(Transaction.TransactionType.ALLY, activeChar, target, 10000L);
		final SystemMessage sm = new SystemMessage(527);
		sm.addString(activeChar.getAlliance().getAllyName());
		sm.addString(activeChar.getName());
		target.sendPacket(sm, new AskJoinAlliance(activeChar.getObjectId(), activeChar.getName(), activeChar.getAlliance().getAllyName()));
	}
}
