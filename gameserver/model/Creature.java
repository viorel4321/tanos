package l2s.gameserver.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import l2s.gameserver.stats.StatTemplate;
import l2s.gameserver.stats.triggers.RunnableTrigger;
import l2s.gameserver.stats.triggers.TriggerInfo;
import l2s.gameserver.stats.triggers.TriggerType;
import l2s.gameserver.tables.SkillTable;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.set.hash.TIntHashSet;
import l2s.commons.collections.LazyArrayList;
import l2s.commons.geometry.Circle;
import l2s.commons.geometry.Shape;
import l2s.commons.lang.reference.HardReference;
import l2s.commons.lang.reference.HardReferences;
import l2s.commons.listener.Listener;
import l2s.commons.util.Rnd;
import l2s.commons.util.concurrent.atomic.AtomicState;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.ai.CtrlEvent;
import l2s.gameserver.ai.CtrlIntention;
import l2s.gameserver.ai.PlayableAI;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.geodata.GeoMove;
import l2s.gameserver.instancemanager.DimensionalRiftManager;
import l2s.gameserver.instancemanager.ZoneManager;
import l2s.gameserver.listener.actor.CharListenerList;
import l2s.gameserver.listener.actor.recorder.CharStatsChangeRecorder;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.actor.instances.creature.AbnormalList;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.quest.QuestEventType;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.model.reference.L2Reference;
import l2s.gameserver.network.l2.components.CustomMessage;
import l2s.gameserver.network.l2.s2c.Attack;
import l2s.gameserver.network.l2.s2c.AutoAttackStart;
import l2s.gameserver.network.l2.s2c.AutoAttackStop;
import l2s.gameserver.network.l2.s2c.ChangeMoveType;
import l2s.gameserver.network.l2.s2c.CharMoveToLocation;
import l2s.gameserver.network.l2.s2c.FlyToLocation;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MagicSkillCanceled;
import l2s.gameserver.network.l2.s2c.MagicSkillLaunched;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.MoveToPawn;
import l2s.gameserver.network.l2.s2c.SetupGauge;
import l2s.gameserver.network.l2.s2c.StatusUpdate;
import l2s.gameserver.network.l2.s2c.StopMove;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.network.l2.s2c.TargetUnselected;
import l2s.gameserver.network.l2.s2c.TeleportToLocation;
import l2s.gameserver.network.l2.s2c.ValidateLocation;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.skills.AbnormalEffect;
import l2s.gameserver.skills.Calculator;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.TimeStamp;
import l2s.gameserver.skills.funcs.Func;
import l2s.gameserver.tables.MapRegionTable;
import l2s.gameserver.taskmanager.LazyPrecisionTaskManager;
import l2s.gameserver.taskmanager.RegenTaskManager;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.templates.npc.NpcTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public abstract class Creature extends GameObject
{
	private static final long serialVersionUID = 1L;
	protected static final Logger _log = LoggerFactory.getLogger(Creature.class);
	public static final double HEADINGS_IN_PI = 10430.378350470453;
	public static final int INTERACTION_DISTANCE = 150;
	private static final int arear = 55501730;
	private static final boolean _dds = true;
	private Skill _castingSkill;
	private long _castInterruptTime;
	private long _animationEndTime;
	public Future<?> _skillTask;
	public Future<?> _skillLaunchedTask;
	public Future<?> _skillCheck;
	private Future<?> _stanceTask;
	private Runnable _stanceTaskRunnable;
	private long _stanceEndTime;
	public static final int CLIENT_BAR_SIZE = 352;
	private int _lastHpBarUpdate = -1;
	private int _lastMpBarUpdate = -1;
	protected double _currentCp = 0.;
	protected double _currentHp = 1.;
	protected double _currentMp = 1.;
	protected boolean _isAttackAborted;
	protected long _attackEndTime;
	protected long _attackReuseEndTime;

	protected final IntObjectMap<Skill> _skills = new CTreeIntObjectMap<>();
	protected Map<TriggerType, Set<TriggerInfo>> _triggers;
	protected IntObjectMap<TimeStamp> _skillReuses = new CHashIntObjectMap<>();

	protected volatile AbnormalList _effectList;
	protected volatile CharStatsChangeRecorder<? extends Creature> _statsRecorder;
	private Clan _clan;
	protected ForceBuff _forceBuff;
	private List<Stats> _blockedStats;
	private int _abnormalEffects;
	protected AtomicBoolean isDead = new AtomicBoolean();
	private boolean _flying;
	private boolean _riding;
	private boolean _fakeDeath;
	protected boolean _isInvul;
	protected boolean _isTeleporting;
	private long _dropDisabled;
	private AtomicState _effectImmunity = new AtomicState();
	private byte _buffImmunity;
	private byte _debuffImmunity;
	private byte _blockBuff;
	private HashMap<Integer, Byte> _skillMastery;
	private boolean _afraid;
	private boolean _meditated;
	private boolean _muted;
	private boolean _pmuted;
	private AtomicState _paralyzed = new AtomicState();
	private boolean _rooted;
	private boolean _sleeping;
	private boolean _stunned;
	private boolean _immobilized;
	private boolean _confused;
	private boolean _blocked;
	private boolean _running;
	public boolean isMoving;
	public boolean isFollow;
	private final Lock moveLock = new ReentrantLock();
	private Future<?> _moveTask;
	private MoveNextTask _moveTaskRunnable;
	private List<Location> _moveList;
	private Location destination;
	private final Location movingDestTempPos = new Location();
	private int _offset;
	private boolean _forestalling;
	private volatile HardReference<? extends GameObject> _target = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _castingTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _followTarget = HardReferences.emptyRef();
	private volatile HardReference<? extends Creature> _aggressionTarget = HardReferences.emptyRef();
	private final List<List<Location>> _targetRecorder = new ArrayList<List<Location>>();
	private long _followTimestamp;
	private long _startMoveTime;
	private int _previousSpeed = 0;
	private int _heading;
	private final Calculator[] _calculators;
	private final CreatureTemplate _template;
	protected CharacterAI _ai;
	protected String _name;
	protected String _title;
	private boolean _isRegenerating;
	private Future<?> _regenTask;
	private Runnable _regenTaskRunnable;
	private final Lock regenLock = new ReentrantLock();
	protected HardReference<? extends Creature> reference;
	private long _lastAttackAnimPacket = 0L;
	private Location _flyLoc;
	private long _nonAggroTime;
	private TIntHashSet _unActiveSkills = new TIntHashSet();
	protected volatile CharListenerList listeners;
	private List<Player> _statusListeners;
	private final Lock statusListenersLock = new ReentrantLock();

	public Creature(final int objectId, final CreatureTemplate template)
	{
		super(objectId);

		_template = template;
		_calculators = new Calculator[Stats.NUM_STATS];

		if(template != null && (isNpc() || isSummon()) && ((NpcTemplate) template).getSkills().size() > 0)
		{
			for(final Skill skill : ((NpcTemplate) template).getSkills().values())
				addSkill(skill);
		}

		Formulas.addFuncsToNewCharacter(this);
		reference = new L2Reference<Creature>(this);

		if(!isPlayer())	// Игрока начинаем хранить после полного рестора.
			GameObjectsStorage.put(this);
	}

	@Override
	public HardReference<? extends Creature> getRef()
	{
		return reference;
	}

	public final void abortAttack(final boolean force, final boolean msg)
	{
		if(isAttackingNow())
		{
			_attackEndTime = 0L;
			if(force)
				_isAttackAborted = true;
			if(isPlayer())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				sendActionFailed();
				if(msg)
					sendMessage(new CustomMessage("l2s.gameserver.model.Creature.AttackAborted"));
			}
		}
	}

	public final void abortCast(final boolean force, final boolean msg)
	{
		if(isCastingNow() && (force || canAbortCast()))
		{
			final Skill castingSkill = _castingSkill;
			final Future<?> skillTask = _skillTask;
			final Future<?> skillLaunchedTask = _skillLaunchedTask;
			final Future<?> skillCheck = _skillCheck;
			finishFly();
			clearCastVars();
			if(skillCheck != null)
				skillCheck.cancel(false);
			if(skillTask != null)
				skillTask.cancel(false);
			if(skillLaunchedTask != null)
				skillLaunchedTask.cancel(false);
			if(castingSkill != null)
			{
				if(castingSkill.isUsingWhileCasting())
				{
					final Creature target = getAI().getAttackTarget();
					if(target != null)
						target.getAbnormalList().stop(castingSkill.getId());
				}
				removeSkillMastery(castingSkill.getId());
			}
			if(_forceBuff != null)
				_forceBuff.delete();
			broadcastPacket(new MagicSkillCanceled(getObjectId()));
			if(isPlayer())
			{
				getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				if(msg)
					sendPacket(Msg.CASTING_HAS_BEEN_INTERRUPTED);
			}
		}
	}

	public void absorbAndReflect(final Creature target, final Skill skill, double damage, final int poleAttackCount)
	{
		if(target.isDead() || target.isDoor() || target.isParalyzed() && target.isInvul())
			return;
		final boolean bow = getActiveWeaponItem() != null && getActiveWeaponItem().getItemType() == WeaponTemplate.WeaponType.BOW;
		double value = 0.0;
		if(skill == null && !bow)
			value = target.calcStat(Stats.REFLECT_DAMAGE_PERCENT, 0.0, this, null);
		if(value > 0.0 && !isInvul() && target.getCurrentHp() + target.getCurrentCp() > damage) {
			if (target.isPlayer() || target.isPet() || target.isSummon()) { //рефлект урон получают игроки, петы и сумоны
				applyReflectDamage(target, damage, value);
			}
		}
		if(skill != null || bow)
			return;
		damage = (int) (damage - target.getCurrentCp());
		if(damage <= 0.0)
			return;
		final double poleMod = poleAttackCount < Config.POLE_VAMPIRIC_MOD.length ? Config.POLE_VAMPIRIC_MOD[poleAttackCount] : 0.0;
		double absorb = poleMod * calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, target, null);
		if(absorb > 0.0 && !target.isInvul())
			setCurrentHp(_currentHp + damage * absorb / 100.0, false);
		absorb = poleMod * calcStat(Stats.ABSORB_DAMAGEMP_PERCENT, 0.0, target, null);
		if(absorb > 0.0 && !target.isInvul())
			setCurrentMp(_currentMp + damage * absorb / 100.0);
	}

	public void applyReflectDamage(final Creature target, final double damage, final double reflect)
	{
		double rdmg = damage * reflect / 100.0;
		rdmg = Math.min(rdmg, target.getCurrentHp());
		if(rdmg >= 1.0)
		{
			if(isPlayable() && !target.isNpc()) // && target.isPlayer() отменить рефлет от монстров и РБ
				reduceCurrentHp(rdmg, this, null, 0, false, true, true, false, false, false, false, false);
			else
				reduceCurrentHp(rdmg, target, null, 0, false, true, true, false, false, false, false, false);
			displayReceiveDamageMessage(target, (int) rdmg);
		}
	}

	public void addBlockStats(final List<Stats> stats)
	{
		if(_blockedStats == null)
			_blockedStats = new ArrayList<Stats>();
		_blockedStats.addAll(stats);
	}

	public Skill addSkill(final Skill newSkill)
	{
		if(newSkill == null)
			return null;

		Skill oldSkill = _skills.get(newSkill.getId());
		if (oldSkill != null && oldSkill.getLevel() == newSkill.getLevel())
			return newSkill;

		_skills.put(newSkill.getId(), newSkill);

		if (isPlayer() && newSkill.getReuseGroupId() > 0) {
			if (getPlayer()._gskills.containsKey(newSkill.getReuseGroupId()))
				getPlayer()._gskills.get(newSkill.getReuseGroupId()).add(newSkill);
			else {
				final List<Skill> list = new ArrayList<Skill>();
				list.add(newSkill);
				getPlayer()._gskills.put(newSkill.getReuseGroupId(), list);
			}
		}

		if (oldSkill != null) {
			removeStatsOwner(oldSkill);
			removeTriggers(oldSkill);
		}

		if (!isUnActiveSkill(newSkill.getId())) {
			addStatFuncs(newSkill.getStatFuncs());
			addTriggers(newSkill);
		}
		return oldSkill;
	}

	public final void addStatFunc(final Func f)
	{
		if(f == null)
			return;
		final int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] == null)
				_calculators[stat] = new Calculator(f.stat, this);
			_calculators[stat].addFunc(f);
		}
	}

	public final void addStatFuncs(final Func[] funcs)
	{
		for(final Func f : funcs)
			addStatFunc(f);
	}

	public final void removeStatFunc(final Func f)
	{
		if(f == null)
			return;
		final int stat = f.stat.ordinal();
		synchronized (_calculators)
		{
			if(_calculators[stat] != null)
				_calculators[stat].removeFunc(f);
		}
	}

	public final void removeStatFuncs(final Func[] funcs)
	{
		for(final Func f : funcs)
			removeStatFunc(f);
	}

	public final void removeStatsOwner(final Object owner)
	{
		synchronized (_calculators)
		{
			for(int i = 0; i < _calculators.length; ++i)
				if(_calculators[i] != null)
					_calculators[i].removeOwner(owner);
		}
	}

	public void altOnMagicUseTimer(final Creature aimingTarget, final Skill skill)
	{
		final boolean ae = !Config.ALT_TOGGLE && skill.isToggle();
		if(isDead())
		{
			if(ae)
				_skillTask = null;
			return;
		}
		final int magicId = skill.getDisplayId();
		int level = getSkillDisplayLevel(skill.getId());
		if(level < 1 && skill.isToggle())
		{
			if(ae)
				_skillTask = null;
			return;
		}

		level = Math.max(1, level);

		Set<Creature> targets = skill.getTargets(this, aimingTarget, true);
		if(!skill.isToggle())
			broadcastPacket(new MagicSkillLaunched(getObjectId(), magicId, level, targets));

		final double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0.0)
		{
			if(_currentMp < mpConsume2)
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				if(ae)
					_skillTask = null;
				return;
			}
			if(skill.isMagic())
				reduceCurrentMp(calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
			else
				reduceCurrentMp(calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill), null);
		}

		callSkill(aimingTarget, skill, targets, false);

		if(ae)
			_skillTask = null;
	}

	public void altUseSkill(final Skill skill, Creature target)
	{
		if(skill == null)
			return;

		final int magicId = skill.getId();
		if(isUnActiveSkill(magicId))
			return;

		if(isSkillDisabled(skill))
		{
			if(Config.ALT_SHOW_REUSE_MSG && isPlayer())
				sendPacket(new SystemMessage(48).addSkillName(magicId, skill.getDisplayLevel()));
			return;
		}

		if(target == null)
		{
			target = skill.getAimingTarget(this, getTarget());
			if(target == null)
				return;
		}

		final int[] itemConsume = skill.getItemConsume();
		if(itemConsume[0] > 0)
			for(int i = 0; i < itemConsume.length; ++i)
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(Msg.INCORRECT_ITEM_COUNT);
					sendChanges();
					return;
				}

		fireMethodInvoked("Creature.altUseSkill", new Object[] { skill, target });
		final int level = Math.max(1, getSkillDisplayLevel(magicId));
		Formulas.calcSkillMastery(skill, this);
		final long reuseDelay = Formulas.calcSkillReuseDelay(this, skill);
		if(!skill.isToggle())
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skill.getHitTime(), reuseDelay));

		if(!skill.isHideUseMessage())
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON)
				sendPacket(new SystemMessage(547));
			else if(!skill.isHandler())
				sendPacket(new SystemMessage(46).addSkillName(magicId, (short) level));
			else
				sendPacket(new SystemMessage(46).addItemName(Integer.valueOf(skill.getItemConsumeId()[0])));

		if(reuseDelay > 10L)
		{
			disableItem(skill, reuseDelay, reuseDelay);
			disableSkill(skill, reuseDelay);
		}

		if(!Config.ALT_TOGGLE && skill.isToggle())
			_skillTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.AltMagicUseTask(this, target, skill), Math.max(skill.getHitTime(), 50));
		else
			ThreadPoolManager.getInstance().schedule(new GameObjectTasks.AltMagicUseTask(this, target, skill), skill.getHitTime());
	}

	public void broadcastPacket(final L2GameServerPacket... packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacket(final List<L2GameServerPacket> packets)
	{
		sendPacket(packets);
		broadcastPacketToOthers(packets);
	}

	public void broadcastPacketToOthers(final L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;

		for(Player player : World.getAroundPlayers(this))
			player.sendPacket(packets);
	}

	public void broadcastPacketToOthers(final List<L2GameServerPacket> packets)
	{
		if(!isVisible() || packets.isEmpty())
			return;

		for(Player player : World.getAroundPlayers(this))
			player.sendPacket(packets);
	}

	public void broadcastStatusUpdate()
	{
		if(!isVisible() || !needHpUpdate())
			return;
		broadcastToStatusListeners(new StatusUpdate(getObjectId()).addAttribute(9, (int) getCurrentHp()));
	}

	public int calcHeading(final Location dest)
	{
		if(dest == null)
			return 0;
		if(Math.abs(getX() - dest.x) == 0 && Math.abs(getY() - dest.y) == 0)
			return _heading;
		return calcHeading(dest.x, dest.y);
	}

	public int calcHeading(final int x_dest, final int y_dest)
	{
		return (int) (Math.atan2(getY() - y_dest, getX() - x_dest) * 10430.378350470453) + 32768;
	}

	public final double calcStat(final Stats stat, final double init)
	{
		return calcStat(stat, init, null, null);
	}

	public final double calcStat(final Stats stat, final double init, final Creature target, final Skill skill)
	{
		final int id = stat.ordinal();
		final Calculator c = _calculators[id];
		if(c == null)
			return init;
		final Env env = new Env();
		env.character = this;
		env.target = target;
		env.skill = skill;
		env.value = init;
		c.calc(env);
		return env.value;
	}

	public final double calcStat(final Stats stat, final Creature target, final Skill skill)
	{
		final Env env = new Env(this, target, skill);
		env.value = stat.getInit();
		final int id = stat.ordinal();
		final Calculator c = _calculators[id];
		if(c != null)
			c.calc(env);
		return env.value;
	}

	public int calculateAttackDelay()
	{
		return Formulas.calcPAtkSpd(getPAtkSpd());
	}

	public void callSkill(Creature aimingTarget, Skill skill, Set<Creature> targets, boolean useActionSkills)
	{
		try
		{
			if(useActionSkills)
			{
				if(skill.isOffensive())
				{
					useTriggers(aimingTarget, TriggerType.OFFENSIVE_SKILL_USE, null, skill, 0);
					
					if(skill.isMagic())
						useTriggers(aimingTarget, TriggerType.OFFENSIVE_MAGICAL_SKILL_USE, null, skill, 0);
					else if(skill.isPhysic())
						useTriggers(aimingTarget, TriggerType.OFFENSIVE_PHYSICAL_SKILL_USE, null, skill, 0);
				}
				else
				{
					useTriggers(aimingTarget, TriggerType.SUPPORT_SKILL_USE, null, skill, 0);
					
					if(skill.isMagic())
						useTriggers(aimingTarget, TriggerType.SUPPORT_MAGICAL_SKILL_USE, null, skill, 0);
					else if(skill.isPhysic())
						useTriggers(aimingTarget, TriggerType.SUPPORT_PHYSICAL_SKILL_USE, null, skill, 0);
				}

				useTriggers(this, TriggerType.ON_CAST_SKILL, null, skill, 0);
			}

			if(isPlayer())
			{
				final Player pl = (Player) this;
				for(final Creature target2 : targets)
					if(target2 != null && target2.isNpc())
					{
						final NpcInstance npc = (NpcInstance) target2;
						final List<QuestState> ql = pl.getQuestsForEvent(npc, QuestEventType.MOB_TARGETED_BY_SKILL);
						if(ql == null)
							continue;
						for(final QuestState qs : ql)
							qs.getQuest().notifySkillUse(npc, skill, qs);
					}
			}

			if(skill.isOffensive() && !skill.isBox())
				startAttackStanceTask();

			if(!skill.isNotTargetAoE() || !skill.isOffensive() || targets.size() != 0)
				skill.getEffects(this, this, false, true);

			useTriggers(aimingTarget, TriggerType.ON_END_CAST, null, skill, 0);

			skill.onEndCast(this, targets);
		}
		catch(Exception e)
		{
			Creature._log.error("", e);
		}
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, double damage)
	{
		useTriggers(target, null, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, Set<Creature> targets, TriggerType type, Skill ex, Skill owner, double damage)
	{
		useTriggers(target, targets, type, ex, owner, owner, damage);
	}

	public void useTriggers(GameObject target, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
		useTriggers(target, null, type, ex, owner, triggersOwner, damage);
	}

	public void useTriggers(GameObject target, Set<Creature> targets, TriggerType type, Skill ex, Skill owner, StatTemplate triggersOwner, double damage)
	{
		Set<TriggerInfo> triggers = null;
		switch(type)
		{
			case ON_START_CAST:
			case ON_TICK_CAST:
			case ON_END_CAST:
			case ON_FINISH_CAST:
			case ON_START_EFFECT:
			case ON_EXIT_EFFECT:
			case ON_FINISH_EFFECT:
			case ON_REVIVE:
				if(triggersOwner != null)
				{
					triggers = new CopyOnWriteArraySet<TriggerInfo>();
					for(TriggerInfo t : triggersOwner.getTriggerList())
					{
						if(t.getType() == type)
							triggers.add(t);
					}
				}
				break;
			case ON_CAST_SKILL:
				if(_triggers != null && _triggers.get(type) != null)
				{
					triggers = new CopyOnWriteArraySet<>();
					for(TriggerInfo t : _triggers.get(type))
					{
						int skillID = t.getArgs() == null || t.getArgs().isEmpty() ? -1 : Integer.parseInt(t.getArgs());
						if(skillID == - 1 || skillID == owner.getId())
							triggers.add(t);
					}
				}
				break;
			default:
				if(_triggers != null)
					triggers = _triggers.get(type);
				break;
		}

		if(triggers != null && !triggers.isEmpty())
		{
			for(TriggerInfo t : triggers)
			{
				Skill skill = t.getSkill();
				if(skill != null)
				{
					if(!skill.equals(ex))
						useTriggerSkill(target == null ? getTarget() : target, targets, t, owner, damage);
				}
			}
		}
	}

	public void useTriggerSkill(GameObject target, Set<Creature> targets, TriggerInfo trigger, Skill owner, double damage)
	{
		Skill skill = trigger.getSkill();
		if(skill == null)
			return;

		/*if(skill.getTargetType() == SkillTargetType.TARGET_SELF && !skill.isTrigger())
			_log.warn("Self trigger skill dont have trigger flag. SKILL ID[" + skill.getId() + "]");*/

		Creature aimTarget = skill.getAimingTarget(this, target);
		if(aimTarget != null && trigger.isIncreasing())
		{
			int increasedTriggerLvl = 0;
			for(Abnormal effect : aimTarget.getAbnormalList())
			{
				if(effect.getSkill().getId() != skill.getId())
					continue;

				increasedTriggerLvl = effect.getSkill().getLevel(); //taking the first one only.
				break;
			}

			if(increasedTriggerLvl == 0)
			{
				Servitor servitor = aimTarget.getServitor();
				if(servitor != null) {
					for(Abnormal effect : servitor.getAbnormalList())
					{
						if(effect.getSkill().getId() != skill.getId())
							continue;

						increasedTriggerLvl = effect.getSkill().getLevel(); //taking the first one only.
						break;
					}
				}
			}

			if(increasedTriggerLvl > 0)
			{
				skill = SkillTable.getInstance().getInfo(skill.getId(), increasedTriggerLvl + 1);
				if(skill == null)
					skill = SkillTable.getInstance().getInfo(skill.getId(), increasedTriggerLvl);
			}
		}

		if(skill.getReuseDelay() > 0 && isSkillDisabled(skill))
			return;

		if(!Rnd.chance(trigger.getChance()))
			return;

		// DS: Для шансовых скиллов с TARGET_SELF и условием "пвп" сам кастер будет являться aimTarget,
		// поэтому в условиях для триггера проверяем реальную цель.
		Creature realTarget = target != null && target.isCreature() ? (Creature) target : null;
		if(trigger.checkCondition(this, realTarget, aimTarget, owner, damage) && skill.checkCondition(this, aimTarget, true, true, true, false, true))
		{
			if(targets == null)
				targets = skill.getTargets(this, aimTarget, false);

			if(!skill.isNotBroadcastable() && !isCastingNow())
			{
				if(trigger.getType() != TriggerType.IDLE)
				{
					for(Creature cha : targets)
						broadcastPacket(new MagicSkillUse(this, cha, skill.getDisplayId(), skill.getDisplayLevel(), 0, 0));
				}
			}

			callSkill(aimTarget, skill, targets, false);
			disableSkill(skill, skill.getReuseDelay());
		}
	}

	private void triggerCancelEffects(TriggerInfo trigger)
	{
		Skill skill = trigger.getSkill();
		if(skill == null)
			return;

		getAbnormalList().stop(skill, false);
	}

	public final boolean canAbortCast()
	{
		return _castInterruptTime > System.currentTimeMillis();
	}

	public boolean checkBlockedStat(final Stats stat)
	{
		return _blockedStats != null && _blockedStats.contains(stat);
	}

	public boolean checkReflectSkill(final Creature attacker, final Skill skill)
	{
		if(!skill.isReflectable() || !skill.isOffensive())
			return false;
		if(skill.isMagic() && skill.getSkillType() != Skill.SkillType.MDAM)
			return false;

		if(Rnd.chance(calcStat(skill.isMagic() ? Stats.REFLECT_MAGIC_SKILL : Stats.REFLECT_PHYSIC_SKILL, 0.0, attacker, skill)))
		{
			sendPacket(new SystemMessage(1998).addName(attacker));
			attacker.sendPacket(new SystemMessage(1999).addName(this));
			return true;
		}
		return false;
	}

	public void doCounterAttack(final Skill skill, final Creature target, final boolean blow)
	{
		if(skill == null || isDead() || target.isInvul() || skill.hasEffects() || skill.isMagic() || !skill.isOffensive() || skill.getCastRange() > 200)
			return;
		if(Rnd.chance(calcStat(Stats.COUNTER_ATTACK, 0.0, target, skill)))
		{
			final double damage = 1189 * getPAtk(target) / Math.max(target.getPDef(this), 1);
			sendPacket(new SystemMessage(1998).addName(target));
			target.sendPacket(new SystemMessage(1997).addName(this));
			if(damage >= 1.0)
			{
				if(blow)
				{
					sendPacket(new SystemMessage(35).addNumber(Integer.valueOf((int) damage)));
					target.reduceCurrentHp(damage, this, skill, 0, false, true, true, false, false, false, false, true);
				}
				sendPacket(new SystemMessage(35).addNumber(Integer.valueOf((int) damage)));
				target.reduceCurrentHp(damage, this, skill, 0, false, true, true, false, false, false, false, true);
			}
		}
	}

	public final void disableDrop(final int time)
	{
		_dropDisabled = System.currentTimeMillis() + time;
	}

	public void disableSkill(final Skill skill, final long delay)
	{
		_skillReuses.put(skill.reuseCode(), new TimeStamp(skill, delay));
	}

	public void doAttack(final Creature target)
	{
		if(target == null || isAttackingNow() || isAlikeDead() || target.isDead() || target.isFakeDeath() && !isPlayable() || !isInRange(target, 2000L))
			return;

		fireMethodInvoked("Creature.doAttack", new Object[] { this, target });

		final WeaponTemplate weaponItem = getActiveWeaponItem();
		final int sAtk = Math.max(calculateAttackDelay(), weaponItem != null && weaponItem.getAttackReuseDelay() > 0 ? Config.ATTACK_DELAY_BOW_MIN : Config.ATTACK_DELAY_MIN);
		int ssGrade = 0;
		if(weaponItem != null)
		{
			if(isPlayer() && weaponItem.getAttackReuseDelay() > 0)
			{
				final int reuse = (int) (weaponItem.getAttackReuseDelay() * getReuseModifier(target) * 666.0 * calcStat(Stats.ATK_BASE, 0.0, target, null) / 293.0 / getPAtkSpd());
				if(reuse > 0)
				{
					sendPacket(new SetupGauge(1, reuse));
					_attackReuseEndTime = reuse + System.currentTimeMillis() - 75L;
					if(reuse > sAtk)
						ThreadPoolManager.getInstance().schedule(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_READY_TO_ACT, null, null), reuse);
				}
			}
			ssGrade = weaponItem.getItemGrade().ordinal();
		}

		_attackEndTime = sAtk + System.currentTimeMillis() - Config.ATTACK_END_CORRECT;
		_isAttackAborted = false;

		setHeading(target, true);

		final Attack attack = new Attack(this, target, getChargedSoulShot(), ssGrade);
		if(weaponItem == null)
			doAttackHitSimple(attack, target, 1.0, 0, !isPlayer(), sAtk, true);
		else
		{
			switch(weaponItem.getItemType())
			{
				case BOW:
				{
					doAttackHitByBow(attack, target, sAtk);
					break;
				}
				case POLE:
				{
					doAttackHitByPole(attack, target, sAtk);
					break;
				}
				case DUAL:
				case DUALFIST:
				{
					doAttackHitByDual(attack, target, sAtk);
					break;
				}
				default:
				{
					doAttackHitSimple(attack, target, 1.0, 0, true, sAtk, true);
					break;
				}
			}
		}

		if(attack.hasHits())
		{
			if(Config.ATTACK_ANIM_MOD)
			{
				if(System.currentTimeMillis() - _lastAttackAnimPacket < Config.ATTACK_ANIM_DELAY)
					return;
				_lastAttackAnimPacket = System.currentTimeMillis();
			}
			broadcastPacket(attack);
		}

		if(isPlayer() && getPlayer().recording && Config.BOTS_WRITE_ATTACK)
		{
			if(target.isNpc())
				getPlayer().recBot(5, target.getNpcId(), target.getX(), target.getY(), 0, 0, 0);
			else
				getPlayer().recBot(1, target.getX(), target.getY(), target.getZ(), getPlayer().getPhysicalAttackRange(), 1, 0);
		}
	}

	private boolean doAttackHitSimple(final Attack attack, final Creature target, final double multiplier, final int poleHitCount, final boolean unchargeSS, final int sAtk, final boolean notify)
	{
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		if(!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(this, target, getCriticalHit(target, null));
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);
			damage1 *= (int) multiplier;
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, poleHitCount, crit1, miss1, attack._soulshot, shld1, unchargeSS, notify, sAtk), sAtk / 2);
		attack.addHit(target, damage1, miss1, crit1, shld1);
		return !miss1;
	}

	private void doAttackHitByBow(final Attack attack, final Creature target, final int sAtk)
	{
		final WeaponTemplate activeWeapon = getActiveWeaponItem();
		if(activeWeapon == null)
			return;
		int damage1 = 0;
		boolean shld1 = false;
		boolean crit1 = false;
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		reduceArrowCount();
		isMoving = false;
		if(!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(this, target, getCriticalHit(target, null));
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, false, attack._soulshot);
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, 0, crit1, miss1, attack._soulshot, shld1, true, true, sAtk), sAtk / 2);
		attack.addHit(target, damage1, miss1, crit1, shld1);
	}

	private void doAttackHitByDual(final Attack attack, final Creature target, final int sAtk)
	{
		int damage1 = 0;
		int damage2 = 0;
		boolean shld1 = false;
		boolean shld2 = false;
		boolean crit1 = false;
		boolean crit2 = false;
		final boolean miss1 = Formulas.calcHitMiss(this, target);
		final boolean miss2 = Formulas.calcHitMiss(this, target);
		if(!miss1)
		{
			shld1 = Formulas.calcShldUse(this, target);
			crit1 = Formulas.calcCrit(this, target, getCriticalHit(target, null));
			damage1 = (int) Formulas.calcPhysDam(this, target, null, shld1, crit1, true, attack._soulshot);
		}
		if(!miss2)
		{
			shld2 = Formulas.calcShldUse(this, target);
			crit2 = Formulas.calcCrit(this, target, getCriticalHit(target, null));
			damage2 = (int) Formulas.calcPhysDam(this, target, null, shld2, crit2, true, attack._soulshot);
		}
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage1, 0, crit1, miss1, attack._soulshot, shld1, true, false, sAtk / 2), sAtk / 4);
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.HitTask(this, target, damage2, 0, crit2, miss2, attack._soulshot, shld2, false, true, sAtk), sAtk / 2);
		attack.addHit(target, damage1, miss1, crit1, shld1);
		attack.addHit(target, damage2, miss2, crit2, shld2);
	}

	private void doAttackHitByPole(final Attack attack, final Creature target, final int sAtk)
	{
		final int angle = (int) calcStat(Stats.POLE_ATTACK_ANGLE, Config.POLE_BASE_ANGLE, target, null);
		final int range = (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, target, null);
		int attackcountmax = (int) Math.round(calcStat(Stats.POLE_TARGET_COUNT, Config.POLE_BASE_TC, target, null));
		if(isBoss())
			attackcountmax += 27;
		else if(isRaid())
			attackcountmax += 12;
		else if(isMonster() && getLevel() > 0)
			attackcountmax += (int) (getLevel() / 7.5);
		double mult = 1.0;
		int poleHitCount = 0;
		int poleAttackCount = 0;
		if(doAttackHitSimple(attack, target, 1.0, poleHitCount, true, sAtk, true))
			++poleHitCount;
		if(!isInZonePeace())
			for(final Creature t : getAroundCharacters(range, 200))
			{
				if(poleAttackCount >= attackcountmax)
					break;
				if(t == target || t.isDead())
					continue;
				if(!Util.isFacing(this, t, angle))
					continue;
				if(t.isPlayable())
				{
					if(!((Playable) t).isAttackable(this, false, true))
						continue;
				}
				else if(!t.isAutoAttackable(this))
					continue;
				if(doAttackHitSimple(attack, t, mult, poleHitCount, false, sAtk, false))
					++poleHitCount;
				mult *= Config.POLE_DAMAGE_MODIFIER;
				++poleAttackCount;
			}
	}

	public long getAnimationEndTime()
	{
		return _animationEndTime;
	}

	public void doCast(final Skill skill, Creature target, final boolean forceUse)
	{
		if(skill == null)
		{
			sendActionFailed();
			return;
		}

		if(!Config.ALT_TOGGLE && skill.isToggle())
		{
			altUseSkill(skill, target);
			return;
		}

		final int[] itemConsume = skill.getItemConsume();
		if(itemConsume[0] > 0)
		{
			for(int i = 0; i < itemConsume.length; ++i)
			{
				if(!consumeItem(skill.getItemConsumeId()[i], itemConsume[i]))
				{
					sendPacket(Msg.INCORRECT_ITEM_COUNT);
					sendChanges();
					return;
				}
			}
		}

		if(target == null)
			target = skill.getAimingTarget(this, getTarget());

		if(target == null)
			return;

		if(isPlayable())
		{
			final DuelEvent duelEvent = getEvent(DuelEvent.class);
			if(duelEvent != null && target.getEvent(DuelEvent.class) != duelEvent)
				duelEvent.abortDuel(getPlayer());
		}

		fireMethodInvoked("Creature.doCast", new Object[] { skill, target, forceUse });

		if(this != target)
			setHeading(target, true);

		int level = Math.max(1, getSkillDisplayLevel(skill.getId()));

		int skillTime = skill.isSkillTimePermanent() ? skill.getHitTime() : Formulas.calcMAtkSpd(this, skill, skill.getHitTime());
		int skillInterruptTime = skill.isMagic() ? Formulas.calcMAtkSpd(this, skill, skill.getSkillInterruptTime()) : 0;
		int minCastTime = Math.min(Config.SKILLS_CAST_TIME_MIN, skill.getHitTime());
		if(skillTime < minCastTime)
		{
			skillTime = minCastTime;
			skillInterruptTime = 0;
		}

		if(skill.isMagic() && !skill.isSkillTimePermanent() && getChargedSpiritShot() > 0)
		{
			skillTime = (int) (0.70 * skillTime);
			skillInterruptTime = (int) (0.70 * skillInterruptTime);
		}

		_animationEndTime = System.currentTimeMillis() + skillTime;
		Formulas.calcSkillMastery(skill, this);

		final long reuseDelay = Math.max(0L, Formulas.calcSkillReuseDelay(this, skill));
		if(!skill.isUsingWhileCasting())
			broadcastPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay));
		else
			sendPacket(new MagicSkillUse(this, target, skill.getDisplayId(), level, skillTime, reuseDelay));

		disableItem(skill, reuseDelay, reuseDelay);
		disableSkill(skill, reuseDelay);

		if(isPlayer() && !skill.isHideUseMessage())
		{
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON)
				sendPacket(new SystemMessage(SystemMessage.SUMMON_A_PET));
			else if(!skill.isHandler())
				sendPacket(new SystemMessage(SystemMessage.USE_S1).addSkillName(skill.getId(), skill.getLevel()));
			else
				sendPacket(new SystemMessage(SystemMessage.USE_S1).addItemName(Integer.valueOf(skill.getItemConsumeId()[0])));
		}
		if(skill.getTargetType() == Skill.SkillTargetType.TARGET_HOLY)
			target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, this, 1);

		final double mpConsume1 = skill.isUsingWhileCasting() ? skill.getMpConsume() : (double) skill.getMpConsume1();
		if(mpConsume1 > 0.0)
		{
			if(_currentMp < mpConsume1)
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				onCastEndTime(target, null, false);
				return;
			}
			reduceCurrentMp(mpConsume1, null);
		}
		final boolean forceBuff = skill.getSkillType() == Skill.SkillType.FORCE_BUFF;
		if(forceBuff)
		{
			if(target == this)
			{
				sendPacket(Msg.INCORRECT_TARGET);
				return;
			}
			startForceBuff(target, skill);
		}

		_flyLoc = null;

		switch(skill.getFlyType())
		{
			case DUMMY:
			case CHARGE:
			{
				final Location flyLoc = getFlyLocation(target, skill);
				if(flyLoc != null)
				{
					_flyLoc = flyLoc;
					broadcastPacket(new FlyToLocation(this, flyLoc, skill.getFlyType()));
					break;
				}
				sendPacket(Msg.CANNOT_SEE_TARGET);
				return;
			}
		}
		_castingSkill = skill;
		_castInterruptTime = System.currentTimeMillis() + skillInterruptTime + Config.CAST_INTERRUPT_TIME_ADD;

		setCastingTarget(target);

		if(skill.isUsingWhileCasting())
			callSkill(target, skill, skill.getTargets(this, target, forceUse), true);

		final boolean checks = isPlayer() && !forceBuff && !skill.isUsingWhileCasting();
		if(checks)
			sendPacket(new SetupGauge(0, skillTime));

		useTriggers(target, TriggerType.ON_START_CAST, null, skill, 0);
		if(checks && getPlayer().recording && !ArrayUtils.contains(Config.BOTS_RT_SKILLS, skill.getId()))
		{
			if(target == this)
				getPlayer().recBot(6, skill.getId(), 0, forceUse ? 1 : 0, 0, 0, 0);
			else if(target.isNpc())
				getPlayer().recBot(6, skill.getId(), target.getNpcId(), forceUse ? 1 : 0, target.getX(), target.getY(), 0);
			else
				getPlayer().recBot(1, target.getX(), target.getY(), target.getZ(), skill.getCastRange(), 1, 0);
		}
		if(!skill.isUsingWhileCasting()) {
			_skillLaunchedTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicLaunchedTask(this, forceUse), skillInterruptTime);
		}
		_skillTask = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicUseTask(this, forceUse), skillTime);

		if(Config.NEXT_CAST_CHECK && this != target && !skill.isUsingWhileCasting() && skill.getCastRange() > 0 && skill.getCastRange() != 32767 && skill.getSkillType() != Skill.SkillType.TAKECASTLE)
			_skillCheck = ThreadPoolManager.getInstance().schedule(new GameObjectTasks.MagicCheck(this, skill.getEffectRange()), (int) (skillTime * 0.66));
	}

	public void startForceBuff(final Creature target, final Skill skill)
	{
		if(_forceBuff == null)
			_forceBuff = new ForceBuff(this, target, skill);
	}

	public ForceBuff getForceBuff()
	{
		return _forceBuff;
	}

	public void setForceBuff(final ForceBuff value)
	{
		_forceBuff = value;
	}

	public Location getFlyLocation(final GameObject target, final Skill skill)
	{
		if(target != null && target != this)
		{
			final double radian = Util.convertHeadingToRadian(target.getHeading());
			Location loc;
			if(skill.isBehind())
				loc = new Location(target.getX() + (int) (Math.sin(radian) * 40.0), target.getY() - (int) (Math.cos(radian) * 40.0), target.getZ());
			else
				loc = new Location(target.getX() - (int) (Math.sin(radian) * 40.0), target.getY() + (int) (Math.cos(radian) * 40.0), target.getZ());
			if(isFlying())
			{
				if(GeoEngine.moveCheckInAir(this, loc.x, loc.y, loc.z) == null)
					return null;
			}
			else
			{
				loc.correctGeoZ(getGeoIndex());
				if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
				{
					loc = target.getLoc();
					if(!GeoEngine.canMoveToCoord(getX(), getY(), getZ(), loc.x, loc.y, loc.z, getGeoIndex()))
						return null;
				}
			}
			return loc;
		}
		final double radian2 = Util.convertHeadingToRadian(getHeading());
		int x1 = -(int) (Math.sin(radian2) * skill.getFlyRadius());
		int y1 = (int) (Math.cos(radian2) * skill.getFlyRadius());
		if(skill.isBehind())
		{
			x1 *= -1;
			y1 *= -1;
		}
		if(isFlying())
			return GeoEngine.moveCheckInAir(this, getX() + x1, getY() + y1, getZ());
		return GeoEngine.moveCheck(getX(), getY(), getZ(), getX() + x1, getY() + y1, getGeoIndex());
	}

	public void doDie(final Creature killer)
	{
		if(!isDead.compareAndSet(false, true))
			return;
		onDeath(killer);
	}

	protected void onDeath(final Creature killer)
	{
		fireMethodInvoked("Creature.doDie", new Object[] { killer });
		if(killer != null)
		{
			killer.fireMethodInvoked("Creature.doDie.KillerNotifier", new Object[] { this });
			if(isPlayer() && killer.isPlayable())
				_currentCp = 0.0;
		}
		setTarget(null);
		stopMove();
		stopAttackStanceTask();
		_currentHp = 0.0;
		if(isBlessedByNoblesse() || isSalvation())
		{
			if(isPlayer() && isSalvation() && !getPlayer().inEvent())
				getPlayer().reviveRequest(getPlayer(), 100.0, false, true);
			for(final Abnormal e : getAbnormalList().values())
				if(e.getSkill().isBlessNoblesse() || e.getSkill().getId() == 1325 || e.getSkill().getId() == 2168)
					e.exit();
		}
		else
		{
			if(Config.RESTORE_CANCEL_BUFFS > 0 && isPlayable())
				((Playable) this)._resEffs = null;
			for(final Abnormal e : getAbnormalList().values())
				if(e.getSkill().getId() != 5041)
					e.exit();
		}
		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, CtrlEvent.EVT_DEAD, killer, null));

		if(killer != null)
			killer.useTriggers(this, TriggerType.ON_KILL, null, null, 0);

		getListeners().onDeath(killer);
		if(!isPlayer())
		{
			final Object[] script_args = { this, killer };
			for(final Scripts.ScriptClassAndMethod handler : Scripts.onDie)
				Scripts.getInstance().callScripts(this, handler.className, handler.methodName, script_args);
		}
		updateEffectIcons();
		updateStats();
		broadcastStatusUpdate();
	}

	protected void onRevive()
	{
		useTriggers(this, TriggerType.ON_REVIVE, null, null, 0);
	}

	public void enableSkill(final Skill skill)
	{
		_skillReuses.remove(skill.reuseCode());
	}

	public int getAbnormalEffect()
	{
		return _abnormalEffects;
	}

	public int getAccuracy()
	{
		return (int) calcStat(Stats.ACCURACY_COMBAT, 0.0, null, null);
	}

	@Override
	public CharacterAI getAI()
	{
		if(_ai == null)
			synchronized (this)
			{
				if(_ai == null)
					_ai = initAI();
			}
		return _ai;
	}

	protected CharacterAI initAI()
	{
		return new CharacterAI(this);
	}

	public void setAI(final CharacterAI newAI)
	{
		if(newAI == null)
			return;
		final CharacterAI oldAI = _ai;
		synchronized (this)
		{
			_ai = newAI;
		}
		if(oldAI != null && oldAI.isActive())
		{
			oldAI.stopAITask();
			newAI.startAITask();
			newAI.setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
		}
	}

	public Collection<Skill> getAllSkills()
	{
		return _skills.valueCollection();
	}

	public final Skill[] getAllSkillsArray()
	{
		return _skills.values(new Skill[_skills.size()]);
	}

	public final float getAttackSpeedMultiplier()
	{
		return (float) (1.1 * getPAtkSpd() / getTemplate().basePAtkSpd);
	}

	public int getBuffLimit()
	{
		return (int) calcStat(Stats.BUFF_LIMIT, Config.ALT_BUFF_LIMIT, null, null);
	}

	public Skill getCastingSkill()
	{
		return _castingSkill;
	}

	public final Creature getCharTarget()
	{
		final GameObject target = getTarget();
		if(target == null || !target.isCreature())
			return null;
		return (Creature) target;
	}

	public byte getCON()
	{
		return (byte) calcStat(Stats.STAT_CON, getTemplate().baseCON, null, null);
	}

	public int getCriticalHit(final Creature target, final Skill skill)
	{
		return (int) calcStat(Stats.CRITICAL_BASE, getTemplate().baseCritRate, target, skill);
	}

	public double getCriticalMagic(final Creature target, final Skill skill)
	{
		double val = calcStat(Stats.MCRITICAL_RATE, 8.0, target, skill) * Config.MCRIT_MOD;
		if(Config.LIM_MCRIT != 0 && val > Config.LIM_MCRIT)
			val = Config.LIM_MCRIT;
		return val;
	}

	public final double getCurrentCp()
	{
		return _currentCp;
	}

	public final double getCurrentCpRatio()
	{
		return getCurrentCp() / getMaxCp();
	}

	public final double getCurrentCpPercents()
	{
		return getCurrentCpRatio() * 100.0;
	}

	public final boolean isCurrentCpFull()
	{
		return getCurrentCp() >= getMaxCp();
	}

	public final boolean isCurrentCpZero()
	{
		return getCurrentCp() < 1.0;
	}

	public final double getCurrentHp()
	{
		return _currentHp;
	}

	public final double getCurrentHpRatio()
	{
		return getCurrentHp() / getMaxHp();
	}

	public final double getCurrentHpPercents()
	{
		return getCurrentHpRatio() * 100.0;
	}

	public final boolean isCurrentHpFull()
	{
		return getCurrentHp() >= getMaxHp();
	}

	public final boolean isCurrentHpZero()
	{
		return getCurrentHp() < 1.0;
	}

	public final double getCurrentMp()
	{
		return _currentMp;
	}

	public final double getCurrentMpRatio()
	{
		return getCurrentMp() / getMaxMp();
	}

	public final double getCurrentMpPercents()
	{
		return getCurrentMpRatio() * 100.0;
	}

	public final boolean isCurrentMpFull()
	{
		return getCurrentMp() >= getMaxMp();
	}

	public final boolean isCurrentMpZero()
	{
		return getCurrentMp() < 1.0;
	}

	public Location getDestination()
	{
		return destination;
	}

	public byte getDEX()
	{
		return (byte) calcStat(Stats.STAT_DEX, getTemplate().baseDEX, null, null);
	}

	public int getEvasionRate(final Creature target)
	{
		return (int) calcStat(Stats.EVASION_RATE, 0.0, target, null);
	}

	@Override
	public final int getHeading()
	{
		if(Config.CONTROL_HEADING && (isAttackingNow() || isCastingNow()))
		{
			final CharacterAI ai = getAI();
			if(ai != null)
			{
				final Creature target = ai.getAttackTarget();
				if(target != null)
					setHeading(target, true);
			}
		}
		return _heading;
	}

	public byte getINT()
	{
		return (byte) calcStat(Stats.STAT_INT, getTemplate().baseINT, null, null);
	}

	public List<Creature> getAroundCharacters(final int radius, final int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundCharacters(this, radius, height);
	}

	public List<NpcInstance> getAroundNpc(final int range, final int height)
	{
		if(!isVisible())
			return Collections.emptyList();
		return World.getAroundNpc(this, range, height);
	}

	public boolean knowsObject(final GameObject obj)
	{
		return World.getAroundObjectById(this, obj.getObjectId()) != null;
	}

	public final Skill getKnownSkill(final int skillId)
	{
		if(_skills == null)
			return null;
		return _skills.get(skillId);
	}

	public final int getMagicalAttackRange(final Skill skill)
	{
		if(skill != null)
			return (int) calcStat(Stats.MAGIC_ATTACK_RANGE, skill.getCastRange(), null, skill);
		return getTemplate().baseAtkRange;
	}

	public int getMAtk(final Creature target, final Skill skill)
	{
		if(skill != null && skill.getMatak() > 0)
			return skill.getMatak();
		return (int) calcStat(Stats.MAGIC_ATTACK, getTemplate().baseMAtk, target, skill);
	}

	public int getMAtkSpd()
	{
		int val = (int) calcStat(Stats.MAGIC_ATTACK_SPEED, getTemplate().baseMAtkSpd, null, null);
		if(Config.LIM_MATK_SPD != 0 && val > Config.LIM_MATK_SPD)
			val = Config.LIM_MATK_SPD;
		return val;
	}

	public int getMaxCp()
	{
		return 0;
	}

	public int getMaxHp()
	{
		return (int) calcStat(Stats.MAX_HP, getTemplate().baseHpMax, null, null);
	}

	public int getMaxMp()
	{
		return (int) calcStat(Stats.MAX_MP, getTemplate().baseMpMax, null, null);
	}

	public int getMDef(final Creature target, final Skill skill)
	{
		return Math.max((int) calcStat(Stats.MAGIC_DEFENCE, getTemplate().baseMDef, null, skill), 1);
	}

	public byte getMEN()
	{
		return (byte) calcStat(Stats.STAT_MEN, getTemplate().baseMEN, null, null);
	}

	public float getMinDistance(final GameObject obj)
	{
		float distance = getTemplate().collisionRadius;
		if(obj != null && obj.isCreature())
			distance += ((Creature) obj).getTemplate().collisionRadius;
		return distance;
	}

	public int getWalkSpeed()
	{
		if(isInWater())
			return getSwimSpeed();
		return (int) calcStat(Stats.WALK_SPEED, getTemplate().baseWalkSpd, null, null);
	}

	public int getRunSpeed()
	{
		if(isInWater())
			return getSwimSpeed();
		return (int) calcStat(Stats.RUN_SPEED, getTemplate().baseRunSpd, null, null);
	}

	public float getMovementSpeedMultiplier()
	{
		if(isRunning())
			return getRunSpeed() * 1.0f / getTemplate().baseRunSpd;
		return getWalkSpeed() * 1.0f / getTemplate().baseWalkSpd;
	}

	@Override
	public int getMoveSpeed()
	{
		if(isRunning())
			return getRunSpeed();
		return getWalkSpeed();
	}

	public final double getMReuseRate(final Skill skill)
	{
		return calcStat(Stats.MAGIC_REUSE_RATE, getTemplate().baseMReuseRate, null, skill);
	}

	@Override
	public String getName()
	{
		return StringUtils.defaultString(_name);
	}

	public String getVisibleName(Player receiver)
	{
		return getName();
	}

	public int getPAtk(final Creature target)
	{
		return (int) calcStat(Stats.POWER_ATTACK, getTemplate().basePAtk, target, null);
	}

	public int getPAtkSpd()
	{
		return (int) calcStat(Stats.POWER_ATTACK_SPEED, getTemplate().basePAtkSpd, null, null);
	}

	public int getPDef(final Creature target)
	{
		return Math.max((int) calcStat(Stats.POWER_DEFENCE, getTemplate().basePDef, target, null), 1);
	}

	public final int getPhysicalAttackRange()
	{
		return (int) calcStat(Stats.POWER_ATTACK_RANGE, getTemplate().baseAtkRange, null, null);
	}

	public final int getRandomDamage()
	{
		final WeaponTemplate weaponItem = getActiveWeaponItem();
		if(weaponItem == null)
			return 5 + (int) Math.sqrt(getLevel());
		return weaponItem.getRandomDamage();
	}

	public double getReuseModifier(final Creature target)
	{
		return calcStat(Stats.ATK_REUSE, 1.0, target, null);
	}

	public final int getShldDef()
	{
		if(isPlayer())
			return (int) calcStat(Stats.SHIELD_DEFENCE, 0.0, null, null);
		return (int) calcStat(Stats.SHIELD_DEFENCE, getTemplate().baseShldDef, null, null);
	}

	public final int getSkillDisplayLevel(final Integer skillId)
	{
		if(_skills == null)
			return -1;
		final Skill skill = _skills.get(skillId);
		if(skill == null)
			return -1;
		return skill.getDisplayLevel();
	}

	public final int getSkillLevel(final Integer skillId)
	{
		if(_skills == null)
			return -1;
		final Skill skill = _skills.get(skillId);
		if(skill == null)
			return -1;
		return skill.getLevel();
	}

	public byte getSkillMastery(final Integer skillId)
	{
		if(_skillMastery == null)
			return 0;
		final Byte val = _skillMastery.get(skillId);
		return val == null ? 0 : (byte) val;
	}

	public void removeSkillMastery(final Integer skillId)
	{
		if(_skillMastery != null)
			_skillMastery.remove(skillId);
	}

	public final List<Skill> getSkillsByType(final Skill.SkillType type)
	{
		final List<Skill> result = new ArrayList<Skill>();
		for(Skill sk : _skills.valueCollection()) {
			if (sk.getSkillType() == type)
				result.add(sk);
		}
		return result;
	}

	public byte getSTR()
	{
		return (byte) calcStat(Stats.STAT_STR, getTemplate().baseSTR, null, null);
	}

	public int getSwimSpeed()
	{
		return (int) calcStat(Stats.RUN_SPEED, Config.SWIMING_SPEED, null, null);
	}

	public GameObject getTarget()
	{
		return _target.get();
	}

	public final int getTargetId()
	{
		final GameObject target = getTarget();
		return target == null ? -1 : target.getObjectId();
	}

	public CreatureTemplate getTemplate()
	{
		return _template;
	}

	public String getTitle()
	{
		return StringUtils.defaultString(_title);
	}

	public String getVisibleTitle(Player receiver)
	{
		return getTitle();
	}

	public byte getWIT()
	{
		return (byte) calcStat(Stats.STAT_WIT, getTemplate().baseWIT, null, null);
	}

	@Override
	public boolean hasAI()
	{
		return _ai != null;
	}

	public double headingToRadians(final int heading)
	{
		return (heading - 32768) / 10430.378350470453;
	}

	public final boolean isAlikeDead()
	{
		return _fakeDeath || isDead();
	}

	public boolean isAttackAborted()
	{
		return _isAttackAborted;
	}

	public final boolean isAttackingNow()
	{
		return _attackEndTime > System.currentTimeMillis();
	}

	public boolean isNoble()
	{
		return false;
	}

	public boolean isBlessedByNoblesse()
	{
		return false;
	}

	public boolean isSalvation()
	{
		return false;
	}

	public boolean isEffectImmune()
	{
		return _effectImmunity.get();
	}

	public final boolean isBuffImmune()
	{
		return _buffImmunity > 0;
	}

	public final boolean isDebuffImmune()
	{
		return _debuffImmunity > 0;
	}

	public final boolean isBlockBuff()
	{
		return _blockBuff > 0;
	}

	public boolean isDead()
	{
		return _currentHp < 0.5 || isDead.get();
	}

	public final boolean isDropDisabled()
	{
		return _dropDisabled > System.currentTimeMillis();
	}

	@Override
	public final boolean isFlying()
	{
		return _flying;
	}

	public final boolean isInCombat()
	{
		return System.currentTimeMillis() < _stanceEndTime;
	}

	public boolean isInvul()
	{
		return _isInvul;
	}

	public boolean isMageClass()
	{
		return getTemplate().baseMAtk > 3;
	}

	public final boolean isRiding()
	{
		return _riding;
	}

	public final boolean isRunning()
	{
		return _running;
	}

	public boolean isSkillDisabled(final Skill skill)
	{
		final TimeStamp sts = _skillReuses.get(skill.reuseCode());
		if(sts == null)
			return false;
		if(sts.hasNotPassed())
			return true;
		_skillReuses.remove(skill.reuseCode());
		return false;
	}

	public final boolean isTeleporting()
	{
		return _isTeleporting;
	}

	public Location getIntersectionPoint(final Creature target)
	{
		if(!Util.isFacing(this, target, 90))
			return new Location(target.getX(), target.getY(), target.getZ());
		final double angle = Util.convertHeadingToDegree(target.getHeading());
		final double radian = Math.toRadians(angle - 90.0);
		final double range = target.getMoveSpeed() / 2;
		return new Location((int) (target.getX() - range * Math.sin(radian)), (int) (target.getY() + range * Math.cos(radian)), target.getZ());
	}

	public Location applyOffset(final Location point, final int offset)
	{
		if(offset <= 0)
			return point;
		final long dx = point.x - getX();
		final long dy = point.y - getY();
		final long dz = point.z - getZ();
		final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(distance <= offset)
		{
			point.set(getX(), getY(), getZ());
			return point;
		}
		if(distance >= 1.0)
		{
			final double cut = offset / distance;
			point.x -= (int) (dx * cut + 0.5);
			point.y -= (int) (dy * cut + 0.5);
			point.z -= (int) (dz * cut + 0.5);
			if(!isFlying() && !isInVehicle() && !isInZone(Zone.ZoneType.water) && !isVehicle())
				point.correctGeoZ(getGeoIndex());
		}
		return point;
	}

	public List<Location> applyOffset(final List<Location> points, int offset)
	{
		offset >>= 4;
		if(offset <= 0)
			return points;
		final long dx = points.get(points.size() - 1).x - points.get(0).x;
		final long dy = points.get(points.size() - 1).y - points.get(0).y;
		final long dz = points.get(points.size() - 1).z - points.get(0).z;
		final double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if(distance <= offset)
		{
			final Location point = points.get(0);
			points.clear();
			points.add(point);
			return points;
		}
		if(distance >= 1.0)
		{
			final double cut = offset / distance;
			for(int num = (int) (points.size() * cut + 0.5), i = 1; i <= num && points.size() > 0; ++i)
				points.remove(points.size() - 1);
		}
		return points;
	}

	private boolean setSimplePath(final Location dest)
	{
		final List<Location> moveList = GeoMove.constructMoveList(getLoc(), dest);
		if(moveList.isEmpty())
			return false;
		_targetRecorder.clear();
		_targetRecorder.add(moveList);
		return true;
	}

	private boolean buildPathTo(final int x, final int y, final int z, final int offset, final boolean pathFind)
	{
		return buildPathTo(x, y, z, offset, null, false, pathFind);
	}

	private boolean buildPathTo(final int x, final int y, final int z, final int offset, final Creature follow, final boolean forestalling, final boolean pathFind)
	{
		Location dest;
		if(forestalling && follow != null && follow.isMoving)
			dest = getIntersectionPoint(follow);
		else
			dest = new Location(x, y, z);
		if(isInVehicle() || isVehicle() || !Config.ALLOW_GEODATA)
		{
			applyOffset(dest, offset);
			return setSimplePath(dest);
		}
		if(isFlying() || isInWater())
		{
			applyOffset(dest, offset);
			if(isFlying())
			{
				if(GeoEngine.canSeeCoord(this, dest.x, dest.y, dest.z, true))
					return setSimplePath(dest);
				final Location nextloc = GeoEngine.moveCheckInAir(this, dest.x, dest.y, dest.z);
				return nextloc != null && !nextloc.equals(getX(), getY(), getZ()) && setSimplePath(nextloc);
			}
			else
			{
				Location nextloc = GeoEngine.moveInWaterCheck(this, dest.x, dest.y, dest.z, getWaterZ());
				if(nextloc == null)
					return false;
				List<Location> moveList = GeoMove.constructMoveList(getLoc(), nextloc.clone());
				_targetRecorder.clear();
				if(!moveList.isEmpty())
					_targetRecorder.add(moveList);
				final int dz = dest.z - nextloc.z;
				if(dz > 0 && dz < 128)
				{
					moveList = GeoEngine.MoveList(nextloc.x, nextloc.y, nextloc.z, dest.x, dest.y, getGeoIndex(), false);
					if(moveList != null && !moveList.isEmpty())
						_targetRecorder.add(moveList);
				}
				return !_targetRecorder.isEmpty();
			}
		}
		else
		{
			List<Location> moveList2 = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, getGeoIndex(), true);
			if(moveList2 == null)
			{
				if(pathFind)
				{
					final List<List<Location>> targets = GeoMove.findMovePath(getX(), getY(), getZ(), dest.getX(), dest.getY(), dest.getZ(), this, getGeoIndex());
					if(!targets.isEmpty())
					{
						moveList2 = targets.remove(targets.size() - 1);
						applyOffset(moveList2, offset);
						if(!moveList2.isEmpty())
							targets.add(moveList2);
						if(!targets.isEmpty())
						{
							_targetRecorder.clear();
							_targetRecorder.addAll(targets);
							return true;
						}
					}
				}
				if(isPlayable())
				{
					applyOffset(dest, offset);
					moveList2 = GeoEngine.MoveList(getX(), getY(), getZ(), dest.x, dest.y, getGeoIndex(), false);
					if(moveList2 != null && !moveList2.isEmpty())
					{
						_targetRecorder.clear();
						_targetRecorder.add(moveList2);
						return true;
					}
				}
				return false;
			}
			if(moveList2.isEmpty())
				return false;
			applyOffset(moveList2, offset);
			if(moveList2.isEmpty())
				return false;
			_targetRecorder.clear();
			_targetRecorder.add(moveList2);
			return true;
		}
	}

	public Creature getFollowTarget()
	{
		return _followTarget.get();
	}

	public void setFollowTarget(final Creature target)
	{
		_followTarget = (target == null ? HardReferences.emptyRef() : target.getRef());
	}

	public boolean followToCharacter(final Creature target, final int offset, final boolean forestalling)
	{
		return followToCharacter(target.getLoc(), target, offset, forestalling);
	}

	public boolean followToCharacter(final Location loc, final Creature target, int offset, final boolean forestalling)
	{
		moveLock.lock();
		try
		{
			if(isMovementDisabled() || target == null || isInVehicle())
				return false;

			offset = Math.max(offset, 10);

			if(isFollow && target == getFollowTarget() && offset == _offset)
				return true;

			if(Math.abs(getZ() - target.getZ()) > 1000 && !isFlying())
			{
				sendPacket(Msg.CANNOT_SEE_TARGET);
				return false;
			}

			getAI().clearNextAction();

			try
			{
				if(_moveTask != null)
				{
					_moveTaskRunnable.abort = true;
					_moveTask.cancel(true);
					if(_moveList != null && !_moveList.isEmpty())
						_moveTaskRunnable.run();
					_moveTask = null;
					isMoving = false;
				}
			}
			catch(NullPointerException ex)
			{}

			deactivateGeoControl();

			if(buildPathTo(loc.x, loc.y, loc.z, offset, target, forestalling, !target.isDoor()))
			{
				movingDestTempPos.set(loc.x, loc.y, loc.z);
				isMoving = true;
				isFollow = true;
				_forestalling = forestalling;
				_offset = offset;
				setFollowTarget(target);
				moveNext(true);
				if(Config.WRITE_BOTS_AI && isPlayer() && getPlayer().recording && getPlayer().getAI().getIntention() != CtrlIntention.AI_INTENTION_CAST && getPlayer().getAI().getIntention() != CtrlIntention.AI_INTENTION_ATTACK)
					getPlayer().recBot(1, loc.x, loc.y, loc.z, offset, 1, 0);
				return true;
			}
			activateGeoControl();
			return isFollow = false;
		}
		finally
		{
			moveLock.unlock();
		}
	}

	public boolean moveToLocation(final Location loc, final int offset, final boolean pathfinding)
	{
		return moveToLocation(loc.x, loc.y, loc.z, offset, pathfinding);
	}

	public boolean moveToLocation(final int x_dest, final int y_dest, final int z_dest, int offset, final boolean pathfinding)
	{
		moveLock.lock();
		try
		{
			offset = Math.max(offset, 0);
			final Location dst_geoloc = new Location(x_dest, y_dest, z_dest).world2geo();
			if(isMoving && !isFollow && movingDestTempPos.equals(dst_geoloc))
			{
				sendActionFailed();
				return true;
			}

			if(isMovementDisabled())
			{
				getAI().setNextAction(PlayableAI.nextAction.MOVE, new Location(x_dest, y_dest, z_dest), offset, pathfinding, false);
				sendActionFailed();
				return false;
			}

			getAI().clearNextAction();

			if(isPlayer())
				getAI().changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);

			isFollow = false;

			if(_moveTask != null)
			{
				_moveTaskRunnable.abort = true;
				_moveTask.cancel(true);
				if(_moveList != null && !_moveList.isEmpty())
					_moveTaskRunnable.run();
				_moveTask = null;
				isMoving = false;
			}

			deactivateGeoControl();

			if(buildPathTo(x_dest, y_dest, z_dest, offset, pathfinding))
			{
				movingDestTempPos.set(dst_geoloc);
				moveNext(isMoving = true);
				if(Config.WRITE_BOTS_AI && isPlayer() && getPlayer().recording)
					getPlayer().recBot(1, x_dest, y_dest, z_dest, offset, pathfinding ? 1 : 0, 0);
				return true;
			}
			activateGeoControl();
			sendActionFailed();
			return false;
		}
		finally
		{
			moveLock.unlock();
		}
	}

	private void moveNext(final boolean firstMove)
	{
		if(!isMoving || isMovementDisabled())
		{
			stopMove();
			return;
		}
		_previousSpeed = getMoveSpeed();
		if(_previousSpeed <= 0)
		{
			stopMove();
			return;
		}
		if(!firstMove)
		{
			final Location dest = destination;
			if(dest != null)
				setLoc(dest, true);
		}
		if(_targetRecorder.isEmpty())
		{
			final CtrlEvent ctrlEvent = isFollow ? CtrlEvent.EVT_ARRIVED_TARGET : CtrlEvent.EVT_ARRIVED;
			stopMove(false, true);
			ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(this, ctrlEvent));
			return;
		}
		_moveList = _targetRecorder.remove(0);
		final Location begin = _moveList.get(0).clone().geo2world();
		final Location end = _moveList.get(_moveList.size() - 1).clone().geo2world();
		final boolean finish = false;
		destination = end;
		final double distance = isFlying() || isInWater() ? begin.distance3D(end) : begin.distance(end);
		if(distance != 0.0)
			setHeading(Util.calculateHeadingFrom(getX(), getY(), destination.x, destination.y));
		broadcastMove();
		final long currentTimeMillis = System.currentTimeMillis();
		_followTimestamp = currentTimeMillis;
		_startMoveTime = currentTimeMillis;
		if(_moveTaskRunnable == null)
			_moveTaskRunnable = new MoveNextTask();
		_moveTask = ThreadPoolManager.getInstance().schedule(_moveTaskRunnable.setDist(distance, finish), getMoveTickInterval());
	}

	public int getMoveTickInterval()
	{
		return 16000 / Math.max(getMoveSpeed(), 1);
	}

	protected void broadcastMove()
	{
		broadcastPacket(movePacket());
	}

	public void broadcastStopMove()
	{
		broadcastPacket(stopMovePacket());
	}

	public void stopMove()
	{
		stopMove(true, true);
	}

	public void stopMove(final boolean validate)
	{
		stopMove(true, validate);
	}

	public void stopMove(final boolean stop, final boolean validate)
	{
		if(!isMoving)
			return;
		moveLock.lock();
		try
		{
			if(!isMoving)
				return;
			isMoving = false;
			isFollow = false;
			if(_moveTask != null)
			{
				_moveTask.cancel(false);
				_moveTask = null;
			}
			destination = null;
			_moveList = null;
			_targetRecorder.clear();
			if(validate)
				validateLocation(isPlayer() ? 2 : 1);
			if(stop)
				broadcastStopMove();

			activateGeoControl();
		}
		finally
		{
			moveLock.unlock();
		}
	}

	/** Возвращает координаты поверхности воды, если мы находимся в ней, или над ней. */
	public int[] getWaterZ()
	{
		int[] waterZ = new int[]{ Integer.MIN_VALUE, Integer.MAX_VALUE };
		if(!isInWater())
			return waterZ;

		final int z = GeoEngine.getLowerHeight(getLoc(), getGeoIndex()) + 1;
		int water_z = Integer.MIN_VALUE;
		final List<Territory> terrlist = World.getTerritories(getX(), getY(), z);
		if(terrlist != null)
		{
			for(final Territory terr : terrlist)
			{
				if(terr != null && terr.getZone() != null && terr.getZone().getType() == Zone.ZoneType.water)
				{
					if(waterZ[0] == Integer.MIN_VALUE || waterZ[0] > terr.getZmin())
						waterZ[0] = terr.getZmin();
					if(waterZ[1] == Integer.MAX_VALUE || waterZ[1] < terr.getZmax())
						waterZ[1] = terr.getZmax();
				}
			}
		}
		return waterZ;
	}

	protected L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}

	public L2GameServerPacket movePacket()
	{
		return new CharMoveToLocation(this);
	}

	@Override
	public boolean isInWater()
	{
		return isInZone(Zone.ZoneType.water) && !isInVehicle() && !isVehicle() && !isFlying();
	}

	protected boolean needHpUpdate()
	{
		final int bar = (int) (getCurrentHp() * 352.0 / getMaxHp());
		if(bar == 0 || bar != _lastHpBarUpdate)
		{
			_lastHpBarUpdate = bar;
			return true;
		}
		return false;
	}

	protected boolean needMpUpdate()
	{
		final int bar = (int) (getCurrentMp() * 352.0 / getMaxMp());
		if(bar == 0 || bar != _lastMpBarUpdate)
		{
			_lastMpBarUpdate = bar;
			return true;
		}
		return false;
	}

	@Override
	protected void onDespawn()
	{
		setTarget(null);
		stopMove();
		stopAttackStanceTask();
		stopRegeneration();
		clearStatusListeners();
		super.onDespawn();
	}

	public void onDecay()
	{
		decayMe();
		fireMethodInvoked("Creature.onDecay", null);
	}

	@Override
	public void onForcedAttack(final Player player, final boolean shift)
	{
		if(!isAttackable(player) || player.isConfused() || player.isBlocked())
		{
			player.sendActionFailed();
			return;
		}
		player.getAI().Attack(this, true, shift);
	}

	public void onHitTimer(final Creature target, final int damage, final int poleHitCount, final boolean crit, final boolean miss, final boolean soulshot, final boolean shld, final boolean unchargeSS)
	{
		if(isAlikeDead())
		{
			sendActionFailed();
			return;
		}

		if(target.isDead() || !isInRange(target, 2000L))
		{
			sendActionFailed();
			return;
		}

		fireMethodInvoked("Creature.onHitTimer", new Object[] { this, target, damage, crit, miss, soulshot, shld, unchargeSS });

		if(!miss && target.isPlayer() && (isCursedWeaponEquipped() || getActiveWeaponInstance() != null && getActiveWeaponInstance().isHeroWeapon() && target.isCursedWeaponEquipped()))
			target.setCurrentCp(0.0);

		displayGiveDamageMessage(target, crit, miss, false);

		if(shld && target.isPlayer()) {
			if (damage > 1)
				target.sendPacket(Msg.SHIELD_DEFENSE_HAS_SUCCEEDED);
			else if (damage == 1)
				target.sendPacket(Msg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
		}

		ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(target, CtrlEvent.EVT_ATTACKED, this, null, damage));

		final boolean checkPvP = checkPvP(target, null);
		if(!miss && damage > 0)
		{
			if(target.getForceBuff() != null)
				target.abortCast(true, false);
			target.reduceCurrentHp(damage, this, null, poleHitCount, crit, true, true, false, true, false, false, true);
			if(!target.isDead())
			{
				if(crit)
					useTriggers(target, TriggerType.CRIT, null, null, damage);

				useTriggers(target, TriggerType.ATTACK, null, null, damage);

				if(getTarget() != null && isPlayer() && Rnd.chance(target.calcStat(Stats.CANCEL_TARGET, 0.0, this, null)) && isInRange(target.getLoc(), 900L))
				{
					target.broadcastPacket(new MagicSkillUse(target, this, 5144, 1, 0, 0L));
					setTarget(null);
					abortAttack(true, true);
					getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				}

				if(Formulas.calcCastBreak(target, crit))
					target.abortCast(false, true);
			}
			if(soulshot && unchargeSS)
				unChargeShots(false);
		}

		if(miss)
			target.useTriggers(this, TriggerType.UNDER_MISSED_ATTACK, null, null, damage);

		startAttackStanceTask();

		if(checkPvP)
			startPvPFlag(target);
	}

	public void onMagicUseTimer(final Creature aimingTarget, final Skill skill, boolean forceUse)
	{
		_castInterruptTime = 0L;

		if(_forceBuff != null)
		{
			_forceBuff.delete();
			return;
		}

		if(skill.isUsingWhileCasting())
		{
			aimingTarget.getAbnormalList().stop(skill.getId());
			onCastEndTime(aimingTarget, null, false);
			return;
		}

		if(!skill.isOffensive() && getAggressionTarget() != null)
			forceUse = true;

		if(Config.CAST_CHECK && !skill.checkCondition(this, aimingTarget, forceUse, false, false))
		{
			if(skill.getSkillType() == Skill.SkillType.PET_SUMMON && isPlayer())
				getPlayer().setPetControlItem(null);
			onCastEndTime(aimingTarget, null, false);
			return;
		}

		final int hpConsume = skill.getHpConsume();
		if(hpConsume > 0)
			setCurrentHp(Math.max(0.0, _currentHp - hpConsume), false);

		double mpConsume2 = skill.getMpConsume2();
		if(mpConsume2 > 0.0)
		{
			if(skill.isMusic())
			{
				final double inc = mpConsume2 / 2.0;
				double add = 0.0;
				for(final Abnormal e : getAbnormalList().values())
					if(e.getSkill().getId() != skill.getId() && e.getSkill().isMusic() && e.getTimeLeft() > 30000L)
						add += inc;
				mpConsume2 += add;
				mpConsume2 = calcStat(Stats.MP_DANCE_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			}
			else if(skill.isMagic())
				mpConsume2 = calcStat(Stats.MP_MAGIC_SKILL_CONSUME, mpConsume2, aimingTarget, skill);
			else
				mpConsume2 = calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, mpConsume2, aimingTarget, skill);

			if(_currentMp < mpConsume2 && isPlayable())
			{
				sendPacket(Msg.NOT_ENOUGH_MP);
				onCastEndTime(aimingTarget, null, false);
				return;
			}

			reduceCurrentMp(mpConsume2, null);
		}

		final Set<Creature> targets = skill.getTargets(this, aimingTarget, forceUse);

		callSkill(aimingTarget, skill, targets, true);

		switch(skill.getFlyType())
		{
			case THROW_UP:
			case THROW_HORIZONTAL:
			{
				for(final Creature target : targets)
				{
					final Location flyLoc = getFlyLocation(null, skill);
					broadcastPacket(new FlyToLocation(target, flyLoc, skill.getFlyType()));
					target.setLoc(flyLoc);
				}
				break;
			}
		}

		if(isPlayer() && getTarget() != null && skill.isOffensive()) {
			for (final Creature target2 : targets) {
				if (Rnd.chance(target2.calcStat(Stats.CANCEL_TARGET, 0.0, aimingTarget, skill)) && isInRange(target2.getLoc(), 900L)) {
					target2.broadcastPacket(new MagicSkillUse(target2, this, 5144, 1, 0, 0L));
					clearCastVars();
					getAI().notifyEvent(CtrlEvent.EVT_FORGET_OBJECT, target2);
					return;
				}
			}
		}

		final int skCoolTime = Formulas.calcMAtkSpd(this, skill, skill.getCoolTime());
		ThreadPoolManager.getInstance().schedule(new GameObjectTasks.CastEndTimeTask(this, aimingTarget, targets), Math.max(skCoolTime, 20));
	}

	public void onCastEndTime(Creature aimingTarget, Set<Creature> targets, boolean success)
	{
		final Skill castingSkill = getCastingSkill();
		final Creature castingTarget = getCastingTarget();

		finishFly();
		clearCastVars();

		if(castingSkill != null) {
			getAI().notifyEvent(CtrlEvent.EVT_FINISH_CASTING, castingSkill, aimingTarget, success);

			if (success) {
				castingSkill.onFinishCast(aimingTarget, this, targets);
				useTriggers(castingTarget, TriggerType.ON_FINISH_CAST, null, castingSkill, 0);
			}
		}
	}

	public void clearCastVars()
	{
		_animationEndTime = 0L;
		_castInterruptTime = 0L;
		_skillCheck = null;
		_castingSkill = null;
		_skillTask = null;
		_skillLaunchedTask = null;
		_flyLoc = null;
	}

	private void finishFly()
	{
		final Location flyLoc = _flyLoc;
		_flyLoc = null;
		if(flyLoc != null)
		{
			setLoc(flyLoc);
			validateLocation(1);
		}
	}

	public void sendDamageMessage(final Creature attacker, final int i, final int ii)
	{
		if(attacker == this)
			return;
		final Player player = attacker.getPlayer();
		if(player == null)
			return;
		if(ii > 0)
		{
			if(attacker.isPlayer())
				player.sendPacket(new SystemMessage(1130).addNumber(Integer.valueOf(i)).addNumber(Integer.valueOf(ii)));
			else if(attacker.isSummon())
				player.sendMessage("The summoned monster gave damage of " + i + " to target and " + ii + " damage to the servitor.");
			else
				player.sendMessage("Your pet hit for " + i + " damage to target and " + ii + " damage to the servitor.");
		}
		else if(attacker.isPlayer())
			player.sendPacket(new SystemMessage(35).addNumber(Integer.valueOf(i)));
		else if(attacker.isSummon())
			player.sendPacket(new SystemMessage(1026).addNumber(Integer.valueOf(i)));
		else
			player.sendPacket(new SystemMessage(1015).addNumber(Integer.valueOf(i)));
	}

	public void reduceCurrentHp(double damage, final Creature attacker, final Skill skill, final int poleHitCount, final boolean crit, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage)
	{
		if(attacker == null || isDead() || attacker.isDead() && !isDot)
			return;
		if(isInvul() && transferDamage)
			return;
		if(isInvul() && attacker != this)
		{
			if(sendMessage)
				if(isDmg())
					attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
				else
					sendDamageMessage(attacker, (int) Math.max(damage, 1.0), 0);
			return;
		}
		int ii = 0;
		if(isPlayer() && !isDot)
		{
			double trans = calcStat(Stats.TRANSFER_DAMAGE_PERCENT, 0.0);
			if(trans >= 1.0)
			{
				trans *= damage * 0.01;
				final Servitor summon = getServitor();
				if(summon == null || summon.isDead() || summon.isPet() || summon.getCurrentHp() < trans)
					getAbnormalList().stop(1262);
				else if(summon.isInRange(this, 1200L))
				{
					damage -= trans;
					summon.reduceCurrentHp(trans, summon, null, 0, false, false, false, false, false, true, false, false);
					ii = (int) trans;
				}
			}
		}

		if(canReflect)
			attacker.absorbAndReflect(this, skill, damage, poleHitCount);

		getListeners().onCurrentHpDamage(damage, attacker, skill);

		if(attacker != this)
		{
			if(sendMessage) {
				sendDamageMessage(attacker, (int) Math.max(damage, 1.0), ii);
				displayReceiveDamageMessage(attacker, (int) Math.max(damage, 1.0));
			}

			if(!isDot)
				useTriggers(attacker, TriggerType.RECEIVE_DAMAGE, null, null, damage);
		}

		onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
	}

	protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp)
	{
		if(awake && isSleeping())
			getAbnormalList().stop(EffectType.Sleep);

		if(attacker != this || skill != null && skill.isOffensive())
		{
			if(isMeditated())
				getAbnormalList().stop(EffectType.Meditation);
			startAttackStanceTask();
		}

		setCurrentHp(Math.max(getCurrentHp() - damage, 0.0), false);

		if(getCurrentHp() < 0.5)
		{
			if(attacker != this || (skill != null && skill.isOffensive()))
				useTriggers(attacker, TriggerType.DIE, null, null, damage);

			doDie(attacker);
		}
	}

	public void displayGiveDamageMessage(final Creature target, final boolean crit, final boolean miss, final boolean magic)
	{
		if(miss && target.isPlayer() && !target.isInvul())
			target.sendPacket(new SystemMessage(42).addName(this));
	}

	public void displayReceiveDamageMessage(final Creature attacker, final int damage)
	{}

	public Collection<TimeStamp> getSkillReuses()
	{
		return _skillReuses.valueCollection();
	}

	public void reduceCurrentMp(double i, final Creature attacker)
	{
		if(attacker != null && attacker != this)
		{
			if(isSleeping())
				getAbnormalList().stop(EffectType.Sleep);
			if(isMeditated())
				getAbnormalList().stop(EffectType.Meditation);
		}
		if(isInvul() && attacker != null && attacker != this)
		{
			if(isDmg())
				attacker.sendPacket(Msg.THE_ATTACK_HAS_BEEN_BLOCKED);
			return;
		}
		if(attacker != null && attacker.isPlayer() && Math.abs(attacker.getLevel() - getLevel()) > 10)
		{
			if(attacker.getKarma() > 0 && getAbnormalList().getEffectsBySkillId(5182) != null && !isInZone(Zone.ZoneType.Siege))
				return;
			if(getKarma() > 0 && attacker.getAbnormalList().getEffectsBySkillId(5182) != null && !attacker.isInZone(Zone.ZoneType.Siege))
				return;
		}
		i = _currentMp - i;
		if(i < 0.0)
			i = 0.0;
		setCurrentMp(i);
		if(attacker != null && attacker != this)
			startAttackStanceTask();
	}

	public void removeAllSkills()
	{
		for(final Skill s : getAllSkillsArray())
			removeSkill(s);
	}

	public void removeBlockStats(final List<Stats> stats)
	{
		if(_blockedStats != null)
		{
			_blockedStats.removeAll(stats);
			if(_blockedStats.isEmpty())
				_blockedStats = null;
		}
	}

	public Skill removeSkill(final Skill skill)
	{
		if(skill == null)
			return null;
		return removeSkillById(skill.getId());
	}

	public Skill removeSkillById(final Integer id)
	{
		final Skill oldSkill = _skills.remove(id);
		if(oldSkill == null)
			return null;

		if(isPlayer() && oldSkill.getReuseGroupId() > 0 && getPlayer()._gskills.containsKey(oldSkill.getReuseGroupId()))
			getPlayer()._gskills.get(oldSkill.getReuseGroupId()).remove(oldSkill);
		removeStatsOwner(oldSkill);
		removeTriggers(oldSkill);
		if(Config.DEL_AUGMENT_BUFFS && oldSkill.isItemSkill())
		{
			List<Abnormal> effects = getAbnormalList().getEffectsBySkill(oldSkill);
			if(effects != null) {
				for (final Abnormal effect : effects)
					effect.exit();
			}

			Servitor pet = getServitor();
			if(pet != null)
			{
				effects = pet.getAbnormalList().getEffectsBySkill(oldSkill);
				if(effects != null)
					for(final Abnormal effect2 : effects)
						effect2.exit();
			}
		}
		return oldSkill;
	}

	public void addTriggers(StatTemplate f)
	{
		if(f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
		{
			addTrigger(t);
		}
	}

	public void addTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			_triggers = new ConcurrentHashMap<TriggerType, Set<TriggerInfo>>();

		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
		{
			hs = new CopyOnWriteArraySet<TriggerInfo>();
			_triggers.put(t.getType(), hs);
		}

		hs.add(t);

		if(t.getType() == TriggerType.ADD)
			useTriggerSkill(this, null, t, null, 0);
		else if(t.getType() == TriggerType.IDLE)
			new RunnableTrigger(this, t).schedule();
	}

	public Map<TriggerType, Set<TriggerInfo>> getTriggers()
	{
		return _triggers;
	}

	public void removeTriggers(StatTemplate f)
	{
		if(_triggers == null || f.getTriggerList().isEmpty())
			return;

		for(TriggerInfo t : f.getTriggerList())
			removeTrigger(t);
	}

	public void removeTrigger(TriggerInfo t)
	{
		if(_triggers == null)
			return;
		Set<TriggerInfo> hs = _triggers.get(t.getType());
		if(hs == null)
			return;
		hs.remove(t);

		if(t.cancelEffectsOnRemove())
			triggerCancelEffects(t);
	}

	public void broadcastToStatusListeners(final L2GameServerPacket... packets)
	{
		if(!isVisible() || packets.length == 0)
			return;
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null || _statusListeners.isEmpty())
				return;
			for(int i = 0; i < _statusListeners.size(); ++i)
			{
				final Player player = _statusListeners.get(i);
				player.sendPacket(packets);
			}
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void addStatusListener(final Player cha)
	{
		if(cha == this)
			return;
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				_statusListeners = new LazyArrayList<Player>();
			if(!_statusListeners.contains(cha))
				_statusListeners.add(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void removeStatusListener(final Creature cha)
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				return;
			_statusListeners.remove(cha);
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public void clearStatusListeners()
	{
		statusListenersLock.lock();
		try
		{
			if(_statusListeners == null)
				return;
			_statusListeners.clear();
		}
		finally
		{
			statusListenersLock.unlock();
		}
	}

	public StatusUpdate makeStatusUpdate(final int... fields)
	{
		final StatusUpdate su = new StatusUpdate(getObjectId());
		for(final int field : fields)
			switch(field)
			{
				case 27:
				{
					su.addAttribute(field, getKarma());
					break;
				}
				case 26:
				{
					su.addAttribute(field, getPvpFlag());
					break;
				}
			}
		return su;
	}

	public void sendActionFailed()
	{
		sendPacket(Msg.ActionFail);
	}

	public final void setCurrentHp(double newHp, final boolean canResurrect, final boolean sendInfo)
	{
		int maxHp = getMaxHp();

		newHp = Math.min(maxHp, Math.max(0.0, newHp));

		if(_currentHp == newHp)
			return;

		if(newHp >= 0.5 && isDead() && !canResurrect)
			return;

		final double hpStart = _currentHp;

		_currentHp = newHp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, newHp);

		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentHp < maxHp)
			startRegeneration();

		getListeners().onChangeCurrentHp(hpStart, newHp);
	}

	public final void setCurrentHp(final double newHp, final boolean canResurrect)
	{
		setCurrentHp(newHp, canResurrect, true);
	}

	public final void setCurrentMp(double newMp, final boolean sendInfo)
	{
		int maxMp = getMaxMp();
		newMp = Math.min(maxMp, Math.max(0.0, newMp));

		if(_currentMp == newMp)
			return;

		if(newMp >= 0.5 && isDead())
			return;

		double mpStart = _currentMp;

		_currentMp = newMp;

		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentMp < maxMp)
			startRegeneration();

		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public final void setCurrentMp(final double newMp)
	{
		setCurrentMp(newMp, true);
	}

	public final void setCurrentCp(double newCp, final boolean sendInfo)
	{
		if(!isPlayer())
			return;

		int maxCp = getMaxCp();
		newCp = Math.min(maxCp, Math.max(0.0, newCp));

		if(_currentCp == newCp)
			return;

		if(newCp >= 0.5 && isDead())
			return;

		double cpStart = _currentCp;

		_currentCp = newCp;

		if(sendInfo)
		{
			broadcastStatusUpdate();
			sendChanges();
		}

		if(_currentCp < maxCp)
			startRegeneration();

		getListeners().onChangeCurrentCp(cpStart, newCp);
	}

	public final void setCurrentCp(final double newCp)
	{
		setCurrentCp(newCp, true);
	}

	public void setCurrentHpMp(double newHp, double newMp, final boolean canResurrect)
	{
		int maxHp = getMaxHp();
		int maxMp = getMaxMp();

		newHp = Math.min(maxHp, Math.max(0.0, newHp));
		newMp = Math.min(maxMp, Math.max(0.0, newMp));

		if(_currentHp == newHp && _currentMp == newMp)
			return;

		if(newHp >= 0.5 && isDead() && !canResurrect)
			return;

		double hpStart = _currentHp;
		double mpStart = _currentMp;

		_currentHp = newHp;
		_currentMp = newMp;

		if(isDead.compareAndSet(true, false))
			onRevive();

		checkHpMessages(hpStart, newHp);

		broadcastStatusUpdate();
		sendChanges();

		if(_currentHp < maxHp || _currentMp < maxMp)
			startRegeneration();

		getListeners().onChangeCurrentHp(hpStart, newHp);
		getListeners().onChangeCurrentMp(mpStart, newMp);
	}

	public final void setFlying(final boolean mode)
	{
		_flying = mode;
	}

	@Override
	public void setHeading(int heading)
	{
		if(heading < 0)
			heading = heading + 1 + Integer.MAX_VALUE & 0xFFFF;
		else if(heading > 65535)
			heading &= 0xFFFF;
		_heading = heading;
	}

	public final void setHeading(final Creature target, final boolean toChar)
	{
		if(target == null || target == this)
			return;
		setHeading(new Location(target.getX(), target.getY(), target.getZ()), toChar);
	}

	public final void setHeading(final Location target, final boolean toChar)
	{
		setHeading((int) (Math.atan2(getY() - target.y, getX() - target.x) * 10430.378350470453) + (toChar ? 32768 : 0));
	}

	public boolean startEffectImmunity()
	{
		return _effectImmunity.getAndSet(true);
	}

	public boolean stopEffectImmunity()
	{
		return _effectImmunity.setAndGet(false);
	}

	public final void setBuffImmunity(final boolean value)
	{
		if(value)
			++_buffImmunity;
		else
			--_buffImmunity;
	}

	public final void setDebuffImmunity(final boolean value)
	{
		if(value)
			++_debuffImmunity;
		else
			--_debuffImmunity;
	}

	public final void setBlockBuff(final boolean value)
	{
		if(value)
			++_blockBuff;
		else
			--_blockBuff;
	}

	public void setIsInvul(final boolean b)
	{
		if(!b)
			getAbnormalList().stop(EffectType.Invulnerable);
		_isInvul = b;
	}

	public boolean isPendingRevive()
	{
		return false;
	}

	public final void setIsTeleporting(final boolean value)
	{
		_isTeleporting = value;
	}

	public final void setName(final String name)
	{
		_name = name;
	}

	public Creature getCastingTarget()
	{
		return _castingTarget.get();
	}

	public void setCastingTarget(final Creature target)
	{
		if(target == null)
			_castingTarget = HardReferences.emptyRef();
		else
			_castingTarget = target.getRef();
	}

	public final void setRiding(final boolean mode)
	{
		_riding = mode;
	}

	public final void setRunning()
	{
		if(!_running)
		{
			_running = true;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	public void setSkillMastery(final Integer skill, final byte mastery)
	{
		if(_skillMastery == null)
			_skillMastery = new HashMap<Integer, Byte>();
		_skillMastery.put(skill, mastery);
	}

	public void setTarget(GameObject object)
	{
		if(object != null && !object.isVisible())
			object = null;
		if(object == null)
		{
			if(getTarget() != null)
				broadcastPacket(new TargetUnselected(this));
			_target = HardReferences.emptyRef();
		}
		else
			_target = object.getRef();
	}

	public void setTitle(final String title)
	{
		_title = title;
	}

	public void setAggressionTarget(final Creature target)
	{
		if(target == null)
			_aggressionTarget = HardReferences.emptyRef();
		else
			_aggressionTarget = target.getRef();
	}

	public Creature getAggressionTarget()
	{
		return _aggressionTarget.get();
	}

	public void setWalking()
	{
		if(_running)
		{
			_running = false;
			broadcastPacket(new ChangeMoveType(this));
		}
	}

	public void startAbnormalEffect(final AbnormalEffect ae)
	{
		if(ae == AbnormalEffect.NULL)
			_abnormalEffects = AbnormalEffect.NULL.getMask();
		else
			_abnormalEffects |= ae.getMask();
		sendChanges();
	}

	public void startAttackStanceTask()
	{
		startAttackStanceTask0();
	}

	protected void startAttackStanceTask0()
	{
		if(isInCombat())
		{
			_stanceEndTime = System.currentTimeMillis() + 15000L;
			return;
		}
		_stanceEndTime = System.currentTimeMillis() + 15000L;
		broadcastPacket(new AutoAttackStart(getObjectId()));
		final Future<?> task = _stanceTask;
		if(task != null)
			task.cancel(false);
		_stanceTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(_stanceTaskRunnable == null ? (_stanceTaskRunnable = new AttackStanceTask()) : _stanceTaskRunnable, 1000L, 1000L);
	}

	public void stopAttackStanceTask()
	{
		_stanceEndTime = 0L;
		final Future<?> task = _stanceTask;
		if(task != null)
		{
			task.cancel(false);
			_stanceTask = null;
			broadcastPacket(new AutoAttackStop(getObjectId()));
		}
	}

	protected void stopRegeneration()
	{
		regenLock.lock();
		try
		{
			if(_isRegenerating)
			{
				_isRegenerating = false;
				if(_regenTask != null)
				{
					_regenTask.cancel(false);
					_regenTask = null;
				}
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	protected void startRegeneration()
	{
		if(!isVisible() || isDead() || getRegenTick() == 0L)
			return;
		if(_isRegenerating)
			return;
		regenLock.lock();
		try
		{
			if(!_isRegenerating)
			{
				_isRegenerating = true;
				_regenTask = RegenTaskManager.getInstance().scheduleAtFixedRate(_regenTaskRunnable == null ? (_regenTaskRunnable = new RegenTask()) : _regenTaskRunnable, 0L, getRegenTick());
			}
		}
		finally
		{
			regenLock.unlock();
		}
	}

	public long getRegenTick()
	{
		return 3330L;
	}

	public void stopAbnormalEffect(final AbnormalEffect ae)
	{
		_abnormalEffects &= ~ae.getMask();
		sendChanges();
	}

	public void block()
	{
		_blocked = true;
	}

	public void unblock()
	{
		_blocked = false;
	}

	public void startConfused()
	{
		if(!_confused)
		{
			_confused = true;
			startAttackStanceTask();
			sendChanges();
		}
	}

	public void stopConfused()
	{
		if(_confused)
		{
			_confused = false;
			sendChanges();
			abortAttack(true, true);
			abortCast(true, false);
			stopMove();
			getAI().setAttackTarget(null);
		}
	}

	public void startFear()
	{
		if(!_afraid)
		{
			abortAttack(_afraid = true, true);
			abortCast(true, false);
			sendActionFailed();
			stopMove();
			startAttackStanceTask();
			sendChanges();
		}
	}

	public void stopFear()
	{
		if(_afraid)
		{
			_afraid = false;
			sendChanges();
		}
	}

	public void startMuted()
	{
		if(!_muted)
		{
			_muted = true;
			if(getCastingSkill() != null && getCastingSkill().isMagic())
				abortCast(true, false);
			startAttackStanceTask();
			sendChanges();
		}
	}

	public void stopMuted()
	{
		if(_muted)
		{
			_muted = false;
			sendChanges();
		}
	}

	public void startPMuted()
	{
		if(!_pmuted)
		{
			_pmuted = true;
			if(getCastingSkill() != null && !getCastingSkill().isMagic())
				abortCast(true, false);
			startAttackStanceTask();
			sendChanges();
		}
	}

	public void stopPMuted()
	{
		if(_pmuted)
		{
			_pmuted = false;
			sendChanges();
		}
	}

	public void startRooted()
	{
		if(!_rooted)
		{
			_rooted = true;
			stopMove();
			startAttackStanceTask();
			sendChanges();
		}
	}

	public void stopRooting()
	{
		if(_rooted)
		{
			_rooted = false;
			sendChanges();
		}
	}

	public void startSleeping()
	{
		if(!_sleeping)
		{
			int targetObjectId = isPlayable() && isAttackingNow() ? getAI().getAttackTargetObjectId() : 0;
			abortAttack(_sleeping = true, false);
			abortCast(true, false);
			sendActionFailed();
			stopMove();
			startAttackStanceTask();
			sendChanges();

			if(targetObjectId > 0)
				getAI().setNextAction(PlayableAI.nextAction.ATTACK, GameObjectsStorage.findObject(targetObjectId), null, false, false);
		}
	}

	public void stopSleeping()
	{
		if(_sleeping)
		{
			_sleeping = false;
			sendChanges();
			if(getAI() instanceof PlayableAI)
				((PlayableAI) getAI()).setNextIntention();
		}
	}

	public void startStunning()
	{
		if(!_stunned)
		{
			int targetObjectId = isPlayable() && isAttackingNow() ? getAI().getAttackTargetObjectId() : 0;
			abortAttack(_stunned = true, false);
			abortCast(true, false);
			sendActionFailed();
			stopMove();
			startAttackStanceTask();
			sendChanges();

			if(targetObjectId > 0L)
				getAI().setNextAction(PlayableAI.nextAction.ATTACK, GameObjectsStorage.findObject(targetObjectId), null, false, false);
		}
	}

	public void stopStunning()
	{
		if(_stunned)
		{
			_stunned = false;
			sendChanges();
			if(getAI() instanceof PlayableAI)
				((PlayableAI) getAI()).setNextIntention();
		}
	}

	public void setFakeDeath(final boolean value)
	{
		_fakeDeath = value;
	}

	public void breakFakeDeath()
	{
		getAbnormalList().stopAll(EffectType.FakeDeath);
	}

	public void setMeditated(final boolean meditated)
	{
		_meditated = meditated;
	}

	public boolean startParalyzed()
	{
		final boolean para = !isParalyzed();
		final boolean result = _paralyzed.getAndSet(true);
		if(para && isParalyzed())
		{
			int targetObjectId = isPlayable() && isAttackingNow() ? getAI().getAttackTargetObjectId() : 0;
			abortAttack(true, false);
			abortCast(true, false);
			sendActionFailed();
			stopMove();

			if(targetObjectId > 0L)
				getAI().setNextAction(PlayableAI.nextAction.ATTACK, GameObjectsStorage.findObject(targetObjectId), null, false, false);
			sendChanges();
		}
		return result;
	}

	public boolean stopParalyzed()
	{
		boolean result = false;
		if(isParalyzed())
		{
			result = _paralyzed.setAndGet(false);
			if(!isParalyzed())
			{
				sendChanges();
				if(getAI() instanceof PlayableAI)
					((PlayableAI) getAI()).setNextIntention();
			}
		}
		return result;
	}

	public void setImmobilized(final boolean immobilized)
	{
		if(_immobilized != immobilized)
		{
			_immobilized = immobilized;
			if(immobilized)
				stopMove();
			sendChanges();
		}
	}

	public boolean isConfused()
	{
		return _confused;
	}

	public boolean isFakeDeath()
	{
		return _fakeDeath;
	}

	public boolean isAfraid()
	{
		return _afraid;
	}

	public boolean isBlocked()
	{
		return _blocked;
	}

	public boolean isMuted(final Skill skill)
	{
		return skill != null && !skill.isNotAffectedByMute() && (_muted && skill.isMagic() || _pmuted && !skill.isMagic());
	}

	public boolean isMMuted()
	{
		return _muted;
	}

	public boolean isPMuted()
	{
		return _pmuted;
	}

	public boolean isRooted()
	{
		return _rooted;
	}

	public boolean isSleeping()
	{
		return _sleeping;
	}

	public boolean isStunned()
	{
		return _stunned;
	}

	public boolean isMeditated()
	{
		return _meditated;
	}

	public boolean isParalyzed()
	{
		return _paralyzed.get();
	}

	public boolean isImmobilized()
	{
		return _immobilized;
	}

	public boolean isCastingNow()
	{
		return _skillTask != null;
	}

	public boolean isMovementDisabled()
	{
		return isBlocked() || isStunned() || isRooted() || isSleeping() || isParalyzed() || isImmobilized() || isAlikeDead() || isAttackingNow() || isCastingNow();
	}

	public boolean isActionsDisabled()
	{
		return isBlocked() || isStunned() || isSleeping() || isParalyzed() || isAttackingNow() || isCastingNow() || isAlikeDead() || isPlayer() && (isTeleporting() || ((Player) this).isLogoutStarted());
	}

	public boolean isPotionsDisabled()
	{
		return isStunned() || isSleeping() || isParalyzed() || isAlikeDead() || isPlayer() && (isTeleporting() || ((Player) this).isLogoutStarted());
	}

	public final boolean isAttackingDisabled()
	{
		return _attackReuseEndTime > System.currentTimeMillis();
	}

	public boolean isOutOfControl()
	{
		return isBlocked() || isConfused() || isAfraid() || isPlayer() && (isTeleporting() || ((Player) this).isLogoutStarted());
	}

	public void teleToLocation(final Location loc)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), 0);
	}

	public void teleToLocation(final Location loc, final int instanceId)
	{
		teleToLocation(loc.getX(), loc.getY(), loc.getZ(), instanceId);
	}

	public void teleToLocation(final int x, final int y, final int z)
	{
		teleToLocation(x, y, z, 0);
	}

	public void teleToLocation(int x, int y, int z, final int instanceId)
	{
		if(isFakeDeath())
			breakFakeDeath();
		abortCast(true, false);
		setTarget(null);
		stopMove();
		if(!isVehicle() && !isFlying() && !World.isWater(new Location(x, y, z)))
			z = GeoEngine.getLowerHeight(x, y, z, getGeoIndex());
		if(isPlayer() && DimensionalRiftManager.getInstance().checkIfInRiftZone(getLoc(), true))
		{
			final Player player = (Player) this;
			if(player.isInParty() && player.getParty().isInDimensionalRift())
			{
				final Location newCoords = DimensionalRiftManager.getInstance().getRoom(0, 0).getTeleportCoords();
				x = newCoords.x;
				y = newCoords.y;
				z = newCoords.z;
				player.getParty().getDimensionalRift().usedTeleport(player);
			}
		}
		if(isPlayer())
		{
			final Player player = (Player) this;
			if(player.isLogoutStarted())
				return;
			if(player.isInVehicle())
				player.setVehicle(null);
			if(player.recording)
			{
				boolean ok = ZoneManager.getInstance().checkIfInZone(Zone.ZoneType.peace_zone, x, y, z) && !ZoneManager.getInstance().checkIfInZone(Zone.ZoneType.RESIDENCE, x, y, z);
				if(ok && Config.BOTS_RT_ZONES.length > 0)
				{
					Zone zone = null;
					for(final int id : Config.BOTS_RT_ZONES)
					{
						zone = ZoneManager.getInstance().getZoneById(Zone.ZoneType.peace_zone, id, true);
						if(zone != null && zone.checkIfInZone(x, y, z))
						{
							ok = false;
							break;
						}
					}
				}
				if(ok)
					player.recBot(9, x, y, z, 0, 0, 0);
				else
					player.writeBot(false);
			}
			setIsTeleporting(true);
			decayMe();
			setXYZInvisible(x, y, z);
			setReflectionId(instanceId);
			setLastClientPosition(null);
			setLastServerPosition(null);
			player.sendPacket(new TeleportToLocation(player, x, y, z));
		}
		else
		{
			setXYZ(x, y, z);
			broadcastPacket(new TeleportToLocation(this, x, y, z));
			getAI().notifyEvent(CtrlEvent.EVT_TELEPORTED);
		}
	}

	public void teleToClosestTown()
	{
		if(isPlayer() && ((Player) this).inEvent())
		{
			final Object[] script_args = { this };
			for(final Scripts.ScriptClassAndMethod handler : Scripts.onPlayerExit)
				Scripts.getInstance().callScripts(this, handler.className, handler.methodName, script_args);
		}
		teleToLocation(MapRegionTable.getTeleToClosestTown(this));
	}

	public void sendMessage(CustomMessage message)
	{
		//
	}

	public long getNonAggroTime()
	{
		return _nonAggroTime;
	}

	public void setNonAggroTime(final long time)
	{
		_nonAggroTime = time;
	}

	@Override
	public String toString()
	{
		return "mob " + getObjectId();
	}

	public void addExpAndSp(final long addToExp, final long addToSp)
	{}

	public void broadcastUserInfo(final boolean force)
	{}

	public void checkHpMessages(final double currentHp, final double newHp)
	{}

	public boolean checkPvP(final Creature target, final Skill skill)
	{
		return false;
	}

	public boolean consumeItem(final int itemConsumeId, final int itemCount)
	{
		return true;
	}

	public void doPickupItem(final GameObject object)
	{}

	public boolean isFearImmune()
	{
		return false;
	}

	public boolean isLethalImmune()
	{
		return false;
	}

	public boolean getChargedSoulShot()
	{
		return false;
	}

	public int getChargedSpiritShot()
	{
		return 0;
	}

	public int getIncreasedForce()
	{
		return 0;
	}

	public int getKarma()
	{
		return 0;
	}

	public double getLevelMod()
	{
		return 1.0;
	}

	public Servitor getServitor()
	{
		return null;
	}

	public int getPvpFlag()
	{
		return 0;
	}

	public int getTeam()
	{
		return 0;
	}

	public boolean isSitting()
	{
		return false;
	}

	public boolean isUndead()
	{
		return false;
	}

	public boolean isUsingDualWeapon()
	{
		return false;
	}

	public boolean isParalyzeImmune()
	{
		return false;
	}

	public void reduceArrowCount()
	{}

	public void sendChanges()
	{
		getStatsRecorder().sendChanges();
	}

	public void sendMessage(final String message)
	{}

	public void sendPacket(final IBroadcastPacket mov)
	{}

	public void sendPacket(final IBroadcastPacket... mov)
	{}

	public void sendPacket(final List<? extends IBroadcastPacket> mov)
	{}

	public void setIncreasedForce(final int i)
	{}

	public void startPvPFlag(final Creature target)
	{}

	public boolean unChargeShots(final boolean spirit)
	{
		return false;
	}

	public void updateEffectIcons()
	{}

	protected void refreshHpMpCp()
	{
		final int maxHp = getMaxHp();
		final int maxMp = getMaxMp();
		final int maxCp = isPlayer() ? getMaxCp() : 0;
		if(_currentHp > maxHp)
			setCurrentHp(maxHp, false);
		if(_currentMp > maxMp)
			setCurrentMp(maxMp, false);
		if(_currentCp > maxCp)
			setCurrentCp(maxCp, false);
		if(_currentHp < maxHp || _currentMp < maxMp || _currentCp < maxCp)
			startRegeneration();
	}

	public void updateStats()
	{
		refreshHpMpCp();
		sendChanges();
	}

	public void setOverhitAttacker(final Creature attacker)
	{}

	public void setOverhitDamage(final double damage)
	{}

	public boolean isCursedWeaponEquipped()
	{
		return false;
	}

	public boolean isHero()
	{
		return false;
	}

	public int getAccessLevel()
	{
		return 0;
	}

	public Clan getClan()
	{
		return null;
	}

	public void setFollowStatus(final boolean state, final boolean changeIntention)
	{}

	public void setLastClientPosition(final Location charPosition)
	{}

	public void setLastServerPosition(final Location charPosition)
	{}

	public boolean hasRandomAnimation()
	{
		return true;
	}

	public int getClanCrestId()
	{
		return 0;
	}

	public int getClanCrestLargeId()
	{
		return 0;
	}

	public int getAllyCrestId()
	{
		return 0;
	}

	public void disableItem(final Skill handler, final long timeTotal, final long timeLeft)
	{}

	public float getRateAdena()
	{
		return 1.0f;
	}

	public float getRateItems()
	{
		return 1.0f;
	}

	public double getRateExp()
	{
		return 1.0;
	}

	public double getRateSp()
	{
		return 1.0;
	}

	public float getRateSpoil()
	{
		return 1.0f;
	}

	@Override
	public void setXYZInvisible(final int x, final int y, final int z)
	{
		stopMove();
		super.setXYZInvisible(x, y, z);
	}

	@Override
	public boolean setLoc(final Location loc)
	{
		return setXYZ(loc.x, loc.y, loc.z);
	}

	public boolean setLoc(final Location loc, final boolean move)
	{
		return setXYZ(loc.x, loc.y, loc.z, move);
	}

	@Override
	public boolean setXYZ(final int x, final int y, final int z)
	{
		return setXYZ(x, y, z, false);
	}

	public boolean setXYZ(final int x, final int y, final int z, final boolean move)
	{
		if(!move)
			stopMove();

		boolean result = false;

		moveLock.lock();
		try
		{
			result = super.setXYZ(x, y, z);
		}
		finally
		{
			moveLock.unlock();
		}
		updateTerritories();
		return result;
	}

	public void turn(final Creature c, final int distance)
	{
		if(inObserverMode() || isActionsDisabled())
			return;
		if(isMoving)
			stopMove();
		broadcastPacket(new MoveToPawn(this, c, distance));
		setHeading(c, true);
	}

	public void validateLocation(final int broadcast)
	{
		final L2GameServerPacket sp = new ValidateLocation(this);
		if(broadcast == 0)
			sendPacket(sp);
		else if(broadcast == 1)
			broadcastPacket(sp);
		else
			broadcastPacketToOthers(sp);
	}

	public void addUnActiveSkill(final Skill skill)
	{
		if(skill == null || isUnActiveSkill(skill.getId()))
			return;

		if(getSkillLevel(skill.getId()) > 0) {
			removeStatsOwner(skill);
			removeTriggers(skill);
		}

		_unActiveSkills.add(skill.getId());
	}

	public void removeUnActiveSkill(final Skill skill)
	{
		if (skill == null || !isUnActiveSkill(skill.getId()))
			return;
		if (isPlayer() && skill.isHeroic() && Config.NO_HERO_SKILLS_SUB && getPlayer().isSubClassActive())
			return;

		if (getSkillLevel(skill.getId()) > 0) {
			addStatFuncs(skill.getStatFuncs());
			addTriggers(skill);
		}

		_unActiveSkills.remove(skill.getId());
	}

	public boolean isUnActiveSkill(final int id)
	{
		return _unActiveSkills.contains(id);
	}

	public int getClanId()
	{
		return _clan == null ? 0 : _clan.getClanId();
	}

	@Override
	public double getCollisionRadius()
	{
		return getTemplate().collisionRadius;
	}

	@Override
	public double getCollisionHeight()
	{
		return getTemplate().collisionHeight;
	}

	public AbnormalList getAbnormalList()
	{
		if(_effectList == null)
			synchronized (this)
			{
				if(_effectList == null)
					_effectList = new AbnormalList(this);
			}
		return _effectList;
	}

	public boolean canAttackCharacter(final Creature _target)
	{
		return _target.getPlayer() != null;
	}

	public boolean isGM()
	{
		return false;
	}

	public boolean isRaidFighter()
	{
		return false;
	}

	public boolean paralizeOnAttack(final Creature attacker)
	{
		int max_attacker_level = 65535;
		if(isNpc())
		{
			final int max_level_diff = ((NpcInstance) this).getParameter("ParalizeOnAttack", -1000);
			if(max_level_diff != -1000)
				max_attacker_level = getLevel() + max_level_diff;
			else if(Config.PARALIZE_ON_RAID_DIFF && (isRaid() || isRaidFighter()))
				max_attacker_level = getLevel() + Config.RAID_MAX_LEVEL_DIFF;
			if(attacker.getLevel() > max_attacker_level)
				return true;
		}
		return false;
	}

	public void onSeeSpell(final Skill skill, final Creature caster)
	{}

	public abstract byte getLevel();

	public abstract ItemInstance getActiveWeaponInstance();

	public abstract WeaponTemplate getActiveWeaponItem();

	public abstract ItemInstance getSecondaryWeaponInstance();

	public abstract WeaponTemplate getSecondaryWeaponItem();

	public CharListenerList getListeners()
	{
		if(listeners == null)
			synchronized (this)
			{
				if(listeners == null)
					listeners = new CharListenerList(this);
			}
		return listeners;
	}

	public <T extends Listener<Creature>> boolean addListener(final T listener)
	{
		return getListeners().add(listener);
	}

	public <T extends Listener<Creature>> boolean removeListener(final T listener)
	{
		return getListeners().remove(listener);
	}

	public CharStatsChangeRecorder<? extends Creature> getStatsRecorder()
	{
		if(_statsRecorder == null)
			synchronized (this)
			{
				if(_statsRecorder == null)
					_statsRecorder = new CharStatsChangeRecorder<Creature>(this);
			}
		return _statsRecorder;
	}

	public boolean isDecoy()
	{
		return false;
	}

	@Override
	public boolean isCreature()
	{
		return true;
	}

	@Override
	protected void onDelete()
	{
		GameObjectsStorage.remove(this);

		getAbnormalList().stopAll();

		super.onDelete();
	}

	public class MoveNextTask implements Runnable
	{
		private double alldist;
		private double donedist;
		public boolean abort;
		private boolean finish;

		public MoveNextTask setDist(final double dist, final boolean fin)
		{
			alldist = dist;
			donedist = 0.0;
			abort = false;
			finish = fin;
			return this;
		}

		@Override
		public void run()
		{
			if(!isMoving)
				return;

			moveLock.lock();
			try
			{
				if(!isMoving)
					return;
				if(isMovementDisabled())
				{
					stopMove();
					return;
				}
				Creature follow = null;
				final int speed = getMoveSpeed();
				if(speed <= 0)
				{
					stopMove();
					return;
				}
				final long now = System.currentTimeMillis();
				if(isFollow)
				{
					follow = getFollowTarget();
					if(follow == null)
					{
						stopMove();
						return;
					}
					if(isInRangeZ(follow, _offset) && GeoEngine.canSeeTarget(Creature.this, follow))
					{
						stopMove();
						ThreadPoolManager.getInstance().execute(new GameObjectTasks.NotifyAITask(Creature.this, CtrlEvent.EVT_ARRIVED_TARGET));
						return;
					}
				}
				if(alldist <= 0.0)
				{
					if(!abort)
						moveNext(false);
					return;
				}
				donedist += (now - _startMoveTime) * _previousSpeed / 1000.0;
				double done = donedist / alldist;
				if(done < 0.0)
					done = 0.0;
				if(done >= 1.0)
				{
					if(!abort)
						moveNext(false);
					return;
				}
				if(isMovementDisabled())
				{
					stopMove();
					return;
				}
				final int sz = _moveList.size();
				int index = (int) (sz * done);
				if(index >= sz)
					index = sz - 1;
				if(index < 0)
					index = 0;
				final Location loc = _moveList.get(index).clone().geo2world();
				if(!isFlying() && !isInVehicle() && !isInZone(Zone.ZoneType.water) && !isVehicle() && loc.z - getZ() > 256)
				{
					stopMove();
					return;
				}
				if(loc == null || isMovementDisabled())
				{
					stopMove();
					return;
				}
				setLoc(loc, true);
				if(isMovementDisabled())
				{
					stopMove();
					return;
				}
				if(isFollow && !abort && now - _followTimestamp > (_forestalling ? 500 : 1000) && follow != null && !follow.isInRange(movingDestTempPos, Math.max(50, _offset)))
				{
					if(Math.abs(getZ() - loc.z) > 1000 && !isFlying())
					{
						sendPacket(Msg.CANNOT_SEE_TARGET);
						stopMove();
						return;
					}
					if(buildPathTo(follow.getX(), follow.getY(), follow.getZ(), _offset, follow, true, true))
					{
						movingDestTempPos.set(follow.getX(), follow.getY(), follow.getZ());
						moveNext(true);
						return;
					}
					stopMove();
				}
				else
				{
					_previousSpeed = speed;
					_startMoveTime = now;
					if(!abort)
						_moveTask = ThreadPoolManager.getInstance().schedule(this, getMoveTickInterval());
				}
			}
			catch(Exception e)
			{
				_log.error("", e);
			}
			finally
			{
				moveLock.unlock();
			}
		}
	}

	private class AttackStanceTask implements Runnable
	{
		@Override
		public void run()
		{
			if(!isInCombat())
				stopAttackStanceTask();
		}
	}

	private class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			if(isDead() || getRegenTick() == 0L)
				return;

			double hpStart = _currentHp;
			double mpStart = _currentMp;
			double cpStart = _currentCp;

			final int maxHp = getMaxHp();
			final int maxMp = getMaxMp();
			final int maxCp = isPlayer() ? getMaxCp() : 0;

			double addHp = 0.0;
			double addMp = 0.0;
			regenLock.lock();
			try
			{
				if(_currentHp < maxHp)
					addHp += Formulas.calcHpRegen(Creature.this);
				if(_currentMp < maxMp)
					addMp += Formulas.calcMpRegen(Creature.this);
				if(isPlayer())
				{
					if(Config.REGEN_SIT_WAIT && isSitting())
					{
						addMp += Config.REGEN_SW_MP;
						addHp += Config.REGEN_SW_HP;
					}
				}
				else if(isRaid())
				{
					addHp *= Config.RATE_RAID_REGEN;
					addMp *= Config.RATE_RAID_REGEN;
				}

				_currentHp += Math.max(0.0, addHp);
				_currentMp += Math.max(0.0, addMp);

				_currentHp = Math.min(maxHp, _currentHp);
				_currentMp = Math.min(maxMp, _currentMp);

				getListeners().onChangeCurrentHp(hpStart, _currentHp);
				getListeners().onChangeCurrentMp(mpStart, _currentMp);

				if(isPlayer())
				{
					_currentCp += Math.max(0.0, Formulas.calcCpRegen(Creature.this));
					_currentCp = Math.min(maxCp, _currentCp);
					getListeners().onChangeCurrentCp(cpStart, _currentCp);
				}

				if(_currentHp == maxHp && _currentMp == maxMp && _currentCp == maxCp)
					stopRegeneration();
			}
			finally
			{
				regenLock.unlock();
			}
			broadcastStatusUpdate();
			sendChanges();
			checkHpMessages(hpStart, _currentHp);
		}
	}

	@Override
	protected Shape makeGeoShape()
	{
		int x = getX();
		int y = getY();
		int z = getZ();
		Circle circle = new Circle(x, y, (int) getCollisionRadius());
		circle.setZmin(z - Config.MAX_Z_DIFF);
		circle.setZmax(z + (int) getCollisionHeight());
		return circle;
	}

	@Override
	public int getGeoZ(int x, int y, int z)
	{
		if(isFlying() || isInWater() || isInVehicle() || isVehicle() || isDoor())
			return z;

		return super.getGeoZ(x, y, z);
	}
}
