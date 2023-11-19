package l2s.gameserver.ai;

import java.util.concurrent.ScheduledFuture;

import l2s.commons.util.Rnd;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public class SummonAI extends PlayableAI
{
	private CtrlIntention _storedIntention;
	private Object _storedIntentionArg0;
	private Object _storedIntentionArg1;
	private boolean _storedForceUse;
	private RunOnAttacked _runOnAttacked;
	private ScheduledFuture<?> _runOnAttackedTask;

	public SummonAI(final Servitor actor)
	{
		super(actor);
		_storedIntention = null;
		_storedIntentionArg0 = null;
		_storedIntentionArg1 = null;
		_storedForceUse = false;
	}

	public void storeIntention()
	{
		if(_storedIntention == null)
		{
			_storedIntention = getIntention();
			_storedIntentionArg0 = _intention_arg0;
			_storedIntentionArg1 = _intention_arg1;
			_storedForceUse = _forceUse;
		}
	}

	public boolean restoreIntention()
	{
		final CtrlIntention intention = _storedIntention;
		final Object arg0 = _storedIntentionArg0;
		final Object arg2 = _storedIntentionArg1;
		if(intention != null)
		{
			_forceUse = _storedForceUse;
			this.setIntention(intention, arg0, arg2);
			clearStoredIntention();
			onEvtThink();
			return true;
		}
		return false;
	}

	public void clearStoredIntention()
	{
		_storedIntention = null;
		_storedIntentionArg0 = null;
		_storedIntentionArg1 = null;
	}

	@Override
	protected void onIntentionIdle()
	{
		clearStoredIntention();
		super.onIntentionIdle();
	}

	@Override
	protected void onEvtFinishCasting(final Skill skill)
	{
		if(!restoreIntention())
			super.onEvtFinishCasting(skill);
	}

	@Override
	protected void thinkActive()
	{
		final Servitor actor = getActor();
		clearNextAction();
		if(actor.isDepressed())
		{
			setAttackTarget(actor.getPlayer());
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, actor.getPlayer(), null);
			thinkAttack(true);
		}
		else if(actor.isFollow() && !actor.isAfraid())
		{
			changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, actor.getPlayer(), 100);
			thinkFollow();
		}
		super.thinkActive();
	}

	@Override
	protected void thinkAttack(final boolean checkRange)
	{
		final Servitor actor = getActor();
		if(actor.isDepressed())
			setAttackTarget(actor.getPlayer());
		super.thinkAttack(checkRange);
	}

	@Override
	protected void onEvtAttacked(final Creature attacker, final Skill skill, final int damage)
	{
		final Servitor actor = getActor();
		if(attacker != null && actor.getPlayer().isDead() && !actor.isDepressed())
			Attack(attacker, false, false);
		else
		{
			if(_runOnAttacked != null)
				_runOnAttacked.setAttacker(attacker);
			if(_runOnAttacked == null && (getIntention() == CtrlIntention.AI_INTENTION_FOLLOW || getIntention() == CtrlIntention.AI_INTENTION_IDLE || getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) && !_actor.isMoving && attacker != _actor.getPlayer())
			{
				if(_runOnAttacked == null)
					(_runOnAttacked = new RunOnAttacked()).setAttacker(attacker);
				if(_runOnAttackedTask == null)
					_runOnAttackedTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(_runOnAttacked, 0L, 500L);
			}
		}
		super.onEvtAttacked(attacker, skill, damage);
	}

	@Override
	public void Cast(final Skill skill, final Creature target, final boolean forceUse, final boolean dontMove)
	{
		storeIntention();
		super.Cast(skill, target, forceUse, dontMove);
	}

	@Override
	public Servitor getActor()
	{
		return (Servitor) super.getActor();
	}

	private class RunOnAttacked implements Runnable
	{
		private Creature _attacker;
		private long _lastAttack;

		@Override
		public void run()
		{
			if(_attacker != null && SummonAI.this._actor.getPlayer() != null && _lastAttack + 20000L > System.currentTimeMillis() && (getIntention() == CtrlIntention.AI_INTENTION_FOLLOW || getIntention() == CtrlIntention.AI_INTENTION_IDLE || getIntention() == CtrlIntention.AI_INTENTION_ACTIVE))
			{
				if(!SummonAI.this._actor.isMoving && SummonAI.this._actor.isInRange(_attacker, 110L))
				{
					Location src;
					if(((Servitor) SummonAI.this._actor).getLastFollowPosition() != null)
						src = ((Servitor) SummonAI.this._actor).getLastFollowPosition();
					else
						src = SummonAI.this._actor.getPlayer().getLoc();
					Location dst = Util.getPointInRadius(src, Rnd.get(80, 160), (int) Util.calculateAngleFrom(_attacker.getX(), _attacker.getY(), SummonAI.this._actor.getPlayer().getX(), SummonAI.this._actor.getPlayer().getY()) + Rnd.get(115, 155));
					final Location loc = Util.getPointInRadius(src, Rnd.get(80, 160), (int) Util.calculateAngleFrom(_attacker.getX(), _attacker.getY(), SummonAI.this._actor.getPlayer().getX(), SummonAI.this._actor.getPlayer().getY()) + Rnd.get(205, 245));
					if(_attacker.getDistance(loc.getX(), loc.getY()) > _attacker.getDistance(dst.getX(), dst.getY()))
						dst = loc;
					SummonAI.this._actor.moveToLocation(dst, 0, false);
				}
			}
			else
			{
				_attacker = null;
				if(_runOnAttackedTask != null)
					_runOnAttackedTask.cancel(true);
				_runOnAttackedTask = null;
				_runOnAttacked = null;
			}
		}

		public void setAttacker(final Creature attacker)
		{
			_attacker = attacker;
			_lastAttack = System.currentTimeMillis();
		}
	}
}
