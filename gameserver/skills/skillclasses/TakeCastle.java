package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.impl.CastleSiegeEvent;
import l2s.gameserver.templates.StatsSet;

public class TakeCastle extends Skill
{
	public TakeCastle(final StatsSet set)
	{
		super(set);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger))
			return false;
		if(activeChar == null || !activeChar.isPlayer())
			return false;
		final Player player = (Player) activeChar;
		if(player.getClan() == null || !player.isClanLeader())
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0435 \u043b\u0438\u0434\u0435\u0440 \u043a\u043b\u0430\u043d\u0430." : "You are not leader of clan.");
			return false;
		}
		final CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
		if(siegeEvent == null)
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0435 \u0443\u0447\u0430\u0441\u0442\u043d\u0438\u043a \u043e\u0441\u0430\u0434\u044b." : "You are not siege participant.");
			return false;
		}
		if(siegeEvent.getSiegeClan("attackers", player.getClan()) == null)
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0435 \u043d\u0430 \u0441\u0442\u043e\u0440\u043e\u043d\u0435 \u0430\u0442\u0430\u043a\u0443\u044e\u0449\u0438\u0445." : "You are not attacker.");
			return false;
		}
		if(!siegeEvent.getResidence().getArtefacts().contains(target.getNpcId()))
		{
			player.sendMessage(player.isLangRus() ? "\u041d\u0435\u0432\u0435\u0440\u043d\u044b\u0439 \u0410\u0440\u0442\u0435\u0444\u0430\u043a\u0442." : "Incorrect Artefact.");
			return false;
		}
		if(!siegeEvent.isActiveArtefact(player.getClan().getClanId(), target.getNpcId()))
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u0443\u0436\u0435 \u043d\u0430\u043b\u043e\u0436\u0438\u043b\u0438 \u043f\u0435\u0447\u0430\u0442\u044c \u043d\u0430 \u044d\u0442\u043e\u0442 \u0430\u043b\u0442\u0430\u0440\u044c. \u041d\u0430\u043b\u043e\u0436\u0438\u0442\u0435 \u043f\u0435\u0447\u0430\u0442\u044c \u043d\u0430 \u0432\u0442\u043e\u0440\u043e\u0439." : "You have left a stamp on this altar. Place the seal on the second.");
			return false;
		}
		if(player.isMounted())
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u0434\u0435\u043b\u0430\u0442\u044c \u044d\u0442\u043e \u0432\u0435\u0440\u0445\u043e\u043c." : "You can't do it in ride state.");
			return false;
		}
		if(!player.isInRangeZ(target, 185L))
		{
			player.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
			return false;
		}
		if(first)
			siegeEvent.broadcastTo(Msg.THE_OPPONENT_CLAN_HAS_BEGUN_TO_ENGRAVE_THE_RULER, "defenders");
		return true;
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				if(!target.isArtefact())
					continue;
				final Player player = (Player) activeChar;
				final CastleSiegeEvent siegeEvent = player.getEvent(CastleSiegeEvent.class);
				if(siegeEvent == null)
					continue;
				siegeEvent.engrave(player, target.getNpcId());
			}
	}
}
