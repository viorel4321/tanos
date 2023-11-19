package l2s.gameserver.ai;

import java.util.concurrent.ScheduledFuture;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Servitor;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Location;

public class PlayableAI extends CharacterAI {
    private volatile int thinking;
    protected Object _intention_arg0;
    protected Object _intention_arg1;
    protected Skill _skill;
    private nextAction _nextAction;
    private Object _nextAction_arg0;
    private Object _nextAction_arg1;
    private boolean _nextAction_arg2;
    private boolean _nextAction_arg3;
    protected boolean _forceUse;
    private boolean _dontMove;
    private ScheduledFuture<?> _followTask;

    public PlayableAI(final Playable actor) {
        super(actor);
        thinking = 0;
        _intention_arg0 = null;
        _intention_arg1 = null;
    }

    @Override
    public void changeIntention(final CtrlIntention intention, final Object arg0, final Object arg1) {
        super.changeIntention(intention, arg0, arg1);
        _intention_arg0 = arg0;
        _intention_arg1 = arg1;
    }

    @Override
    public void setIntention(final CtrlIntention intention, final Object arg0, final Object arg1) {
        _intention_arg0 = null;
        _intention_arg1 = null;
        super.setIntention(intention, arg0, arg1);
    }

    @Override
    protected void onIntentionCast(final Skill skill, final Creature target) {
        super.onIntentionCast(_skill = skill, target);
    }

    @Override
    public void setNextAction(final nextAction action, final Object arg0, final Object arg1, final boolean arg2, final boolean arg3) {
        _nextAction = action;
        _nextAction_arg0 = arg0;
        _nextAction_arg1 = arg1;
        _nextAction_arg2 = arg2;
        _nextAction_arg3 = arg3;
    }

    public boolean setNextIntention() {
        final nextAction nextAction = _nextAction;
        final Object nextAction_arg0 = _nextAction_arg0;
        final Object nextAction_arg2 = _nextAction_arg1;
        final boolean nextAction_arg3 = _nextAction_arg2;
        final boolean nextAction_arg4 = _nextAction_arg3;
        final Playable actor = getActor();
        if (nextAction == null || actor.isActionsDisabled())
            return false;
        switch (nextAction) {
            case ATTACK: {
                if (!(nextAction_arg0 instanceof Creature))
                    return false;
                final Creature target = (Creature) nextAction_arg0;
                _forceUse = nextAction_arg3;
                _dontMove = nextAction_arg4;
                clearNextAction();
                setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
                break;
            }
            case CAST: {
                if (!(nextAction_arg0 instanceof Skill) || !(nextAction_arg2 instanceof Creature))
                    return false;
                final Skill skill = (Skill) nextAction_arg0;
                final Creature target = (Creature) nextAction_arg2;
                _forceUse = nextAction_arg3;
                _dontMove = nextAction_arg4;
                clearNextAction();
                if (skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
                    setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
                    break;
                }
                if (skill.getNextAction() == Skill.NextAction.ATTACK && target.isAutoAttackable(actor)) {
                    setNextAction(PlayableAI.nextAction.ATTACK, target, null, _forceUse, false);
                    return setNextIntention();
                }
                return false;
            }
            case MOVE: {
                if (!(nextAction_arg0 instanceof Location) || !(nextAction_arg2 instanceof Integer))
                    return false;
                final Location loc = (Location) nextAction_arg0;
                final int offset = (int) nextAction_arg2;
                clearNextAction();
                actor.moveToLocation(loc, offset, nextAction_arg3);
                break;
            }
            case REST: {
                actor.sitDown(0);
                break;
            }
            case INTERACT: {
                if (!(nextAction_arg0 instanceof GameObject))
                    return false;
                final GameObject object = (GameObject) nextAction_arg0;
                clearNextAction();
                onIntentionInteract(object);
                break;
            }
            case PICKUP: {
                if (!(nextAction_arg0 instanceof GameObject))
                    return false;
                final GameObject object = (GameObject) nextAction_arg0;
                clearNextAction();
                onIntentionPickUp(object);
                break;
            }
            default: {
                return false;
            }
        }
        return true;
    }

    @Override
    public void clearNextAction() {
        _nextAction = null;
        _nextAction_arg0 = null;
        _nextAction_arg1 = null;
        _nextAction_arg2 = false;
        _nextAction_arg3 = false;
    }

