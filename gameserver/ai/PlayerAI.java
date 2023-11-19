package l2s.gameserver.ai;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;

public class PlayerAI extends PlayableAI
{
	public PlayerAI(final Player actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionRest()
	{
		changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
		setAttackTarget(null);
		this.clientStopMoving();
	}

	@Override
	protected void onIntentionActive()
	{
		clearNextAction();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
	}

	@Override
	public void onIntentionInteract(final GameObject object)
	{
		final Player actor = getActor();
		if(actor.getSittingTask())
		{
			setNextAction(nextAction.INTERACT, object, null, false, false);
			return;
		}
		if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionInteract(object);
	}

	@Override
	public void onIntentionPickUp(final GameObject object)
	{
		final Player actor = getActor();
		if(actor.getSittingTask())
		{
			setNextAction(nextAction.PICKUP, object, null, false, false);
			return;
		}
		if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.onIntentionPickUp(object);
	}

	@Override
	protected void thinkAttack(final boolean checkRange)
	{
		final Player actor = getActor();
		if(actor.isBlocked())
		{
			this.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_IN_A_FROZEN_STATE_PLEASE_WAIT_A_MOMENT, Msg.ActionFail);
			return;
		}
		super.thinkAttack(checkRange);
	}

	@Override
	protected void thinkCast(final boolean checkRange)
	{
		final Player actor = getActor();
		if(actor.isBlocked())
		{
			this.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_IN_A_FROZEN_STATE_PLEASE_WAIT_A_MOMENT, Msg.ActionFail);
			return;
		}
		super.thinkCast(checkRange);
	}

	@Override
	public void Attack(final GameObject target, final boolean forceUse, final boolean dontMove)
	{
		final Player actor = getActor();
		if(System.currentTimeMillis() - actor.getLastAttackPacket() < Config.ATTACK_PACKET_DELAY)
		{
			actor.sendActionFailed();
			return;
		}
		actor.setLastAttackPacket();
		actor.setActive();
		if(actor.getSittingTask())
		{
			setNextAction(nextAction.ATTACK, target, null, forceUse, false);
			return;
		}
		if(actor.isSitting())
		{
			actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
			clientActionFailed();
			return;
		}
		super.Attack(target, forceUse, dontMove);
	}

	@Override
	public void Cast(final Skill skill, final Creature target, final boolean forceUse, final boolean dontMove)
	{
		final Player actor = getActor();
		if(!skill.altUse() && (!Config.ALT_TOGGLE || !skill.isToggle()) && (skill.getSkillType() != Skill.SkillType.CRAFT || !Config.ALLOW_TALK_WHILE_SITTING))
		{
			if(actor.getSittingTask())
			{
				setNextAction(nextAction.CAST, skill, target, forceUse, dontMove);
				clientActionFailed();
				return;
			}
			if(skill.getSkillType() == Skill.SkillType.SUMMON && actor.getPrivateStoreType() != 0)
			{
				actor.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
				clientActionFailed();
				return;
			}
			if(actor.isSitting())
			{
				actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
				clientActionFailed();
				return;
			}
		}
		super.Cast(skill, target, forceUse, dontMove);
	}

	@Override
	protected void onEvtAttacked(final Creature attacker, final Skill skill, final int damage)
	{
		final Player actor = getActor();
		if(actor == null)
			return;
		if(actor.getTrainedBeast() != null)
			actor.getTrainedBeast().onOwnerGotAttacked(attacker);
		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	public Player getActor()
	{
		return (Player) super.getActor();
	}
}
