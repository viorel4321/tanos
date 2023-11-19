package l2s.gameserver.network.l2.c2s;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Transaction;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.AskJoinPledge;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Util;

public class RequestJoinPledge extends L2GameClientPacket
{
	private int _target;
	private int _pledgeType;

	@Override
	public void readImpl()
	{
		_target = readD();
		_pledgeType = readD();
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
		final Clan clan = activeChar.getClan();
		if(clan == null || !clan.canInvite())
		{
			activeChar.sendPacket(new SystemMessage(231));
			return;
		}
		if(activeChar.isInTransaction())
		{
			activeChar.sendPacket(Msg.WAITING_FOR_ANOTHER_REPLY);
			return;
		}
		if(_target == activeChar.getObjectId())
		{
			activeChar.sendPacket(new SystemMessage(4));
			return;
		}
		if((activeChar.getClanPrivileges() & 0x2) != 0x2)
		{
			activeChar.sendPacket(new SystemMessage(154));
			return;
		}
		final GameObject object = activeChar.getVisibleObject(_target);
		if(object == null || !object.isPlayer())
			return;
		final Player member = (Player) object;
		if(!activeChar.getPlayerAccess().CanJoinClan)
		{
			activeChar.sendPacket(new SystemMessage(760).addString(member.getName()));
			member.sendPacket(new SystemMessage(308));
			return;
		}
		if(!member.getPlayerAccess().CanJoinClan)
		{
			activeChar.sendPacket(new SystemMessage(760).addString(member.getName()));
			member.sendPacket(new SystemMessage(308));
			return;
		}
		if(member.getClanId() != 0)
		{
			activeChar.sendPacket(new SystemMessage(10).addString(member.getName()));
			return;
		}
		if(member.isInTransaction())
		{
			activeChar.sendPacket(new SystemMessage(153).addString(member.getName()));
			return;
		}
		if(_pledgeType == -1 && (member.getLevel() > 40 || member.getClassId().getLevel() > 2))
		{
			activeChar.sendPacket(new SystemMessage(1734));
			return;
		}
		if(clan.getSubPledgeMembersCount(_pledgeType) >= clan.getSubPledgeLimit(_pledgeType))
		{
			if(_pledgeType == 0)
				activeChar.sendPacket(new SystemMessage(1835).addString(clan.getName()));
			else
				activeChar.sendPacket(new SystemMessage(233));
			return;
		}
		if(clan.block_invite > System.currentTimeMillis())
		{
			final String cn = String.valueOf(Math.max((clan.block_invite - System.currentTimeMillis()) / 1000L, 1L));
			activeChar.sendMessage(activeChar.isLangRus() ? "\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u043f\u0440\u0438\u0433\u043b\u0430\u0448\u0430\u0442\u044c \u043f\u043e\u043a\u0430 \u043f\u0440\u0438\u0433\u043b\u0430\u0448\u0435\u043d\u043d\u044b\u0439 \u0438\u0433\u0440\u043e\u043a \u043d\u0435 \u043e\u0442\u0432\u0435\u0442\u0438\u0442, \u043b\u0438\u0431\u043e \u043f\u043e\u0434\u043e\u0436\u0434\u0438\u0442\u0435 " + cn + " " + Util.secondFormat(true, cn) + "." : "You can't invite until invited player does not respond, or wait " + cn + " " + Util.secondFormat(false, cn) + ".");
			return;
		}
		clan.block_invite = System.currentTimeMillis() + 15000L;
		new Transaction(Transaction.TransactionType.CLAN, activeChar, member, 10000L);
		member.setPledgeType(_pledgeType);
		member.sendPacket(new AskJoinPledge(activeChar.getObjectId(), activeChar.getClan().getName()));
		if(Config.BOTS_CAN_JOIN_CLAN && member.isFashion && Rnd.chance(Config.BOTS_CHANCE_JOIN_CLAN))
		{
			final int ans = Rnd.chance(Config.BOTS_CHANCE_JOIN_CLAN) ? 1 : Rnd.chance(Config.BOTS_CHANCE_REFUSE_CLAN) ? 0 : 2;
			if(ans < 2)
			{
				final int id = member.getObjectId();
				ThreadPoolManager.getInstance().schedule(new Runnable(){
					@Override
					public void run()
					{
						final Player bot = GameObjectsStorage.getPlayer(id);
						if(bot != null)
							RequestAnswerJoinPledge.answer(bot, ans);
					}
				}, Rnd.get(2500, 8500));
			}
		}
	}
}