    @Override
    protected void onEvtFinishCasting(final Skill skill) {
        if (!setNextIntention())
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    @Override
    protected void onEvtReadyToAct() {
        if (Config.OFF_AUTOATTACK && getIntention() == CtrlIntention.AI_INTENTION_ATTACK) {
            final Creature target = getAttackTarget();
            if (target != null && target.isPlayable() && !target.isAutoAttackable(getActor()))
                changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        }
        if (!setNextIntention())
            onEvtThink();
    }

    @Override
    protected void onEvtArrived() {
        if (!setNextIntention())
            if (getIntention() == CtrlIntention.AI_INTENTION_INTERACT || getIntention() == CtrlIntention.AI_INTENTION_PICK_UP)
                onEvtThink();
            else
                changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
    }

    @Override
    protected void onEvtArrivedTarget() {
        switch (getIntention()) {
            case AI_INTENTION_ATTACK: {
                thinkAttack(true);
                break;
            }
            case AI_INTENTION_CAST: {
                thinkCast(true);
                break;
            }
            case AI_INTENTION_FOLLOW: {
                thinkFollow();
                break;
            }
            default: {
                onEvtThink();
                break;
            }
        }
    }

    @Override
    protected final void onEvtThink() {
        final Playable actor = getActor();
        if (actor.isActionsDisabled())
            return;
        try {
            if (thinking++ > 1)
                return;
            switch (getIntention()) {
                case AI_INTENTION_ACTIVE: {
                    thinkActive();
                    break;
                }
                case AI_INTENTION_ATTACK: {
                    thinkAttack(false);
                    break;
                }
                case AI_INTENTION_CAST: {
                    thinkCast(false);
                    break;
                }
                case AI_INTENTION_PICK_UP: {
                    thinkPickUp();
                    break;
                }
                case AI_INTENTION_INTERACT: {
                    thinkInteract();
                    break;
                }
                case AI_INTENTION_FOLLOW: {
                    thinkFollow();
                    break;
                }
            }
        } catch (Exception e) {
            AbstractAI._log.error("", e);
        } finally {
            --thinking;
        }
    }

    protected void thinkActive() {
    }

    protected void thinkFollow() {
        final Playable actor = getActor();
        final Creature target = (Creature) _intention_arg0;
        final Integer offset = _intention_arg1 instanceof Integer ? (Integer) _intention_arg1 : null;
        if (target == null || actor.getDistance(target) > 4000.0 || offset == null) {
            clientActionFailed();
            return;
        }
        if (actor.isFollow && actor.getFollowTarget() == target) {
            clientActionFailed();
            return;
        }
        if (actor.isInRange(target, offset + 20) || actor.isMovementDisabled())
            clientActionFailed();
        if (_followTask != null) {
            _followTask.cancel(false);
            _followTask = null;
        }
        _followTask = ThreadPoolManager.getInstance().schedule(new ThinkFollow(), 250L);
    }

    @Override
    protected void onIntentionInteract(final GameObject object) {
        final Playable actor = getActor();
        if (actor.isActionsDisabled()) {
            setNextAction(nextAction.INTERACT, object, null, false, false);
            clientActionFailed();
            return;
        }
        clearNextAction();
        changeIntention(CtrlIntention.AI_INTENTION_INTERACT, object, null);
        onEvtThink();
    }

    protected void thinkInteract() {
        final Playable actor = getActor();
        final GameObject target = (GameObject) _intention_arg0;
        if (target == null) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            return;
        }
        final int range = (int) (Math.max(30.0f, actor.getMinDistance(target)) + 20.0f);
        if (actor.isInRangeZ(target, range)) {
            if (actor.isPlayer())
                ((Player) actor).doInteract(target);
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        } else {
            actor.moveToLocation(target.getLoc(), 40, true);
            setNextAction(nextAction.INTERACT, target, null, false, false);
        }
    }

    @Override
    protected void onIntentionPickUp(final GameObject object) {
        final Playable actor = getActor();
        if (actor.isActionsDisabled()) {
            setNextAction(nextAction.PICKUP, object, null, false, false);
            clientActionFailed();
            return;
        }
        clearNextAction();
        changeIntention(CtrlIntention.AI_INTENTION_PICK_UP, object, null);
        onEvtThink();
    }

