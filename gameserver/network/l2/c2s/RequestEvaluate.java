package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.UserInfo;

public class RequestEvaluate extends L2GameClientPacket
{
	private int _targetid;

	@Override
	public void readImpl()
	{
		_targetid = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(!activeChar.getPlayerAccess().CanEvaluate)
			return;
		if(activeChar.getTarget() == null)
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}
		final Player target = activeChar.getTarget().getPlayer();
		if(target == null)
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return;
		}
		if(activeChar.getLevel() < 10)
		{
			final SystemMessage sm = Msg.ONLY_LEVEL_SUP_10_CAN_RECOMMEND;
			activeChar.sendPacket(sm);
			return;
		}
		if(target == activeChar)
		{
			final SystemMessage sm = Msg.YOU_CANNOT_RECOMMEND_YOURSELF;
			activeChar.sendPacket(sm);
			return;
		}
		if(activeChar.getRecomLeft() <= 0)
		{
			final SystemMessage sm = Msg.NO_MORE_RECOMMENDATIONS_TO_HAVE;
			activeChar.sendPacket(sm);
			return;
		}
		if(target.getRecomHave() >= 255)
		{
			final SystemMessage sm = Msg.YOU_NO_LONGER_RECIVE_A_RECOMMENDATION;
			activeChar.sendPacket(sm);
			return;
		}
		if(!activeChar.canRecom(target))
		{
			final SystemMessage sm = Msg.THAT_CHARACTER_IS_RECOMMENDED;
			activeChar.sendPacket(sm);
			return;
		}
		activeChar.giveRecom(target);
		SystemMessage sm = new SystemMessage(830);
		sm.addString(target.getName());
		sm.addNumber(Integer.valueOf(activeChar.getRecomLeft()));
		activeChar.sendPacket(sm);
		sm = new SystemMessage(831);
		sm.addString(activeChar.getName());
		target.sendPacket(sm);
		activeChar.sendPacket(new UserInfo(activeChar));
		target.broadcastUserInfo(true);
	}
}
