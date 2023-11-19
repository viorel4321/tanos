package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.network.l2.s2c.SendTradeRequest;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class TradeRequest extends L2GameClientPacket
{
	private int _objectId;

	@Override
	public void readImpl()
	{
		_objectId = readD();
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
		if(!activeChar.getPlayerAccess().UseTrade)
		{
			activeChar.sendMessage("You can't use trade.");
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getPvpFlag() > 0 || activeChar.isInCombat())
		{
			activeChar.sendMessage("You can't trade in combat or PvP flag.");
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isDead())
		{
			activeChar.sendActionFailed();
			return;
		}
		final GameObject target = World.getAroundObjectById(activeChar, _objectId);
		if(target == null || !target.isPlayer() || target.getObjectId() == activeChar.getObjectId())
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}
		final Player pcTarget = (Player) target;
		if(activeChar.getPrivateStoreType() != 0 || pcTarget.getPrivateStoreType() != 0)
		{
			activeChar.sendPacket(Msg.WHILE_OPERATING_A_PRIVATE_STORE_OR_WORKSHOP_YOU_CANNOT_DISCARD_DESTROY_OR_TRADE_AN_ITEM);
			return;
		}
		if(!pcTarget.getPlayerAccess().UseTrade)
		{
			activeChar.sendMessage("Your target can't use trade.");
			activeChar.sendActionFailed();
			return;
		}
		if(pcTarget.getPvpFlag() > 0 || pcTarget.isInCombat())
		{
			activeChar.sendMessage("Your target can't trade in combat or PvP flag.");
			activeChar.sendActionFailed();
			return;
		}
		if(pcTarget.getTeam() != 0)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(pcTarget.isInOlympiadMode() || activeChar.isInOlympiadMode())
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}
		if(pcTarget.getTradeRefusal() || pcTarget.isInBlockList(activeChar) || pcTarget.isBlockAll())
		{
			activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED);
			return;
		}
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.ALREADY_TRADING);
			return;
		}
		if(pcTarget.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(153).addString(pcTarget.getName()));
			return;
		}
		if(activeChar.isFishing())
		{
			activeChar.sendPacket(Msg.YOU_CANNOT_DO_THAT_WHILE_FISHING);
			return;
		}
		new Transaction(Transaction.TransactionType.TRADE_REQUEST, activeChar, pcTarget, 10000L);
		pcTarget.sendPacket(new SendTradeRequest(activeChar.getObjectId()));
		activeChar.sendPacket(new SystemMessage(118).addString(pcTarget.getName()));
	}
}