    protected void thinkPickUp() {
        final Playable actor = getActor();
        final GameObject target = (GameObject) _intention_arg0;
        if (target == null) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            return;
        }
        if (actor.isInRange(target, 30L) && Math.abs(actor.getZ() - target.getZ()) < 50) {
            if (actor.isPlayer() || actor.isPet())
                actor.doPickupItem(target);
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        } else
            ThreadPoolManager.getInstance().execute(() -> {
                actor.moveToLocation(target.getLoc(), 10, true);
                setNextAction(nextAction.PICKUP, target, null, false, false);
            });
    }

    protected void thinkAttack(final boolean arrived) {
        final Playable actor = getActor();
        final Player player = actor.getPlayer();
        if (player == null) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            return;
        }

        if (actor.isActionsDisabled() || actor.isAttackingDisabled()) {
            actor.sendActionFailed();
            return;
        }

        boolean isPosessed = actor.isServitor() && ((Servitor) actor).isDepressed();

        final Creature attack_target = getAttackTarget();
        if (attack_target == null || attack_target.isDead() || !isPosessed && !(_forceUse ? attack_target.isAttackable(actor) : attack_target.isAutoAttackable(actor))) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
            return;
        }

        int range = actor.getPhysicalAttackRange();
        if (range < 10)
            range = 10;

        boolean canSee = GeoEngine.canSeeTarget(actor, attack_target) ||
                (actor.getDistance(attack_target) < actor.getPhysicalAttackRange() && attack_target.isDoor());
        if (!canSee && (range > 200 || Math.abs(actor.getZ() - attack_target.getZ()) > 200)) {

            ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, Math.max(range -30, 10)));
