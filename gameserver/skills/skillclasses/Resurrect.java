package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.listener.actor.player.impl.ReviveAnswerListener;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.instances.PetInstance;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class Resurrect extends Skill
{
	private final boolean _canPet;

	public Resurrect(final StatsSet set)
	{
		super(set);
		_canPet = set.getBool("canPet", false);
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(!activeChar.isPlayer())
			return false;
		if(target == null || target != activeChar && !target.isDead())
		{
			activeChar.sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		final Player player = (Player) activeChar;
		final Player pcTarget = target.getPlayer();
		if(pcTarget == null || pcTarget.inEvent())
		{
			player.sendPacket(Msg.TARGET_IS_INCORRECT);
			return false;
		}
		if(player.inEvent())
		{
			player.sendMessage(player.isLangRus() ? "\u0412\u043e\u0441\u043a\u0440\u0435\u0448\u0435\u043d\u0438\u0435 \u043d\u0435\u0434\u043e\u0441\u0442\u0443\u043f\u043d\u043e \u0432 \u0442\u0435\u043a\u0443\u0449\u0435\u043c \u044d\u0432\u0435\u043d\u0442\u0435." : "Resurrection impossible in current event.");
			return false;
		}
		if(player.isOnSiegeField() && !player.isInSiege() || pcTarget.isOnSiegeField() && !player.isInSiege())
		{
			player.sendPacket(new SystemMessage(113).addSkillName(_id, getDisplayLevel()));
			return false;
		}
		for(final GlobalEvent e : player.getEvents())
			if(!e.canResurrect(player, target, forceUse))
			{
				player.sendPacket(new SystemMessage(113).addSkillName(_id, getDisplayLevel()));
				return false;
			}
		if(oneTarget())
			if(target.isPet())
			{
				final Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
				final ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
						activeChar.sendPacket(new SystemMessage(1513));
					else
						activeChar.sendPacket(new SystemMessage(1515));
					return false;
				}
				if(!_canPet && _targetType != SkillTargetType.TARGET_PET)
				{
					player.sendPacket(Msg.TARGET_IS_INCORRECT);
					return false;
				}
			}
			else if(target.isPlayer())
			{
				final Pair<Integer, OnAnswerListener> ask = pcTarget.getAskListener(false);
				final ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null)
				{
					if(reviveAsk.isForPet())
						activeChar.sendPacket(new SystemMessage(1511));
					else
						activeChar.sendPacket(new SystemMessage(1513));
					return false;
				}
				if(_targetType == SkillTargetType.TARGET_PET)
				{
					player.sendPacket(Msg.TARGET_IS_INCORRECT);
					return false;
				}
				if(pcTarget.isFestivalParticipant())
				{
					player.sendMessage(new CustomMessage("l2s.gameserver.skills.skillclasses.Resurrect"));
					return false;
				}
			}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		double percent = _power;
		if(percent < 100.0 && !isHandler())
		{
			final double wit_bonus = _power * (Formulas.WITbonus[activeChar.getWIT()] - 1.0);
			percent += wit_bonus > 20.0 ? 20.0 : wit_bonus;
			if(percent > 90.0)
				percent = 90.0;
		}

		loop: for(final Creature target : targets)
		{
			if(target == null)
				continue;
			if(target.getPlayer() == null)
				continue;
			if(!GeoEngine.canSeeTarget(activeChar, target))
				continue;
			for(final GlobalEvent e : target.getEvents())
				if(!e.canResurrect((Player) activeChar, target, true))
					continue loop;
			if(target.isPet())
			{
				if(!_canPet)
					continue;
				if(target.getPlayer() == activeChar)
					((PetInstance) target).doRevive(percent);
				else
					target.getPlayer().reviveRequest((Player) activeChar, percent, true, false);
			}
			else
			{
				if(!target.isPlayer())
					continue;
				if(_targetType == SkillTargetType.TARGET_PET)
					continue;
				final Player targetPlayer = (Player) target;
				final Pair<Integer, OnAnswerListener> ask = targetPlayer.getAskListener(false);
				final ReviveAnswerListener reviveAsk = ask != null && ask.getValue() instanceof ReviveAnswerListener ? (ReviveAnswerListener) ask.getValue() : null;
				if(reviveAsk != null)
					continue;
				if(targetPlayer.isFestivalParticipant())
					continue;
				targetPlayer.reviveRequest((Player) activeChar, percent, false, false);
			}
			getEffects(activeChar, target, getActivateRate() > 0, false);
		}

		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}
}
