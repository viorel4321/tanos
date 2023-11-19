package l2s.gameserver.ai;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.math.random.RndSelector;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.AggroList;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Playable;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.World;
import l2s.gameserver.model.WorldRegion;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.instances.TamedBeastInstance;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.taskmanager.AiTaskManager;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.MinionList;
import l2s.gameserver.utils.NpcUtils;

public class DefaultAI extends CharacterAI
{
	protected static final Logger _log;
	public static final int TaskDefaultWeight = 10000;
	protected long AI_TASK_ATTACK_DELAY;
	protected long AI_TASK_ACTIVE_DELAY;
	protected long AI_TASK_DELAY_CURRENT;
	protected int MAX_PURSUE_RANGE;
	protected ScheduledFuture<?> _aiTask;
	protected ScheduledFuture<?> _madnessTask;
	private Lock _thinking;
	protected boolean _def_think;
	protected long _globalAggro;
	protected long _randomAnimationEnd;
	protected int _pathfindFails;
	protected final NavigableSet<Task> _tasks;
	protected final Skill[] _damSkills;
	protected final Skill[] _dotSkills;
	protected final Skill[] _debuffSkills;
	protected final Skill[] _healSkills;
	protected final Skill[] _buffSkills;
	protected final Skill[] _stunSkills;
	protected long _lastActiveCheck;
	protected long _checkAggroTimestamp;
	protected long _attackTimeout;
	protected long _lastFactionNotifyTime;
	protected long _minFactionNotifyInterval;
	protected boolean _isGlobal;
	private boolean _isSearchingMaster;

