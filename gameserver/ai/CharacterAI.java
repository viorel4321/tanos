package l2s.gameserver.ai;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.network.l2.s2c.Die;
import l2s.gameserver.utils.Location;

public class CharacterAI extends AbstractAI
{
	public CharacterAI(final Creature actor)
	{
		super(actor);
	}

	@Override
	protected void onIntentionIdle()
	{
		this.clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionActive()
	{
		this.clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(final Creature target)
	{
		setAttackTarget(target);
		this.clientStopMoving();
		changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
		onEvtThink();
	}

	@Override
	protected void onIntentionCast(final Skill skill, final Creature target)
	{
		setAttackTarget(target);
		changeIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		onEvtThink();
	}

	@Override
	protected void onIntentionFollow(final Creature target, final Integer offset)
	{
		changeIntention(CtrlIntention.AI_INTENTION_FOLLOW, target, offset);
		onEvtThink();
	}

	@Override
	protected void onIntentionInteract(final GameObject object)
	{}

	@Override
	protected void onIntentionPickUp(final GameObject item)
	{}

	@Override
	protected void onIntentionRest()
	{}

	@Override
	protected void onEvtArrivedBlocked(final Location blocked_at_pos)
	{
		final Creature actor = getActor();
		if(actor.isPlayer())
		{
			final Location loc = ((Player) actor).getLastServerPosition();
			if(loc != null)
				actor.setLoc(loc, true);
			actor.stopMove();
		}
		onEvtThink();
	}

	@Override
	protected void onEvtForgetObject(final GameObject object)
	{
		if(object == null)
			return;
		final Creature actor = getActor();
		if(actor.isAttackingNow() && getAttackTarget() == object)
			actor.abortAttack(true, true);
		if(actor.isCastingNow() && getAttackTarget() == object)
			actor.abortCast(true, false);
		if(getAttackTarget() == object)
			setAttackTarget(null);
		if(actor.getTargetId() == object.getObjectId())
			actor.setTarget(null);
		if(actor.getFollowTarget() == object)
			actor.setFollowTarget(null);
		if(actor.getServitor() != null)
			actor.getServitor().getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, object);
	}

	@Override
	protected void onEvtDead(final Creature killer)
	{
		final Creature actor = getActor();
		actor.abortAttack(true, false);
		actor.abortCast(true, false);
		actor.stopMove();
		actor.broadcastPacket(new Die(actor));
		this.setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtFakeDeath()
	{
		this.clientStopMoving();
		this.setIntention(CtrlIntention.AI_INTENTION_IDLE);
	}

	@Override
	protected void onEvtAttacked(final Creature attacker, final Skill skill, final int damage)
	{}

	@Override
	protected void onEvtClanAttacked(final Creature attacked_member, final Creature attacker, final int damage)
	{}

	public void Attack(final GameObject target, final boolean forceUse, final boolean dontMove)
	{
		this.setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	public void Cast(final Skill skill, final Creature target)
	{
		this.Cast(skill, target, false, false);
	}

	public void Cast(final Skill skill, final Creature target, final boolean forceUse, final boolean dontMove)
	{
		this.setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
	}

	@Override
	protected void onEvtThink()
	{}

	@Override
	protected void onEvtAggression(final Creature target, final int aggro)
	{}

	@Override
	protected void onEvtFinishCasting(final Skill skill)
	{}

	@Override
	protected void onEvtReadyToAct()
	{}

	@Override
	protected void onEvtArrived()
	{}

	@Override
	protected void onEvtArrivedTarget()
	{}

	@Override
	protected void onEvtSeeSpell(final Skill skill, final Creature caster)
	{
		final Creature actor = getActor();
		if(actor != null)
			actor.onSeeSpell(skill, caster);
	}

	@Override
	protected void onEvtSpawn()
	{}

	@Override
	protected void onEvtDeSpawn()
	{}

	@Override
	protected void onEvtTeleported()
	{}

	public void stopAITask()
	{}

	public void startAITask()
	{}

	public void setNextAction(final PlayableAI.nextAction action, final Object arg0, final Object arg1, final boolean arg2, final boolean arg3)
	{}

	public void clearNextAction()
	{}

	public int getAttackTargetObjectId()
	{
		Creature target = getAttackTarget();
		return target != null ? target.getObjectId() : 0;
	}

	public void setGlobalAggro(final long value)
	{}

	public boolean isNulled()
	{
		return true;
	}

	public boolean isActive()
	{
		return true;
	}
}
