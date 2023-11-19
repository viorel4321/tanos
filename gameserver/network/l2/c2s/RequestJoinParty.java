package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.AskJoinParty;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestJoinParty extends L2GameClientPacket
{
	private String _name;
	private int _itemDistribution;

	@Override
	public void readImpl()
	{
		_name = readS();
		_itemDistribution = readD();
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
		final Player target = GameObjectsStorage.getPlayer(_name);
		if(target == null || target == activeChar)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY, Msg.ActionFail);
			return;
		}
		final SystemMessage problem = target.canJoinParty(activeChar);
		if(problem != null)
		{
			activeChar.sendPacket(problem);
			return;
		}
		if(!activeChar.isInParty())
			createNewParty(_itemDistribution, target, activeChar);
		else
			addTargetToParty(_itemDistribution, target, activeChar);
	}

	private void addTargetToParty(final int itemDistribution, final Player target, final Player activeChar)
	{
		if(activeChar.getParty().getMemberCount() >= 9)
		{
			activeChar.sendPacket(new SystemMessage(155));
			return;
		}
		if(Config.PARTY_LEADER_ONLY_CAN_INVITE && !activeChar.getParty().isLeader(activeChar))
		{
			activeChar.sendPacket(Msg.ONLY_THE_LEADER_CAN_GIVE_OUT_INVITATIONS);
			return;
		}
		if(activeChar.getParty().isInDimensionalRift())
		{
			activeChar.sendMessage(new CustomMessage("l2s.gameserver.network.l2.c2s.RequestJoinParty.InDimensionalRift"));
			activeChar.sendActionFailed();
		}
		if(!target.isInTransaction())
		{
			new Transaction(Transaction.TransactionType.PARTY, activeChar, target, 10000L);
			target.sendPacket(new AskJoinParty(activeChar.getName(), itemDistribution));
			activeChar.sendPacket(new SystemMessage(105).addString(target.getName()));
			if(Config.BOTS_CAN_JOIN_PARTY && target.isFashion && Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY))
			{
				final int ans = Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY) ? 1 : Rnd.chance(Config.BOTS_CHANCE_REFUSE_PARTY) ? 0 : 2;
				if(ans < 2)
				{
					final int id = target.getObjectId();
					ThreadPoolManager.getInstance().schedule(() ->
					{
						final Player bot = GameObjectsStorage.getPlayer(id);
						if(bot != null)
							RequestAnswerJoinParty.answer(bot, ans);
					}, Rnd.get(2500, 8500));
				}
			}
		}
		else
			activeChar.sendPacket(new SystemMessage(153).addString(target.getName()));
	}

	private void createNewParty(final int itemDistribution, final Player target, final Player requestor)
	{
		if(!target.isInTransaction())
		{
			requestor.setParty(new Party(requestor, itemDistribution));
			new Transaction(Transaction.TransactionType.PARTY, requestor, target, 10000L);
			target.sendPacket(new AskJoinParty(requestor.getName(), itemDistribution));
			requestor.sendPacket(new SystemMessage(105).addString(target.getName()));
			if(Config.BOTS_CAN_JOIN_PARTY && target.isFashion && Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY))
			{
				final int ans = Rnd.chance(Config.BOTS_CHANCE_JOIN_PARTY) ? 1 : Rnd.chance(Config.BOTS_CHANCE_REFUSE_PARTY) ? 0 : 2;
				if(ans < 2)
				{
					final int id = target.getObjectId();
					ThreadPoolManager.getInstance().schedule(() ->
					{
						final Player bot = GameObjectsStorage.getPlayer(id);
						if(bot != null)
							RequestAnswerJoinParty.answer(bot, ans);
					}, Rnd.get(2500, 8500));
				}
			}
		}
		else
			requestor.sendPacket(new SystemMessage(153).addString(target.getName()));
	}
}