//			actor.sendPacket(Msg.CANNOT_SEE_TARGET);
//			setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
//			actor.sendActionFailed();
//			return;
        }

        range += (int) actor.getMinDistance(attack_target);
        if (actor.isInRangeZ(attack_target, arrived ? (long) (range + Config.ATTACK_RANGE_ARRIVED_ADD) : (long) (range + Config.ATTACK_RANGE_ADD))) {
            if (!canSee) {

                if (/*actor.getDistance(attack_target) > actor.getPhysicalAttackRange() && */ attack_target.isDoor()) {
                    ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, Math.max(actor.getPhysicalAttackRange() - 30, 15)));
                } else {
                    actor.sendPacket(Msg.CANNOT_SEE_TARGET);
                    setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                    actor.sendActionFailed();
                    return;
                }
            }
            //done by: skype viorel:4321, метод который иногда останавливал атаку рядом с мобами, исправлен.
            if(actor.getDistance(attack_target) <= actor.getPhysicalAttackRange()) {
                clientStopMoving(false);
                actor.doAttack(attack_target);
            } else {
                if(actor.getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW){
                    ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, Math.max(range - 30, 10)));
                } else {
                    ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, 10));
                }
            }
        } else if (!_dontMove) {
            ThreadPoolManager.getInstance().execute(new ExecuteFollow(attack_target, Math.max(range - 30, 10)));
        }
        else {
            actor.sendActionFailed();
        }
    }

    protected void thinkCast(final boolean arrived) {
        final Playable actor = getActor();
        final Creature target = getAttackTarget();
        if (_skill.getSkillType() == Skill.SkillType.CRAFT || Config.ALT_TOGGLE && _skill.isToggle()) {
            if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true))
                actor.doCast(_skill, target, _forceUse);
            return;
        }
        if (target == null || target.isDead() != _skill.getCorpse() && !_skill.isNotTargetAoE()) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
            return;
        }
        int range = actor.getMagicalAttackRange(_skill);
        if (range < 10)
            range = 10;
        final boolean canSee = _skill.getSkillType() == Skill.SkillType.TAKECASTLE || GeoEngine.canSeeTarget(actor, target) ||
                (target.isDoor() && actor.getDistance(target) < _skill.getCastRange()); //без этого не бёт двери
        final boolean noRangeSkill = _skill.getCastRange() == 32767;
        if (!noRangeSkill && !canSee && (range > 200 || Math.abs(actor.getZ() - target.getZ()) > 200)) {
            actor.sendPacket(Msg.CANNOT_SEE_TARGET);
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
            return;
        }
        range += (int) actor.getMinDistance(target);
        if (actor.isInRangeZ(target, arrived ? (long) (range + Config.CAST_RANGE_ARRIVED_ADD) : (long) (range + Config.CAST_RANGE_ADD)) || noRangeSkill) {
            if (!noRangeSkill && !canSee) {
                actor.sendPacket(Msg.CANNOT_SEE_TARGET);
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                actor.sendActionFailed();
                return;
            }
            if (_skill.getNextAction() == Skill.NextAction.ATTACK && target.isAutoAttackable(actor)) {
                setNextAction(nextAction.ATTACK, target, null, _forceUse, false);
            } else {
                clearNextAction();
            }
            if (_skill.checkCondition(actor, target, _forceUse, _dontMove, true)) {
                if (_skill.stopActor())
                    clientStopMoving(false);
                actor.doCast(_skill, target, _forceUse);
            } else {
                setNextIntention();
                if (getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
                    thinkAttack(false);
            }
        } else if (!_dontMove) {
            ThreadPoolManager.getInstance().execute(new ExecuteFollow(target, Math.max(range - 30, 10)));
        } else {
            actor.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
        }
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        clearNextAction();
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtFakeDeath() {
        clearNextAction();
        super.onEvtFakeDeath();
    }

    @Override
    public void Attack(final GameObject target, final boolean forceUse, final boolean dontMove) {
        final Playable actor = getActor();
        if (target.isCreature() && (actor.isActionsDisabled() || actor.isAttackingDisabled())) {
            if (actor.isMoving)
                clientStopMoving();
            setNextAction(nextAction.ATTACK, target, null, forceUse, false);
            actor.sendActionFailed();
            return;
        }
        _dontMove = dontMove;
        _forceUse = forceUse;
        clearNextAction();
        setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
    }

    @Override
    public void Cast(final Skill skill, final Creature target, final boolean forceUse, final boolean dontMove) {
        final Playable actor = getActor();
        if (skill.altUse() || Config.ALT_TOGGLE && skill.isToggle()) {
            if (skill.isHandler() && (actor.isOutOfControl() || actor.isStunned() || actor.isSleeping() || actor.isParalyzed() || actor.isDead()))
                clientActionFailed();
            else
                actor.altUseSkill(skill, target);
            return;
        }
        if (actor.isActionsDisabled()) {
            setNextAction(nextAction.CAST, skill, target, forceUse, dontMove);
            clientActionFailed();
            return;
        }
        _forceUse = forceUse;
        _dontMove = dontMove;
        clearNextAction();
        setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
    }

    @Override
    public Playable getActor() {
        return (Playable) super.getActor();
    }

    public enum nextAction {
        ATTACK,
        CAST,
        MOVE,
        REST,
        PICKUP,
        INTERACT;
    }

    protected class ThinkFollow implements Runnable {
        @Override
        public void run() {
            final Playable actor = getActor();
            if (getIntention() != CtrlIntention.AI_INTENTION_FOLLOW) {
                if (actor.isSummon() && getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
                    ((Servitor) actor).setFollowStatus(false, false);
                return;
            }
            if (!(_intention_arg0 instanceof Creature)) {
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                return;
            }
            final Creature target = (Creature) _intention_arg0;
            if (actor.getDistance(target) > 4000.0) {
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                return;
            }
            final Player player = actor.getPlayer();
            if (player == null || player.isLogoutStarted() || actor.isSummon() && player.getServitor() != actor) {
                setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
                return;
            }
            final int offset = (int) (_intention_arg1 instanceof Integer ? _intention_arg1 : 0);
            if (!actor.isAfraid() && !actor.isInRange(target, offset + 20) && (!actor.isFollow || actor.getFollowTarget() != target))
                actor.followToCharacter(target, offset, false);
            _followTask = ThreadPoolManager.getInstance().schedule(this, 250L);
        }
    }

    protected class ExecuteFollow implements Runnable {
        private Creature _target;
        private int _range;

        public ExecuteFollow(final Creature target, final int range) {
            _target = target;
            _range = range;
        }

        @Override
        public void run() {
//            if (_target.isDoor())
//                _actor.moveToLocation(_target.getLoc(), 40, true);
//            else
            if (!_actor.followToCharacter(_target, _range, true)) {
                _actor.sendActionFailed();
            }
        }
    }
}
