package l2s.gameserver.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.Location;

public abstract class AbstractAI implements Runnable
{
	protected static final Logger _log;
	protected final Creature _actor;
	private HardReference<? extends Creature> _attackTarget;
	private CtrlIntention _intention;

	protected AbstractAI(final Creature actor)
	{
		_attackTarget = HardReferences.emptyRef();
		_intention = CtrlIntention.AI_INTENTION_IDLE;
		_actor = actor;
	}

	public void changeIntention(final CtrlIntention intention, final Object arg0, final Object arg1)
	{
		_intention = intention;
		if(intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK)
			setAttackTarget(null);
	}

	public final void setIntention(final CtrlIntention intention)
	{
		this.setIntention(intention, null, null);
	}

	public final void setIntention(final CtrlIntention intention, final Object arg0)
	{
		this.setIntention(intention, arg0, null);
	}

	public void setIntention(CtrlIntention intention, final Object arg0, final Object arg1)
	{
		if(intention != CtrlIntention.AI_INTENTION_CAST && intention != CtrlIntention.AI_INTENTION_ATTACK)
			setAttackTarget(null);
		final Creature actor = getActor();
		if(!actor.isVisible())
		{
			if(_intention == CtrlIntention.AI_INTENTION_IDLE)
				return;
			intention = CtrlIntention.AI_INTENTION_IDLE;
		}
		actor.getListeners().onAiIntention(intention, arg0, arg1);
		switch(intention)
		{
			case AI_INTENTION_IDLE:
			{
				onIntentionIdle();
				break;
			}
			case AI_INTENTION_ACTIVE:
			{
				onIntentionActive();
				break;
			}
			case AI_INTENTION_REST:
			{
				onIntentionRest();
				break;
			}
			case AI_INTENTION_ATTACK:
			{
				onIntentionAttack((Creature) arg0);
				break;
			}
			case AI_INTENTION_CAST:
			{
				onIntentionCast((Skill) arg0, (Creature) arg1);
				break;
			}
			case AI_INTENTION_PICK_UP:
			{
				onIntentionPickUp((GameObject) arg0);
				break;
			}
			case AI_INTENTION_INTERACT:
			{
				onIntentionInteract((GameObject) arg0);
				break;
			}
			case AI_INTENTION_FOLLOW:
			{
				onIntentionFollow((Creature) arg0, (Integer) arg1);
				break;
			}
		}
	}

	public final void notifyEvent(final CtrlEvent evt)
	{
		this.notifyEvent(evt, new Object[0]);
	}

	public final void notifyEvent(final CtrlEvent evt, final Object arg0)
	{
		this.notifyEvent(evt, new Object[] { arg0 });
	}

	public final void notifyEvent(final CtrlEvent evt, final Object arg0, final Object arg1)
	{
		this.notifyEvent(evt, new Object[] { arg0, arg1 });
	}

	public final void notifyEvent(final CtrlEvent evt, final Object arg0, final Object arg1, final Object arg2)
	{
		this.notifyEvent(evt, new Object[] { arg0, arg1, arg2 });
	}

	protected void notifyEvent(final CtrlEvent evt, final Object[] args)
	{
		final Creature actor = getActor();
		actor.getListeners().onAiEvent(evt, args);
		switch(evt)
		{
			case EVT_THINK:
			{
				onEvtThink();
				break;
			}
			case EVT_ATTACKED:
			{
				onEvtAttacked((Creature) args[0], (Skill) args[1], ((Number) args[2]).intValue());
				break;
			}
			case EVT_CLAN_ATTACKED:
			{
				onEvtClanAttacked((Creature) args[0], (Creature) args[1], ((Number) args[2]).intValue());
				break;
			}
			case EVT_AGGRESSION:
			{
				onEvtAggression((Creature) args[0], ((Number) args[1]).intValue());
				break;
			}
			case EVT_READY_TO_ACT:
			{
				onEvtReadyToAct();
				break;
			}
			case EVT_ARRIVED:
			{
				onEvtArrived();
				break;
			}
			case EVT_ARRIVED_TARGET:
			{
				onEvtArrivedTarget();
				break;
			}
			case EVT_ARRIVED_BLOCKED:
			{
				onEvtArrivedBlocked((Location) args[0]);
				break;
			}
			case EVT_FORGET_OBJECT:
			{
				onEvtForgetObject((GameObject) args[0]);
				break;
			}
			case EVT_DEAD:
			{
				onEvtDead((Creature) args[0]);
				break;
			}
			case EVT_FAKE_DEATH:
			{
				onEvtFakeDeath();
				break;
			}
			case EVT_FINISH_CASTING:
			{
				onEvtFinishCasting((Skill) args[0]);
				break;
			}
			case EVT_SEE_SPELL:
			{
				onEvtSeeSpell((Skill) args[0], (Creature) args[1]);
				break;
			}
			case EVT_SPAWN:
			{
				onEvtSpawn();
				break;
			}
			case EVT_DESPAWN:
			{
				onEvtDeSpawn();
				break;
			}
			case EVT_TELEPORTED:
			{
				onEvtTeleported();
				break;
			}
		}
	}

	protected void clientActionFailed()
	{
		final Creature actor = getActor();
		if(actor != null && actor.isPlayer())
			actor.sendActionFailed();
	}

	public void clientStopMoving(final boolean validate)
	{
		final Creature actor = getActor();
		actor.stopMove(validate);
	}

	public void clientStopMoving()
	{
		final Creature actor = getActor();
		actor.stopMove();
	}

	public Creature getActor()
	{
		return _actor;
	}

	public CtrlIntention getIntention()
	{
		return _intention;
	}

	public void setAttackTarget(final Creature target)
	{
		_attackTarget = (target == null ? HardReferences.emptyRef() : target.getRef());
	}

	public Creature getAttackTarget()
	{
		return _attackTarget.get();
	}

	public boolean isGlobalAI()
	{
		return false;
	}

	@Override
	public void run()
	{}

	@Override
	public String toString()
	{
		return this.getClass().getSimpleName() + " for " + getActor();
	}

	protected abstract void onIntentionIdle();

	protected abstract void onIntentionActive();

	protected abstract void onIntentionRest();

	protected abstract void onIntentionAttack(final Creature p0);

	protected abstract void onIntentionCast(final Skill p0, final Creature p1);

	protected abstract void onIntentionPickUp(final GameObject p0);

	protected abstract void onIntentionInteract(final GameObject p0);

	protected abstract void onEvtThink();

	protected abstract void onEvtAttacked(final Creature p0, final Skill p1, final int p2);

	protected abstract void onEvtClanAttacked(final Creature p0, final Creature p1, final int p2);

	protected abstract void onEvtAggression(final Creature p0, final int p1);

	protected abstract void onEvtReadyToAct();

	protected abstract void onEvtArrived();

	protected abstract void onEvtArrivedTarget();

	protected abstract void onEvtArrivedBlocked(final Location p0);

	protected abstract void onEvtForgetObject(final GameObject p0);

	protected abstract void onEvtDead(final Creature p0);

	protected abstract void onEvtFakeDeath();

	protected abstract void onEvtFinishCasting(final Skill p0);

	protected abstract void onEvtSeeSpell(final Skill p0, final Creature p1);

	protected abstract void onEvtSpawn();

	protected abstract void onEvtDeSpawn();

	protected abstract void onEvtTeleported();

	protected abstract void onIntentionFollow(final Creature p0, final Integer p1);

	static
	{
		_log = LoggerFactory.getLogger(AbstractAI.class);
	}
}