	public void addTaskCast(final Creature target, final Skill skill)
	{
		final Task task = new Task();
		task.type = TaskType.CAST;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskBuff(final Creature target, final Skill skill)
	{
		final Task task = new Task();
		task.type = TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskAttack(final Creature target)
	{
		final Task task = new Task();
		task.type = TaskType.ATTACK;
		task.target = target.getRef();
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskAttack(final Creature target, final Skill skill, final int weight)
	{
		final Task task = new Task();
		task.type = skill.isOffensive() ? TaskType.CAST : TaskType.BUFF;
		task.target = target.getRef();
		task.skill = skill;
		task.weight = weight;
		_tasks.add(task);
		_def_think = true;
	}

	public void addTaskMove(final Location loc, final boolean pathfind)
	{
		final Task task = new Task();
		task.type = TaskType.MOVE;
		task.loc = loc;
		task.pathfind = pathfind;
		_tasks.add(task);
		_def_think = true;
	}

	protected void addTaskMove(final int locX, final int locY, final int locZ, final boolean pathfind)
	{
		this.addTaskMove(new Location(locX, locY, locZ), pathfind);
	}

	public DefaultAI(final NpcInstance actor)
	{
		super(actor);
		AI_TASK_ATTACK_DELAY = Config.AI_TASK_ATTACK_DELAY;
		AI_TASK_ACTIVE_DELAY = Config.AI_TASK_ACTIVE_DELAY;
		AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
		_thinking = new ReentrantLock();
		_def_think = false;
		_tasks = new ConcurrentSkipListSet<Task>(TaskComparator.getInstance());
		_checkAggroTimestamp = 0L;
		_lastFactionNotifyTime = 0L;
		_minFactionNotifyInterval = 10000L;
		setAttackTimeout(Long.MAX_VALUE);
		_damSkills = actor.getTemplate().getDamageSkills();
		_dotSkills = actor.getTemplate().getDotSkills();
		_debuffSkills = actor.getTemplate().getDebuffSkills();
		_buffSkills = actor.getTemplate().getBuffSkills();
		_stunSkills = actor.getTemplate().getStunSkills();
		_healSkills = actor.getTemplate().getHealSkills();
		MAX_PURSUE_RANGE = actor.getParameter("MaxPursueRange", actor.isRaid() ? Config.MAX_PURSUE_RANGE_RAID : actor.isUnderground() ? Config.MAX_PURSUE_UNDERGROUND_RANGE : Config.MAX_PURSUE_RANGE);
		_minFactionNotifyInterval = actor.getParameter("FactionNotifyInterval", Config.FACTION_NOTIFY_INTERVAL);
		_isGlobal = actor.getParameter("GlobalAI", false);
		_isSearchingMaster = actor.getParameter("searchingMaster", false);
	}

	@Override
	public void run()
	{
		if(_aiTask == null)
			return;
		if(!isGlobalAI() && System.currentTimeMillis() - _lastActiveCheck > 60000L)
		{
			_lastActiveCheck = System.currentTimeMillis();
			final NpcInstance actor = getActor();
			final WorldRegion region = actor == null ? null : actor.getCurrentRegion();
			if(region == null || region.areNeighborsEmpty())
			{
				stopAITask();
				return;
			}
		}
		onEvtThink();
	}

	@Override
	public synchronized void startAITask()
	{
		if(_aiTask == null)
		{
			AI_TASK_DELAY_CURRENT = AI_TASK_ACTIVE_DELAY;
			_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
		}
	}

	protected final synchronized void switchAITask(final long NEW_DELAY)
	{
		if(_aiTask != null)
		{
			if(AI_TASK_DELAY_CURRENT == NEW_DELAY)
				return;
			_aiTask.cancel(false);
		}
		AI_TASK_DELAY_CURRENT = NEW_DELAY;
		_aiTask = AiTaskManager.getInstance().scheduleAtFixedRate(this, 0L, AI_TASK_DELAY_CURRENT);
	}

	@Override
	public synchronized void stopAITask()
	{
		if(_aiTask != null)
		{
			_aiTask.cancel(false);
			_aiTask = null;
		}
	}

	@Override
	public boolean isGlobalAI()
	{
		return _isGlobal;
	}

	protected boolean canSeeInSilentMove(final Playable target)
	{
		return getActor().getParameter("canSeeInSilentMove", false) || !target.isSilentMoving();
	}

	protected boolean checkAggression(final Creature target)
	{
		final NpcInstance actor = getActor();
		if(actor == null || getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
			return false;
		if(target.isAlikeDead())
			return false;
		if(target.isNpc() && target.isInvul())
			return false;
		if(target.isPlayable())
		{
			if(!canSeeInSilentMove((Playable) target))
				return false;
			if(actor.getFactionId().equalsIgnoreCase("varka_silenos_clan") && target.getPlayer() != null && target.getPlayer().getVarka() > 0)
				return false;
			if(actor.getFactionId().equalsIgnoreCase("ketra_orc_clan") && target.getPlayer() != null && target.getPlayer().getKetra() > 0)
				return false;
			if(target.isPlayer() && target.isInvisible())
				return false;
			if(((Playable) target).getNonAggroTime() > System.currentTimeMillis())
				return false;
			if(actor.isMonster() && target.isInZonePeace())
				return false;
		}
		return isInAggroRange(target) && canAttackCharacter(target) && GeoEngine.canSeeTarget(actor, target);
	}

	protected boolean isInAggroRange(final Creature target)
	{
		final NpcInstance actor = getActor();
		final AggroList.AggroInfo ai = actor.getAggroList().get(target);
		if(ai != null && ai.hate > 0)
		{
			if(!target.isInRangeZ(actor.getSpawnedLoc(), MAX_PURSUE_RANGE))
				return false;
		}
		else if(!isAggressive() || !target.isInRangeZ(actor.getLoc(), actor.getAggroRange()))
			return false;
		return true;
	}

	protected void setIsInRandomAnimation(final long time)
	{
		_randomAnimationEnd = System.currentTimeMillis() + time;
	}

	protected boolean randomAnimation()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.getParameter("noRandomAnimation", false))
			return false;
		if(actor.hasRandomAnimation() && !actor.isActionsDisabled() && !actor.isMoving && !actor.isInCombat() && Rnd.chance(Config.RND_ANIMATION_RATE))
		{
			setIsInRandomAnimation(3000L);
			actor.onRandomAnimation();
			return true;
		}
		return false;
	}

	protected boolean randomWalk()
	{
		final NpcInstance actor = getActor();
		return actor.hasRandomWalk() && !actor.isMoving && maybeMoveToHome();
	}

	protected Creature getNearestTarget(final List<Creature> targets)
	{
		final NpcInstance actor = getActor();
		Creature nextTarget = null;
		long minDist = Long.MAX_VALUE;
		for(int i = 0; i < targets.size(); ++i)
		{
			final Creature target = targets.get(i);
			final long dist = actor.getXYZDeltaSq(target.getX(), target.getY(), target.getZ());
			if(dist < minDist)
			{
				minDist = dist;
				nextTarget = target;
			}
		}
		return nextTarget;
	}

	protected boolean thinkActive()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isActionsDisabled())
			return true;
		if(actor.isBlocked() || _randomAnimationEnd > System.currentTimeMillis())
			return true;
		if(_def_think)
		{
			if(doTask())
				clearTasks();
			return true;
		}
		final long now = System.currentTimeMillis();
		if(now - _checkAggroTimestamp > Config.AGGRO_CHECK_INTERVAL)
		{
			_checkAggroTimestamp = now;
			final boolean aggressive = Rnd.chance(actor.getParameter("SelfAggressive", isAggressive() ? 100 : 0));
			if(!actor.getAggroList().isEmpty() || aggressive)
			{
				int count = 0;
				final List<Creature> chars = World.getAroundCharacters(actor, Config.AGGRO_CHECK_RADIUS, Config.AGGRO_CHECK_HEIGHT);
				final int size = Math.min(chars.size(), 1000);
				while(!chars.isEmpty())
				{
					if(++count > size)
						break;
					final Creature target = getNearestTarget(chars);
					if(target == null)
						break;
					if((aggressive || actor.getAggroList().get(target) != null) && checkAggression(target))
					{
						actor.getAggroList().addDamageHate(target, 0, 2);
						if(target.isSummon())
							actor.getAggroList().addDamageHate(target.getPlayer(), 0, 1);
						actor.setRunning();
						this.setIntention(CtrlIntention.AI_INTENTION_ATTACK, target);
						return true;
					}
					chars.remove(target);
				}
			}
		}
		if(actor.isChest() || actor.isBox() || actor instanceof TamedBeastInstance)
			return true;
		if(actor.isMinion())
		{
			final MonsterInstance leader = ((MonsterInstance) actor).getLeader();
			if(leader != null)
			{
				final double distance = actor.getDistance(leader.getX(), leader.getY());
				if(distance > 1000.0)
				{
					actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0L));
					actor.teleToLocation(leader.getMinionPosition());
					return true;
				}
				if(distance > 200.0)
				{
					this.addTaskMove(leader.getMinionPosition(), false);
					return true;
				}
			}
		}
		return randomAnimation() || randomWalk();
	}

	@Override
	protected void onIntentionIdle()
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		clearTasks();
		actor.stopMove();
		actor.getAggroList().clear(true);
		setAttackTimeout(Long.MAX_VALUE);
		setAttackTarget(null);
		changeIntention(CtrlIntention.AI_INTENTION_IDLE, null, null);
	}

	@Override
	protected void onIntentionActive()
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		actor.stopMove();
		setAttackTimeout(Long.MAX_VALUE);
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE)
		{
			switchAITask(AI_TASK_ACTIVE_DELAY);
			changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		}
		onEvtThink();
	}

	@Override
	protected void onIntentionAttack(final Creature target)
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		clearTasks();
		actor.stopMove();
		setAttackTarget(target);
		setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
		setGlobalAggro(0L);
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			changeIntention(CtrlIntention.AI_INTENTION_ATTACK, target, null);
			switchAITask(AI_TASK_ATTACK_DELAY);
		}
		onEvtThink();
	}

	protected boolean canAttackCharacter(final Creature target)
	{
		return target.isPlayable();
	}

	protected boolean isAggressive()
	{
		return getActor().isAggressive();
	}

	protected boolean checkTarget(final Creature target, final int range)
	{
		final NpcInstance actor = getActor();
		if(actor == null || target == null || target.isAlikeDead() || !actor.isInRangeZ(target, range))
			return false;
		final boolean hidden = target.isPlayable() && target.isInvisible();
		if(!hidden && actor.isConfused())
			return true;
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
			return canAttackCharacter(target);
		final AggroList.AggroInfo ai = actor.getAggroList().get(target);
		if(ai == null)
			return false;
		if(hidden)
		{
			ai.hate = 0;
			return false;
		}
		return ai.hate > 0;
	}

	public void setAttackTimeout(final long time)
	{
		_attackTimeout = time;
	}

	protected long getAttackTimeout()
	{
		return _attackTimeout;
	}

	protected void thinkAttack()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return;
		final Location loc = actor.getSpawnedLoc();
		if(!actor.isInRange(loc, MAX_PURSUE_RANGE))
		{
			teleportHome();
			return;
		}
		if(doTask() && !actor.isAttackingNow() && !actor.isCastingNow() && !createNewTask() && System.currentTimeMillis() > getAttackTimeout())
			this.returnHome();
	}

	@Override
	protected void onEvtSpawn()
	{
		setGlobalAggro(System.currentTimeMillis() + getActor().getParameter("globalAggro", Config.GLOBAL_AGGRO));
		if(getActor().isMinion())
			_isGlobal = ((MonsterInstance) getActor()).getLeader().getAI().isGlobalAI();
	}

	@Override
	protected void onEvtTeleported()
	{
		if(getActor().isRB())
		{
			final MonsterInstance master = (MonsterInstance) getActor();
			final MinionList minionList = master.getMinionList();
			if(minionList != null)
				for(final MonsterInstance minion : minionList.getAliveMinions())
				{
					minion.broadcastPacketToOthers(new MagicSkillUse(minion, minion, 2036, 1, 500, 0L));
					minion.teleToLocation(master.getMinionPosition());
					minion.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
				}
		}
	}

	@Override
	protected void onEvtReadyToAct()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrivedTarget()
	{
		onEvtThink();
	}

	@Override
	protected void onEvtArrived()
	{
		onEvtThink();
	}

	protected boolean tryMoveToTarget(final Creature target, final int offset)
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return false;
		if(!actor.followToCharacter(target, offset, true))
			++_pathfindFails;
		if(_pathfindFails >= getMaxPathfindFails() && System.currentTimeMillis() > getAttackTimeout() - getMaxAttackTimeout() + getTeleportTimeout() && actor.isInRange(target, MAX_PURSUE_RANGE))
		{
			_pathfindFails = 0;
			if(Config.NO_TELEPORT_TO_TARGET || actor.isRaid())
			{
				clearTasks();
				actor.stopMove();
				actor.getAggroList().remove(target, false);
				setAttackTimeout(Long.MAX_VALUE);
				setAttackTarget(null);
				changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
				return false;
			}
			if(target.isPlayable())
			{
				final AggroList.AggroInfo hate = actor.getAggroList().get(target);
				if(hate == null || hate.hate < 100)
				{
					this.returnHome();
					return false;
				}
			}
			Location loc = GeoEngine.moveCheckForAI(target.getLoc(), actor.getLoc(), actor.getGeoIndex());
			if(loc == null || !GeoEngine.canMoveToCoord(actor.getX(), actor.getY(), actor.getZ(), loc.x, loc.y, loc.z, actor.getGeoIndex()))
				loc = target.getLoc();
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0L));
			actor.teleToLocation(loc);
		}
		return true;
	}

	protected boolean maybeNextTask(final Task currentTask)
	{
		_tasks.remove(currentTask);
		return _tasks.size() == 0;
	}

	protected boolean doTask()
	{
		final NpcInstance actor = getActor();
		if(!_def_think)
			return true;
		final Task currentTask = _tasks.pollFirst();
		if(currentTask == null)
		{
			clearTasks();
			return true;
		}
		if(actor == null || actor.isDead() || actor.isAttackingNow() || actor.isCastingNow())
			return false;
		switch(currentTask.type)
		{
			case MOVE:
			{
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;
				if(actor.isInRange(currentTask.loc, 100L))
					return maybeNextTask(currentTask);
				if(actor.isMoving)
					return false;
				if(!actor.moveToLocation(currentTask.loc, 0, currentTask.pathfind))
				{
					this.clientStopMoving();
					_pathfindFails = 0;
					actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0L));
					actor.teleToLocation(currentTask.loc);
					return maybeNextTask(currentTask);
				}
				break;
			}
			case ATTACK:
			{
				final Creature target = currentTask.target.get();
				if(!checkTarget(target, MAX_PURSUE_RANGE))
					return true;
				setAttackTarget(target);
				if(actor.isMoving)
					return Rnd.chance(25);
				if(actor.getRealDistance3D(target) <= actor.getPhysicalAttackRange() + 40 && GeoEngine.canSeeTarget(actor, target))
				{
					this.clientStopMoving();
					_pathfindFails = 0;
					setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doAttack(target);
					return maybeNextTask(currentTask);
				}
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;
				tryMoveToTarget(target, actor.getPhysicalAttackRange());
				break;
			}
			case CAST:
			{
				final Creature target = currentTask.target.get();
				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId()))
					return true;
				final boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				final int castRange = currentTask.skill.getAOECastRange();
				if(!checkTarget(target, MAX_PURSUE_RANGE + castRange))
					return true;
				setAttackTarget(target);
				if(actor.getRealDistance3D(target) <= castRange + 60 && GeoEngine.canSeeTarget(actor, target))
				{
					this.clientStopMoving();
					_pathfindFails = 0;
					setAttackTimeout(getMaxAttackTimeout() + System.currentTimeMillis());
					actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}
				if(actor.isMoving)
					return Rnd.chance(10);
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;
				tryMoveToTarget(target, currentTask.skill.getCastRangeForAi());
				break;
			}
			case BUFF:
			{
				final Creature target = currentTask.target.get();
				if(actor.isMuted(currentTask.skill) || actor.isSkillDisabled(currentTask.skill) || actor.isUnActiveSkill(currentTask.skill.getId()))
					return true;
				if(currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF)
				{
					actor.doCast(currentTask.skill, actor, false);
					return maybeNextTask(currentTask);
				}
				if(target == null || target.isAlikeDead() || !actor.isInRange(target, 2000L))
					return true;
				final boolean isAoE = currentTask.skill.getTargetType() == Skill.SkillTargetType.TARGET_AURA;
				final int castRange = currentTask.skill.getAOECastRange();
				if(actor.isMoving)
					return Rnd.chance(10);
				if(actor.getRealDistance3D(target) <= castRange + 60 && GeoEngine.canSeeTarget(actor, target))
				{
					this.clientStopMoving();
					_pathfindFails = 0;
					actor.doCast(currentTask.skill, isAoE ? actor : target, !target.isPlayable());
					return maybeNextTask(currentTask);
				}
				if(actor.isMovementDisabled() || !getIsMobile())
					return true;
				tryMoveToTarget(target, currentTask.skill.getCastRangeForAi());
				break;
			}
		}
		return false;
	}

	protected boolean createNewTask()
	{
		return false;
	}

	protected boolean defaultNewTask()
	{
		clearTasks();
		final NpcInstance actor = getActor();
		final Creature target;
		if(actor == null || (target = prepareTarget()) == null)
			return false;
		final double distance = actor.getDistance(target);
		return chooseTaskAndTargets(null, target, distance);
	}

	@Override
	protected void onEvtThink()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isActionsDisabled() || actor.isAfraid())
			return;
		if(actor.isBlocked() || _randomAnimationEnd > System.currentTimeMillis())
			return;
		if(!_thinking.tryLock())
			return;
		try
		{
			if(!Config.BLOCK_ACTIVE_TASKS && getIntention() == CtrlIntention.AI_INTENTION_ACTIVE)
				thinkActive();
			else if(getIntention() == CtrlIntention.AI_INTENTION_ATTACK)
				thinkAttack();
		}
		finally
		{
			_thinking.unlock();
		}
	}

	@Override
	protected void onEvtDead(final Creature killer)
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		final int transformer = actor.getParameter("transformOnDead", 0);
		final int chance = actor.getParameter("transformChance", 100);
		if(transformer > 0 && Rnd.chance(chance))
		{
			final NpcInstance npc = NpcUtils.spawnSingle(transformer, actor.getLoc());
			if(killer != null && killer.isPlayable())
			{
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, killer, 100);
				killer.setTarget(npc);
				killer.sendPacket(new StatusUpdate(npc.getObjectId()).addAttribute(9, (int) npc.getCurrentHp()).addAttribute(10, npc.getMaxHp()));
			}
		}
		super.onEvtDead(killer);
	}

	@Override
	protected void onEvtClanAttacked(final Creature attacked, final Creature attacker, final int damage)
	{
		if(getIntention() != CtrlIntention.AI_INTENTION_ACTIVE || !isGlobalAggro())
			return;
		this.notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 2);
	}

	@Override
	protected void onEvtAttacked(final Creature attacker, final Skill skill, final int damage)
	{
		final NpcInstance actor = getActor();
		if(attacker == null || actor == null || actor.isDead())
			return;
		if(attacker.isPlayable() && actor.paralizeOnAttack(attacker))
		{
			actor.getAggroList().remove(attacker, false);
			((Playable) attacker).paralizeMe(actor);
			return;
		}
		final int transformer = actor.getParameter("transformOnUnderAttack", 0);
		if(transformer > 0)
		{
			final int chance = actor.getParameter("transformChance", 30);
			final int hp = actor.getParameter("transformHp", 50);
			if(chance == 100 || ((MonsterInstance) actor).getChampion() == 0 && actor.getCurrentHpPercents() > hp && Rnd.chance(chance))
			{
				final MonsterInstance npc = (MonsterInstance) NpcTable.getTemplate(transformer).getNewInstance();
				npc.setSpawnedLoc(actor.getLoc());
				npc.setChampion(((MonsterInstance) actor).getChampion());
				npc.spawnMe(npc.getSpawnedLoc());
				npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 100);
				actor.doDie(actor);
				actor.decayMe();
				attacker.setTarget(npc);
				attacker.sendPacket(new StatusUpdate(npc.getObjectId()).addAttribute(9, (int) npc.getCurrentHp()).addAttribute(10, npc.getMaxHp()));
				return;
			}
		}
		final Player player = attacker.getPlayer();
		if(player != null)
		{
			if(Config.ALLOW_SEVEN_SIGNS && Config.SEVEN_SIGNS_CHECK && (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) && actor.isSevenSignsMonster())
			{
				final int pcabal = SevenSigns.getInstance().getPlayerCabal(player);
				final int wcabal = SevenSigns.getInstance().getCabalHighestScore();
				if(pcabal != wcabal && wcabal != 0)
				{
					player.sendMessage("You have been teleported to the nearest town because you not signed for winning cabal.");
					player.teleToClosestTown();
					return;
				}
			}
			final List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.MOBGOTATTACKED);
			if(quests != null)
				for(final QuestState qs : quests)
					qs.getQuest().notifyAttack(actor, qs);
		}
		Creature myTarget = attacker;
		if(damage > 0 && attacker.isSummon())
		{
			final Player summoner = attacker.getPlayer();
			if(summoner != null)
				if(_isSearchingMaster)
					myTarget = summoner;
				else
					actor.getAggroList().addDamageHate(summoner, 0, 1);
		}
		actor.getAggroList().addDamageHate(myTarget, 0, damage);
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			actor.setRunning();
			this.setIntention(CtrlIntention.AI_INTENTION_ATTACK, myTarget);
		}
		notifyFriends(attacker, skill, damage);
	}

	@Override
	protected void onEvtAggression(final Creature attacker, final int aggro)
	{
		final NpcInstance actor = getActor();
		if(attacker == null || actor.isDead())
			return;
		Creature myTarget = attacker;
		if(aggro > 0 && attacker.isSummon())
		{
			final Player summoner = attacker.getPlayer();
			if(summoner != null)
				if(_isSearchingMaster)
					myTarget = summoner;
				else
					actor.getAggroList().addDamageHate(summoner, 0, 1);
		}
		actor.getAggroList().addDamageHate(myTarget, 0, aggro);
		if(getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
		{
			actor.animationShots();
			actor.setRunning();
			this.setIntention(CtrlIntention.AI_INTENTION_ATTACK, myTarget);
		}
	}

	protected boolean maybeMoveToHome()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;
		final boolean randomWalk = actor.hasRandomWalk();
		final Location sloc = actor.getSpawnedLoc();
		if(randomWalk && (!Config.RND_WALK || !Rnd.chance(Config.RND_WALK_RATE)))
			return false;
		final boolean isInRange = actor.isInRangeZ(sloc, Config.MAX_DRIFT_RANGE);
		if(!randomWalk && isInRange)
			return false;
		final Location pos = Location.findPointToStay(sloc, 0, Config.MAX_DRIFT_RANGE, actor.getGeoIndex());
		actor.setWalking();
		if(!actor.moveToLocation(pos, 0, true) && !isInRange)
			teleportHome();
		return true;
	}

	protected void returnHome()
	{
		this.returnHome(true, Config.ALWAYS_TELEPORT_HOME);
	}

	protected void teleportHome()
	{
		this.returnHome(true, true);
	}

	protected void returnHome(final boolean clearAggro, final boolean teleport)
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		final Location sloc = actor.getSpawnedLoc();
		clearTasks();
		actor.stopMove();
		if(clearAggro)
			actor.getAggroList().clear(true);
		setAttackTimeout(Long.MAX_VALUE);
		setAttackTarget(null);
		changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
		if(teleport)
		{
			actor.broadcastPacketToOthers(new MagicSkillUse(actor, actor, 2036, 1, 500, 0L));
			actor.teleToLocation(sloc.x, sloc.y, GeoEngine.getLowerHeight(sloc, actor.getGeoIndex()));
		}
		else
		{
			if(!clearAggro)
				actor.setRunning();
			else
				actor.setWalking();
			this.addTaskMove(sloc, false);
		}
	}

	protected Creature prepareTarget()
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return null;
		if(actor.isConfused())
			return getAttackTarget();
		if(Rnd.chance(actor.getParameter("isMadness", 0)))
		{
			final Creature randomHated = actor.getAggroList().getRandomHated();
			if(randomHated != null && Math.abs(actor.getZ() - randomHated.getZ()) < 1000)
			{
				setAttackTarget(randomHated);
				if(_madnessTask == null && !actor.isConfused())
				{
					actor.startConfused();
					_madnessTask = ThreadPoolManager.getInstance().schedule(new MadnessTask(), 10000L);
				}
				return randomHated;
			}
		}
		final List<Creature> hateList = actor.getAggroList().getHateList(MAX_PURSUE_RANGE);
		Creature hated = null;
		for(final Creature cha : hateList)
		{
			if(checkTarget(cha, MAX_PURSUE_RANGE))
			{
				hated = cha;
				break;
			}
			actor.getAggroList().remove(cha, true);
		}
		if(hated != null)
		{
			setAttackTarget(hated);
			return hated;
		}
		return null;
	}

	protected boolean canUseSkill(final Skill skill, final Creature target, final double distance)
	{
		final NpcInstance actor = getActor();
		if(actor == null || skill == null || skill.isNotUsedByAI())
			return false;
		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_SELF && target != actor)
			return false;
		final int castRange = skill.getAOECastRange();
		if(castRange <= 200 && distance > 200.0)
			return false;
		if(skill.getSkillType() == Skill.SkillType.TELEPORT_NPC && actor.getPhysicalAttackRange() > distance)
			return false;
		if(actor.isSkillDisabled(skill) || actor.isMuted(skill) || actor.isUnActiveSkill(skill.getId()))
			return false;
		double mpConsume2 = skill.getMpConsume2();
		if(skill.isMagic())
			mpConsume2 = actor.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, target, skill);
		else
			mpConsume2 = actor.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, target, skill);
		return actor.getCurrentMp() >= mpConsume2 && target.getAbnormalList().getCount(skill.getId()) == 0;
	}

	protected boolean canUseSkill(final Skill sk, final Creature target)
	{
		return this.canUseSkill(sk, target, 0.0);
	}

	protected Skill[] selectUsableSkills(final Creature target, final double distance, final Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return null;
		Skill[] ret = null;
		int usable = 0;
		for(final Skill skill : skills)
			if(this.canUseSkill(skill, target, distance))
			{
				if(ret == null)
					ret = new Skill[skills.length];
				ret[usable++] = skill;
			}
		if(ret == null || usable == skills.length)
			return ret;
		if(usable == 0)
			return null;
		ret = Arrays.copyOf(ret, usable);
		return ret;
	}

	protected static Skill selectTopSkillByDamage(final Creature actor, final Creature target, final double distance, final Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;
		if(skills.length == 1)
			return skills[0];
		final RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		for(final Skill skill : skills)
		{
			double weight = skill.getSimpleDamage(actor, target) * skill.getAOECastRange() / distance;
			if(weight < 1.0)
				weight = 1.0;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected static Skill selectTopSkillByDebuff(final Creature actor, final Creature target, final double distance, final Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;
		if(skills.length == 1)
			return skills[0];
		final RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		for(final Skill skill : skills)
			if(skill.getSameByStackType(target) == null)
			{
				double weight;
				if((weight = 100.0 * skill.getAOECastRange() / distance) <= 0.0)
					weight = 1.0;
				rnd.add(skill, (int) weight);
			}
		return rnd.select();
	}

	protected static Skill selectTopSkillByBuff(final Creature target, final Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;
		if(skills.length == 1)
			return skills[0];
		final RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		for(final Skill skill : skills)
			if(skill.getSameByStackType(target) == null)
			{
				double weight;
				if((weight = skill.getPower()) <= 0.0)
					weight = 1.0;
				rnd.add(skill, (int) weight);
			}
		return rnd.select();
	}

	protected static Skill selectTopSkillByHeal(final Creature target, final Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return null;
		final double hpReduced = target.getMaxHp() - target.getCurrentHp();
		if(hpReduced < 1.0)
			return null;
		if(skills.length == 1)
			return skills[0];
		final RndSelector<Skill> rnd = new RndSelector<Skill>(skills.length);
		for(final Skill skill : skills)
		{
			double weight;
			if((weight = Math.abs(skill.getPower() - hpReduced)) <= 0.0)
				weight = 1.0;
			rnd.add(skill, (int) weight);
		}
		return rnd.select();
	}

	protected void addDesiredSkill(final Map<Skill, Integer> skillMap, final Creature target, final double distance, final Skill[] skills)
	{
		if(skills == null || skills.length == 0 || target == null)
			return;
		for(final Skill sk : skills)
			this.addDesiredSkill(skillMap, target, distance, sk);
	}

	protected void addDesiredSkill(final Map<Skill, Integer> skillMap, final Creature target, final double distance, final Skill skill)
	{
		if(skill == null || target == null || !this.canUseSkill(skill, target))
			return;
		int weight = (int) -Math.abs(skill.getAOECastRange() - distance);
		if(skill.getAOECastRange() >= distance)
			weight += 1000000;
		else if(skill.isNotTargetAoE() && skill.getTargets(getActor(), target, false).size() == 0)
			return;
		skillMap.put(skill, weight);
	}

	protected void addDesiredHeal(final Map<Skill, Integer> skillMap, final Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		final double hpReduced = actor.getMaxHp() - actor.getCurrentHp();
		final double hpPercent = actor.getCurrentHpPercents();
		if(hpReduced < 1.0)
			return;
		for(final Skill sk : skills)
			if(this.canUseSkill(sk, actor) && sk.getPower() <= hpReduced)
			{
				int weight = (int) sk.getPower();
				if(hpPercent < 50.0)
					weight += 1000000;
				skillMap.put(sk, weight);
			}
	}

	protected void addDesiredBuff(final Map<Skill, Integer> skillMap, final Skill[] skills)
	{
		if(skills == null || skills.length == 0)
			return;
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		for(final Skill sk : skills)
			if(this.canUseSkill(sk, actor))
				skillMap.put(sk, 1000000);
	}

	protected Skill selectTopSkill(final Map<Skill, Integer> skillMap)
	{
		if(skillMap == null || skillMap.isEmpty())
			return null;
		int topWeight = Integer.MIN_VALUE;
		for(final Skill next : skillMap.keySet())
		{
			final int nWeight;
			if((nWeight = skillMap.get(next)) > topWeight)
				topWeight = nWeight;
		}
		if(topWeight == Integer.MIN_VALUE)
			return null;
		final Skill[] skills = new Skill[skillMap.size()];
		int nWeight = 0;
		for(final Map.Entry<Skill, Integer> e : skillMap.entrySet())
		{
			if(e.getValue() < topWeight)
				continue;
			skills[nWeight++] = e.getKey();
		}
		return skills[Rnd.get(nWeight)];
	}

	protected boolean chooseTaskAndTargets(final Skill skill, Creature target, final double distance)
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return false;
		if(skill != null)
		{
			if(actor.isMovementDisabled() && distance > skill.getAOECastRange() + 60)
			{
				target = null;
				if(skill.isOffensive())
				{
					final LazyArrayList<Creature> targets = LazyArrayList.newInstance();
					for(final Creature cha : actor.getAggroList().getHateList(MAX_PURSUE_RANGE))
						if(checkTarget(cha, skill.getAOECastRange() + 60))
						{
							if(!this.canUseSkill(skill, cha))
								continue;
							targets.add(cha);
						}
					if(!targets.isEmpty())
						target = targets.get(Rnd.get(targets.size()));
					LazyArrayList.recycle(targets);
				}
			}
			if(target == null)
				return false;
			if(skill.isOffensive())
				addTaskCast(target, skill);
			else
				addTaskBuff(target, skill);
			return true;
		}
		else
		{
			if(actor.isMovementDisabled() && distance > actor.getPhysicalAttackRange() + 40)
			{
				target = null;
				final LazyArrayList<Creature> targets = LazyArrayList.newInstance();
				for(final Creature cha : actor.getAggroList().getHateList(MAX_PURSUE_RANGE))
				{
					if(!checkTarget(cha, actor.getPhysicalAttackRange() + 40))
						continue;
					targets.add(cha);
				}
				if(!targets.isEmpty())
					target = targets.get(Rnd.get(targets.size()));
				LazyArrayList.recycle(targets);
			}
			if(target == null)
				return false;
			this.addTaskAttack(target);
			return true;
		}
	}

	@Override
	public boolean isActive()
	{
		return _aiTask != null;
	}

	protected void clearTasks()
	{
		_def_think = false;
		_tasks.clear();
	}

	protected boolean isGlobalAggro()
	{
		if(_globalAggro == 0L)
			return true;
		if(_globalAggro <= System.currentTimeMillis())
		{
			_globalAggro = 0L;
			return true;
		}
		return false;
	}

	@Override
	public void setGlobalAggro(final long value)
	{
		_globalAggro = value;
	}

	@Override
	public NpcInstance getActor()
	{
		return (NpcInstance) super.getActor();
	}

	protected boolean defaultThinkBuff(final int rateSelf)
	{
		return this.defaultThinkBuff(rateSelf, 0);
	}

	protected void notifyFriends(final Creature attacker, final Skill skill, final int damage)
	{
		final NpcInstance actor = getActor();
		if(actor == null)
			return;
		if(System.currentTimeMillis() - _lastFactionNotifyTime > _minFactionNotifyInterval)
		{
			_lastFactionNotifyTime = System.currentTimeMillis();
			if(actor.isMinion())
			{
				final MonsterInstance master = ((MonsterInstance) actor).getLeader();
				if(master != null)
				{
					if(!master.isDead() && master.isVisible())
						master.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, skill, damage);
					final MinionList minionList = master.getMinionList();
					if(minionList != null)
						for(final MonsterInstance minion : minionList.getAliveMinions())
							if(minion != actor)
								minion.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, skill, damage);
				}
			}
			if(actor.hasMinions())
			{
				final MinionList minionList2 = actor.getMinionList();
				if(minionList2.hasAliveMinions())
					for(final MonsterInstance minion2 : minionList2.getAliveMinions())
						minion2.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, attacker, skill, damage);
			}
			for(final NpcInstance npc : activeFactionTargets())
				npc.getAI().notifyEvent(CtrlEvent.EVT_CLAN_ATTACKED, actor, attacker, damage);
		}
	}

	protected List<NpcInstance> activeFactionTargets()
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.getFactionId() == null || actor.getFactionId().isEmpty() || actor.getFactionRange() <= 0)
			return Collections.emptyList();
		final String attacked_name = actor.getName();
		final int range = actor.getFactionRange();
		final List<NpcInstance> npcFriends = new LazyArrayList<NpcInstance>();
		for(final NpcInstance npc : World.getAroundNpc(actor))
		{
			final String my_name = npc.getName();
			if(my_name.startsWith("Lilim ") && attacked_name.startsWith("Nephilim "))
				continue;
			if(my_name.startsWith("Nephilim ") && attacked_name.startsWith("Lilim "))
				continue;
			if(my_name.startsWith("Lith ") && attacked_name.startsWith("Gigant "))
				continue;
			if(my_name.startsWith("Gigant ") && attacked_name.startsWith("Lith "))
				continue;
			if(npc.isDead() || !npc.isInRangeZ(actor, range) || !npc.getFactionId().equalsIgnoreCase(actor.getFactionId()))
				continue;
			npcFriends.add(npc);
		}
		return npcFriends;
	}

	protected boolean defaultThinkBuff(final int rateSelf, final int rateFriends)
	{
		final NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return true;
		if(!Rnd.chance(rateSelf))
		{
			if(Rnd.chance(rateFriends))
				for(final NpcInstance npc : activeFactionTargets())
				{
					final double targetHp = npc.getCurrentHpPercents();
					final Skill[] skills = targetHp < 50.0 ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
					if(skills != null)
					{
						if(skills.length == 0)
							continue;
						final Skill skill = skills[Rnd.get(skills.length)];
						addTaskBuff(actor, skill);
						return true;
					}
				}
			return false;
		}
		final double actorHp = actor.getCurrentHpPercents();
		final Skill[] skills2 = actorHp < 50.0 ? selectUsableSkills(actor, 0.0, _healSkills) : selectUsableSkills(actor, 0.0, _buffSkills);
		if(skills2 == null || skills2.length == 0)
			return false;
		final Skill skill2 = skills2[Rnd.get(skills2.length)];
		addTaskBuff(actor, skill2);
		return true;
	}

	protected boolean defaultFightTask()
	{
		clearTasks();
		final NpcInstance actor = getActor();
		if(actor == null || actor.isDead())
			return false;
		final Creature target;
		if((target = prepareTarget()) == null)
			return false;
		final double distance = actor.getDistance(target);
		final double targetHp = target.getCurrentHpPercents();
		final double actorHp = actor.getCurrentHpPercents();
		final Skill[] dam = Rnd.chance(getRateDAM()) ? selectUsableSkills(target, distance, _damSkills) : null;
		final Skill[] dot = Rnd.chance(getRateDOT()) ? selectUsableSkills(target, distance, _dotSkills) : null;
		final Skill[] debuff = targetHp > 10.0 ? Rnd.chance(getRateDEBUFF()) ? selectUsableSkills(target, distance, _debuffSkills) : null : null;
		final Skill[] stun = Rnd.chance(getRateSTUN()) ? selectUsableSkills(target, distance, _stunSkills) : null;
		final Skill[] heal = actorHp < 50.0 ? Rnd.chance(getRateHEAL()) ? selectUsableSkills(actor, 0.0, _healSkills) : null : null;
		final Skill[] buff = Rnd.chance(getRateBUFF()) ? selectUsableSkills(actor, 0.0, _buffSkills) : null;
		final RndSelector<Skill[]> rnd = new RndSelector<Skill[]>();
		rnd.add(null, getRatePHYS());
		rnd.add(dam, getRateDAM());
		rnd.add(dot, getRateDOT());
		rnd.add(debuff, getRateDEBUFF());
		rnd.add(heal, getRateHEAL());
		rnd.add(buff, getRateBUFF());
		rnd.add(stun, getRateSTUN());
		final Skill[] selected = rnd.select();
		if(selected != null)
		{
			if(selected == dam || selected == dot)
				return chooseTaskAndTargets(selectTopSkillByDamage(actor, target, distance, selected), target, distance);
			if(selected == debuff || selected == stun)
				return chooseTaskAndTargets(selectTopSkillByDebuff(actor, target, distance, selected), target, distance);
			if(selected == buff)
				return chooseTaskAndTargets(selectTopSkillByBuff(actor, selected), actor, distance);
			if(selected == heal)
				return chooseTaskAndTargets(selectTopSkillByHeal(actor, selected), actor, distance);
		}
		return chooseTaskAndTargets(null, target, distance);
	}

	public int getRatePHYS()
	{
		return 100;
	}

	public int getRateDOT()
	{
		return 0;
	}

	public int getRateDEBUFF()
	{
		return 0;
	}

	public int getRateDAM()
	{
		return 0;
	}

	public int getRateSTUN()
	{
		return 0;
	}

	public int getRateBUFF()
	{
		return 0;
	}

	public int getRateHEAL()
	{
		return 0;
	}

	public boolean getIsMobile()
	{
		return !getActor().getParameter("isImmobilized", false);
	}

	public int getMaxPathfindFails()
	{
		return Config.MAX_PATHFIND_FAILS;
	}

	public int getMaxAttackTimeout()
	{
		return Config.MAX_ATTACK_TIMEOUT;
	}

	public int getTeleportTimeout()
	{
		return Config.TELEPORT_TIMEOUT;
	}

	@Override
	public boolean isNulled()
	{
		return false;
	}

	static
	{
		_log = LoggerFactory.getLogger(DefaultAI.class);
	}

	public enum TaskType
	{
		MOVE,
		ATTACK,
		CAST,
		BUFF;
	}

	public static class Task
	{
		public TaskType type;
		public Skill skill;
		public HardReference<? extends Creature> target;
		public Location loc;
		public boolean pathfind;
		public int weight;

		public Task()
		{
			weight = 10000;
		}
	}

	private static class TaskComparator implements Comparator<Task>
	{
		private static final Comparator<Task> instance;

		public static final Comparator<Task> getInstance()
		{
			return TaskComparator.instance;
		}

		@Override
		public int compare(final Task o1, final Task o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o2.weight - o1.weight;
		}

		static
		{
			instance = new TaskComparator();
		}
	}

	protected class Teleport implements Runnable
	{
		Location _destination;

		public Teleport(final Location destination)
		{
			_destination = destination;
		}

		@Override
		public void run()
		{
			final NpcInstance actor = getActor();
			if(actor != null)
				actor.teleToLocation(_destination);
		}
	}

	protected class MadnessTask implements Runnable
	{
		@Override
		public void run()
		{
			final NpcInstance actor = getActor();
			if(actor != null)
				actor.stopConfused();
			_madnessTask = null;
		}
	}
}
