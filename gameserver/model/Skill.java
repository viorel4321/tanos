package l2s.gameserver.model;

import java.lang.reflect.Constructor;
import java.util.*;

import l2s.gameserver.model.instances.*;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.skills.*;
import l2s.gameserver.skills.funcs.Func;
import l2s.gameserver.skills.skillclasses.*;
import l2s.gameserver.skills.skillclasses.DeathPenalty;
import l2s.gameserver.stats.StatTemplate;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.geometry.Polygon;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.SkillTrait;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.impl.DuelEvent;
import l2s.gameserver.network.l2.s2c.FlyToLocation;
import l2s.gameserver.network.l2.s2c.MagicSkillUse;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.conditions.Condition;
import l2s.gameserver.skills.effects.EffectTemplate;
import l2s.gameserver.skills.funcs.FuncTemplate;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.tables.SkillTree;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Util;

public abstract class Skill extends StatTemplate implements SkillInfo
{
	protected static Logger _log = LoggerFactory.getLogger(Skill.class);
	
	public static final int SKILL_CRAFTING = 172;
	public static final int SKILL_CRYSTALLIZE = 248;
	public static final int SKILL_TRANSFER_PAIN = 1262;
	public static final int WEAPON_MAGIC_MASTERY1 = 249;
	public static final int WEAPON_MAGIC_MASTERY2 = 250;
	public static final int SKILL_FISHING_MASTERY = 1315;
	public static final int SKILL_DIVINE_INSPIRATION = 1405;
	public static final int POLEARM_MASTERY = 216;
	public static final int BLINDING_BLOW = 321;
	public static final int SKILL_BLUFF = 358;
	public static final int SKILL_MYSTIC_IMMUNITY = 1411;
	public static final int SKILL_RAID_CURSE = 4515;
	public static final int PETRIFICATION = 4578;
	public static final int FAKE_DEATH = 60;
	public static final int PUNCH_OF_DOOM = 81;
	public static final int SAVEVS_INT = 1;
	public static final int SAVEVS_WIT = 2;
	public static final int SAVEVS_MEN = 3;
	public static final int SAVEVS_CON = 4;
	public static final int SAVEVS_DEX = 5;
	public static final int SAVEVS_STR = 6;
	
	protected Integer _id;
	protected int _level;
	protected boolean _isNotUsedByAI;
	protected boolean _isFishingSkill;
	protected boolean _isMusic;
	protected boolean _isTrigger;
	protected boolean _isNotAffectedByMute;
	private final Integer _displayId;
	protected int _displayLevel;
	private final String _name;
	private final SkillOpType _operateType;
	private final int _mpConsume1;
	private final int _mpConsume2;
	protected final double _lethal1;
	protected final double _lethal2;
	protected final double _lethalPvP1;
	protected final double _lethalPvP2;
	private final int _hpConsume;
	protected final int[] _itemConsume;
	protected final int[] _itemConsumeId;
	protected final boolean _isItemHandler;
	private final boolean _isCommon;
	private final boolean _isSaveable;
	private final boolean _showActivate;
	private final boolean _isItemSkill;
	private final boolean _isValidateable;
	private final int _castRange;
	private final int _effectRange;
	protected double _absorbPart;
	protected int _absorbPartStatic;
	private final int _reuseGroupId;
	private final int hashCode;
	private final int reuseCode;
	private final int _matak;
	protected Ternary _isUseSS;
	protected static final HashMap<Integer, List<Integer>> _reuseGroups = new HashMap<Integer, List<Integer>>();
	protected static final TreeMap<TriggerActionType, Double> EMPTY_ACTIONS = new TreeMap<TriggerActionType, Double>();
	protected AddedSkill[] _addedSkills;
	private final int _hitTime;
	private final int _coolTime;
	private final int _skillInterruptTime;
	private final int _reuseDelay;
	protected final SkillType _skillType;
	protected final SkillTargetType _targetType;
	protected SkillMagicType _magicType;
	protected SkillTrait _traitType;
	protected final double _power;
	protected double _powerPvP;
	protected double _powerPvE;
	private final double _powerStaticPvP;
	protected int _delayedEffect;
	protected final int _effectPoint;
	protected final int _skillRadius;
	private final int _affect_min;
	private final int _affect_max;
	private final boolean _undeadOnly;
	private final boolean _corpse;
	private final boolean _altUse;
	private final Element _element;
	private int _savevs;
	private final int _activateRate;
	private final int _levelModifier;
	private int _magicLevel;
	private final boolean _cancelable;
	private final boolean _shieldignore;
	private final boolean _overhit;
	protected boolean _isUsingWhileCasting;
	protected boolean _isIgnoreResists;
	protected boolean _deathlink;
	protected boolean _basedOnTargetDebuff;
	protected boolean _hideStartMessage;
	protected boolean _hideUseMessage;
	protected boolean _isCubicSkill;
	private final boolean _blessNoblesse;
	private final boolean _salvation;
	private final int _weaponsAllowed;
	protected int _criticalRate;
	private final boolean _isOffensive;
	private final boolean _isPvm;
	protected boolean _isHeroic;
	private final boolean _stopActor;
	private final int _forceId;
	private final FlyToLocation.FlyType _flyType;
	private final int _flyRadius;
	private final List<ClassId> _canLearn;
	private final List<Integer> _teachers;
	private Condition[] _preCondition = Condition.EMPTY_ARRAY;
	protected EffectTemplate[] _effectTemplates;
	private final int _minPledgeClass;
	private final boolean _isSuicideAttack;
	private final boolean _isSkillTimePermanent;
	protected boolean _isReuseDelayPermanent;
	protected boolean _isReflectable;
	protected boolean _isChargeBoost;
	private NextAction _nextAction;
	private final int _chance;
	private final int _angle;
	private final int _symbolId;
	private final int _negatePower;
	protected final int _numCharges;
	protected final int _condCharges;
	private final int _npcId;
	protected final boolean _behind;

	public final boolean isPvpSkill()
	{
		switch(_skillType)
		{
			case BLEED:
			case CANCEL:
			case AGGRESSION:
			case DEBUFF:
			case DOT:
			case MDOT:
			case MUTE:
			case PARALYZE:
			case POISON:
			case ROOT:
			case SLEEP:
			case MANADAM:
			case DESTROY_SUMMON:
			case NEGATE_STATS:
			case NEGATE_EFFECTS:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public final boolean isFishingSkill()
	{
		return _isFishingSkill;
	}

	public boolean isMusic()
	{
		return _magicType == SkillMagicType.MUSIC;
	}

	public boolean isTrigger()
	{
		return _isTrigger;
	}

	public boolean isOffensive()
	{
		if(_isOffensive)
			return _isOffensive;
		switch(_skillType)
		{
			case BLEED:
			case AGGRESSION:
			case DEBUFF:
			case DOT:
			case MDOT:
			case MUTE:
			case PARALYZE:
			case POISON:
			case ROOT:
			case SLEEP:
			case MANADAM:
			case DESTROY_SUMMON:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
			case AIEFFECTS:
			case DRAIN:
			case DRAIN_SOUL:
			case FATALBLOW:
			case LETHAL_SHOT:
			case MDAM:
			case MDAM_ELEMENTAL:
			case PDAM:
			case CPDAM:
			case SOULSHOT:
			case SPIRITSHOT:
			case SPOIL:
			case STUN:
			case SWEEP:
			case HARVESTING:
			case TELEPORT_NPC:
			case SOWING:
			case SHIFT_AGGRESSION:
			case DISCORD:
			case UNLOCK:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isPvM()
	{
		if(_isPvm)
			return _isPvm;
		switch(_skillType)
		{
			case DISCORD:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isAI()
	{
		switch(_skillType)
		{
			case AGGRESSION:
			case DELETE_HATE:
			case DELETE_HATE_OF_ME:
			case AIEFFECTS:
			case SOWING:
			case SHIFT_AGGRESSION:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isAoE()
	{
		switch(_targetType)
		{
			case TARGET_AREA:
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_AURA:
			case TARGET_PET_AURA:
			case TARGET_MULTIFACE:
			case TARGET_MULTIFACE_AURA:
			case TARGET_TUNNEL:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean isNotTargetAoE()
	{
		switch(_targetType)
		{
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_PARTY:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	public boolean oneTarget()
	{
		switch(_targetType)
		{
			case TARGET_CORPSE:
			case TARGET_CORPSE_PLAYER:
			case TARGET_HOLY:
			case TARGET_ITEM:
			case TARGET_NONE:
			case TARGET_ONE:
			case TARGET_PARTY_ONE:
			case TARGET_PARTY_ONE_OTHER:
			case TARGET_PET:
			case TARGET_OWNER:
			case TARGET_ENEMY_PET:
			case TARGET_SELF:
			case TARGET_UNLOCKABLE:
			case TARGET_CHEST:
			case TARGET_SIEGE:
			case TARGET_FLAGPOLE:
			{
				return true;
			}
			default:
			{
				return false;
			}
		}
	}

	protected Skill(final StatsSet set)
	{
		_addedSkills = AddedSkill.EMPTY_ARRAY;
		_isCubicSkill = false;
		_id = set.getInteger("skill_id");
		_level = set.getInteger("level");
		_displayId = set.getInteger("displayId", _id);
		_displayLevel = set.getInteger("displayLevel", _level);
		_name = set.getString("name");
		_operateType = set.getEnum("operateType", SkillOpType.class);
		_isHeroic = set.getBool("isHeroic", false);
		_altUse = set.getBool("altUse", false);
		_mpConsume1 = set.getInteger("mpConsume1", 0);
		_mpConsume2 = set.getInteger("mpConsume2", 0);
		_hpConsume = set.getInteger("hpConsume", 0);
		_isChargeBoost = set.getBool("chargeBoost", false);
		_matak = set.getInteger("mAtk", 0);
		_isUseSS = Ternary.valueOf(set.getString("useSS", Ternary.DEFAULT.toString()).toUpperCase());
		_forceId = set.getInteger("forceId", 0);

		final String s1 = set.getString("itemConsumeCount", "");
		final String s2 = set.getString("itemConsumeId", "");
		if(s1.length() == 0)
			_itemConsume = new int[] { 0 };
		else
		{
			final String[] s3 = s1.split(" ");
			_itemConsume = new int[s3.length];
			for(int i = 0; i < s3.length; ++i)
				_itemConsume[i] = Integer.parseInt(s3[i]);
		}

		if(s2.length() == 0)
			_itemConsumeId = new int[] { 0 };
		else
		{
			final String[] s3 = s2.split(" ");
			_itemConsumeId = new int[s3.length];
			for(int i = 0; i < s3.length; ++i)
				_itemConsumeId[i] = Integer.parseInt(s3[i]);
		}

		_isItemHandler = set.getBool("isHandler", false);
		_reuseGroupId = set.getInteger("reuseGroup", 0);
		_isCommon = set.getBool("isCommon", false);
		_isSaveable = set.getBool("isSaveable", true);
		_showActivate = set.getBool("showActivate", true);
		_isItemSkill = _name.contains("Item Skill");
		_isValidateable = set.getBool("isValidateable", !_isItemSkill);
		_castRange = set.getInteger("castRange", 40);
		_effectRange = set.getInteger("effectRange", -1);
		_hitTime = set.getInteger("hitTime", 0);
		_coolTime = set.getInteger("coolTime", 0);
		_skillInterruptTime = set.getInteger("hitCancelTime", 0);
		_reuseDelay = set.getInteger("reuseDelay", 0);
		_skillRadius = set.getInteger("skillRadius", 80);
		_isNotUsedByAI = set.getBool("isNotUsedByAI", false);
		_isIgnoreResists = set.getBool("isIgnoreResists", false);
		_isMusic = set.getBool("isMusic", false);
		_isTrigger = set.getBool("isTrigger", false);
		_isNotAffectedByMute = set.getBool("isNotAffectedByMute", false);
		_isUsingWhileCasting = set.getBool("isUsingWhileCasting", false);
		_targetType = set.getEnum("target", SkillTargetType.class);
		_magicType = set.getEnum("magicType", SkillMagicType.class, SkillMagicType.PHYSIC);
		_traitType = set.getEnum("trait", SkillTrait.class, null);
		_undeadOnly = set.getBool("undeadOnly", false);
		_corpse = set.getBool("corpse", false);
		_power = set.getDouble("power", 0.0);
		_powerPvP = set.getDouble("powerPvP", 0.0);
		_powerPvE = set.getDouble("powerPvE", 0.0);
		_powerStaticPvP = getPowerPvP() * set.getDouble("powerModPvP", 1.0);
		_effectPoint = set.getInteger("effectPoint", 0);
		_nextAction = NextAction.valueOf(set.getString("nextAction", "DEFAULT").toUpperCase());
		_skillType = set.getEnum("skillType", SkillType.class);
		_isSuicideAttack = set.getBool("isSuicideAttack", false);
		_isSkillTimePermanent = set.getBool("isSkillTimePermanent", false);
		_isReuseDelayPermanent = set.getBool("isReuseDelayPermanent", false);
		_deathlink = set.getBool("deathlink", false);
		_basedOnTargetDebuff = set.getBool("basedOnTargetDebuff", false);
		_element = Element.valueOf(set.getString("element", "NONE").toUpperCase());
		_savevs = set.getInteger("save", 0);
		_hideStartMessage = set.getBool("isHideStartMessage", false);
		_hideUseMessage = set.getBool("isHideUseMessage", false);
		_activateRate = set.getInteger("activateRate", -1);
		_levelModifier = set.getInteger("levelModifier", 1);
		_magicLevel = set.getInteger("magicLevel", 0);
		_cancelable = set.getBool("cancelable", true);
		_isReflectable = set.getBool("reflectable", true);
		_shieldignore = set.getBool("shieldignore", false);
		_criticalRate = set.getInteger("criticalRate", 0);
		_overhit = set.getBool("overHit", false);
		_weaponsAllowed = set.getInteger("weaponsAllowed", 0);
		_minPledgeClass = set.getInteger("minPledgeClass", 0);
		_isOffensive = set.getBool("isOffensive", false);
		_isFishingSkill = set.getBool("isFishingSkill", false);
		_isPvm = set.getBool("isPvm", false);
		_chance = set.getInteger("chance", 100);
		_behind = set.getBool("behind", false);
		_angle = set.getInteger("angle", _behind ? 120 : 60);
		_symbolId = set.getInteger("symbolId", 0);
		_npcId = set.getInteger("npcId", 0);
		_absorbPart = set.getFloat("absorbPart", 0);
		_absorbPartStatic = set.getInteger("absorbPartStatic", 0);
		_flyType = FlyToLocation.FlyType.valueOf(set.getString("flyType", "NONE").toUpperCase());
		_flyRadius = set.getInteger("flyRadius", 200);
		final String[] a = set.getString("affectLimit", "0;0").split(";");
		_affect_min = Integer.parseInt(a[0]);
		_affect_max = Integer.parseInt(a[1]);
		_negatePower = set.getInteger("negatePower", Integer.MAX_VALUE);
		_numCharges = set.getInteger("num_charges", 0);
		_condCharges = set.getInteger("cond_charges", 0);
		_delayedEffect = set.getInteger("delayedEffect", 0);
		_lethal1 = set.getDouble("lethal1", 0.0);
		_lethal2 = set.getDouble("lethal2", 0.0);
		_lethalPvP1 = set.getDouble("lethalPvP1", 0.0);
		_lethalPvP2 = set.getDouble("lethalPvP2", 0.0);
		_blessNoblesse = set.getBool("isBlessNoblesse", false);
		_salvation = set.getBool("isSalvation", false);
		_stopActor = set.getBool("stopActor", Config.SKILLS_STOP_ACTOR);
		StringTokenizer st2 = new StringTokenizer(set.getString("addSkills", ""), ";");
		while(st2.hasMoreTokens())
		{
			final int id = Integer.parseInt(st2.nextToken());
			int level = Integer.parseInt(st2.nextToken());
			if(level == -1)
				level = _level;
			_addedSkills = ArrayUtils.add(_addedSkills, new AddedSkill(id, level));
		}
		if(_nextAction == NextAction.DEFAULT)
			switch(_skillType)
			{
				case DRAIN_SOUL:
				case FATALBLOW:
				case LETHAL_SHOT:
				case PDAM:
				case CPDAM:
				case SPOIL:
				case STUN:
				case SOWING:
				{
					_nextAction = NextAction.ATTACK;
					break;
				}
				default:
				{
					_nextAction = NextAction.NONE;
					break;
				}
			}
		final String canLearn = set.getString("canLearn", null);
		if(canLearn == null)
			_canLearn = null;
		else
		{
			_canLearn = new ArrayList<ClassId>();
			st2 = new StringTokenizer(canLearn, " \r\n\t,;");
			while(st2.hasMoreTokens())
			{
				final String cls = st2.nextToken();
				try
				{
					_canLearn.add(ClassId.valueOf(cls));
				}
				catch(Throwable t)
				{
					_log.error("Bad class " + cls + " to learn skill", t);
				}
			}
		}
		final String teachers = set.getString("teachers", null);
		if(teachers == null)
			_teachers = null;
		else
		{
			_teachers = new ArrayList<Integer>();
			st2 = new StringTokenizer(teachers, " \r\n\t,;");
			while(st2.hasMoreTokens())
			{
				final String npcid = st2.nextToken();
				try
				{
					_teachers.add(Integer.parseInt(npcid));
				}
				catch(Throwable t2)
				{
					_log.error("Bad teacher id " + npcid + " to teach skill", t2);
				}
			}
		}
		hashCode = SkillTable.getSkillHashCode(_id, _level);
		if(_reuseGroupId > 0)
		{
			reuseCode = _reuseGroupId;
			if(_reuseGroups.get(_reuseGroupId) == null)
				_reuseGroups.put(_reuseGroupId, new ArrayList<Integer>());
			if(_itemConsumeId[0] != 0 && !_reuseGroups.get(_reuseGroupId).contains(_itemConsumeId[0]))
				_reuseGroups.get(_reuseGroupId).add(_itemConsumeId[0]);
		}
		else
			reuseCode = hashCode;
	}

	public abstract void onEndCast(Creature activeChar, Set<Creature> targets);

	public void onFinishCast(Creature aimingTarget, Creature activeChar, Set<Creature> targets)
	{
		if(isOffensive())
		{
			if(getTargetType() == SkillTargetType.TARGET_AREA_AIM_CORPSE)
			{
				if(aimingTarget.isNpc())
					((NpcInstance) aimingTarget).endDecayTask();
				else if(aimingTarget.isSummon())
					((SummonInstance) aimingTarget).endDecayTask();
			}
			else if(getTargetType() == SkillTargetType.TARGET_CORPSE)
			{
				for(Creature target : targets)
				{
					if(target.isNpc())
						((NpcInstance) target).endDecayTask();
					else if(target.isSummon())
						((SummonInstance) target).endDecayTask();
				}
			}
		}
	}

	public final boolean altUse()
	{
		return _altUse;
	}

	public final SkillType getSkillType()
	{
		return _skillType;
	}

	public final int getSavevs()
	{
		return _savevs;
	}

	public final int getActivateRate()
	{
		return _activateRate;
	}

	public final int getLevelModifier()
	{
		return _levelModifier;
	}

	public final int getMagicLevel()
	{
		return _magicLevel;
	}

	public boolean isBox()
	{
		return _skillType == SkillType.UNLOCK;
	}

	public final boolean isCancelable()
	{
		return _cancelable && !isToggle();
	}

	public final boolean getShieldIgnore()
	{
		return _shieldignore;
	}

	public Abnormal getSameByStackType(final List<Abnormal> ef_list)
	{
		if(_effectTemplates == null)
			return null;
		for(final EffectTemplate et : _effectTemplates)
		{
			final Abnormal ret;
			if(et != null && (ret = et.getSameByStackType(ef_list)) != null)
				return ret;
		}
		return null;
	}

	public Abnormal getSameByStackType(final Creature actor)
	{
		return getSameByStackType(actor.getAbnormalList().values());
	}

	public final Element getElement()
	{
		return _element;
	}

	public final SkillTargetType getTargetType()
	{
		return _targetType;
	}

	public final SkillTrait getTraitType()
	{
		return _traitType;
	}

	public final boolean isOverhit()
	{
		return _overhit;
	}

	public final boolean isSuicideAttack()
	{
		return _isSuicideAttack;
	}

	public final boolean isSkillTimePermanent()
	{
		return _isSkillTimePermanent || _isItemHandler;
	}

	public final boolean isReuseDelayPermanent()
	{
		return _isReuseDelayPermanent || _isItemHandler;
	}

	public boolean isDeathlink()
	{
		return _deathlink;
	}

	public boolean isBasedOnTargetDebuff()
	{
		return _basedOnTargetDebuff;
	}

	public boolean isChargeBoost()
	{
		return _isChargeBoost;
	}

	public final double getPower(final Creature target)
	{
		if(target != null)
		{
			if(target.isPlayable() && !target.isInOlympiadMode())
				return _powerStaticPvP;
			if(target.isMonster())
				return getPowerPvE();
		}
		return this.getPower();
	}

	public final double getPower()
	{
		return _power;
	}

	public final double getPowerPvP()
	{
		return _powerPvP != 0.0 ? _powerPvP : _power;
	}

	public final double getPowerPvE()
	{
		return _powerPvE != 0.0 ? _powerPvE : _power;
	}

	public final int getCastRange()
	{
		return _castRange;
	}

	public final int getEffectRange()
	{
		return _effectRange;
	}

	public final int getAOECastRange()
	{
		return Math.max(_castRange, _skillRadius);
	}

	public final int getCastRangeForAi()
	{
		return _castRange > 0 ? _castRange : _skillRadius;
	}

	public final int getHpConsume()
	{
		return _hpConsume;
	}

	public void setDefMagicLevel()
	{
		if(_magicLevel == 0)
			_magicLevel = SkillTree.getInstance().getMinSkillLevel(_id, _level);
	}

	public final int getId()
	{
		return _id;
	}

	@Override
	public final int getDisplayId()
	{
		return _displayId;
	}

	@Override
	public int getDisplayLevel()
	{
		return _displayLevel;
	}

	@Override
	public final Skill getTemplate() {
		return this;
	}

	public void setDisplayLevel(final Short lvl)
	{
		_displayLevel = lvl;
	}

	public void setMagicType(final SkillMagicType type)
	{
		_magicType = type;
	}

	public boolean isItemSkill()
	{
		return _isItemSkill;
	}

	public int getMinPledgeClass()
	{
		return _minPledgeClass;
	}

	public final int[] getItemConsume()
	{
		return _itemConsume;
	}

	public final int[] getItemConsumeId()
	{
		return _itemConsumeId;
	}

	public final boolean isHandler()
	{
		return _isItemHandler;
	}

	public final boolean isMagic()
	{
		return _magicType == SkillMagicType.MAGIC || _magicType == SkillMagicType.SPECIAL;
	}

	public final boolean isPhysic()
	{
		return _magicType == SkillMagicType.PHYSIC || _magicType == SkillMagicType.MUSIC;
	}

	public final SkillMagicType getMagicType()
	{
		return _magicType;
	}

	public final boolean isHeroic()
	{
		return _isHeroic;
	}

	public final boolean isCommon()
	{
		return _isCommon;
	}

	public final int getCriticalRate()
	{
		return _criticalRate;
	}

	public final List<Integer> getReuseGroup()
	{
		return _reuseGroups.get(_reuseGroupId);
	}

	public final int getReuseGroupId()
	{
		return _reuseGroupId;
	}

	public final int getLevel()
	{
		return _level;
	}

	public final int getMpConsume()
	{
		return _mpConsume1 + _mpConsume2;
	}

	public final int getMpConsume1()
	{
		return _mpConsume1;
	}

	public final int getMpConsume2()
	{
		return _mpConsume2;
	}

	public final String getName()
	{
		return _name;
	}

	public final int getReuseDelay()
	{
		return _reuseDelay;
	}

	public final int getHitTime()
	{
		return _hitTime;
	}

	public final int getCoolTime()
	{
		return _coolTime;
	}

	public final boolean isReflectable()
	{
		return _isReflectable;
	}

	public final int getSkillInterruptTime()
	{
		return _skillInterruptTime;
	}

	public final int getSkillRadius()
	{
		return _skillRadius;
	}

	public boolean isNotUsedByAI()
	{
		return _isNotUsedByAI;
	}

	public boolean isIgnoreResists()
	{
		return _isIgnoreResists;
	}

	public boolean isNotAffectedByMute()
	{
		return _isNotAffectedByMute;
	}

	public final boolean isActive()
	{
		return _operateType == SkillOpType.OP_ACTIVE;
	}

	public final boolean isPassive()
	{
		return _operateType == SkillOpType.OP_PASSIVE;
	}

	public final boolean isToggle()
	{
		return _operateType == SkillOpType.OP_TOGGLE;
	}

	public final int getWeaponsAllowed()
	{
		return _weaponsAllowed;
	}

	public final boolean getCanLearn(final ClassId cls)
	{
		return _canLearn == null || _canLearn.contains(cls);
	}

	public final boolean canTeachBy(final int npcId)
	{
		return _teachers == null || _teachers.contains(npcId);
	}

	public final boolean getWeaponDependancy(final Creature activeChar)
	{
		if(_weaponsAllowed == 0)
			return true;
		if(activeChar.getActiveWeaponInstance() != null && activeChar.getActiveWeaponItem() != null && (activeChar.getActiveWeaponItem().getItemType().mask() & _weaponsAllowed) != 0x0L)
			return true;
		if(activeChar.getSecondaryWeaponInstance() != null && activeChar.getSecondaryWeaponItem() != null && (activeChar.getSecondaryWeaponItem().getItemType().mask() & _weaponsAllowed) != 0x0L)
			return true;
		final StringBuffer skillmsg = new StringBuffer();
		skillmsg.append(_name);
		skillmsg.append(" can only be used with weapons of type ");
		for(final WeaponTemplate.WeaponType wt : WeaponTemplate.WeaponType.values())
			if((wt.mask() & _weaponsAllowed) != 0x0L)
				skillmsg.append(wt).append('/');
		skillmsg.setCharAt(skillmsg.length() - 1, '.');
		activeChar.sendMessage(skillmsg.toString());
		return false;
	}

	public final boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first)
	{
		return checkCondition(activeChar, target, forceUse, dontMove, first, true, false);
	}

	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(activeChar.isDead())
			return false;

		boolean ok = checkCondition2(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
		if(!ok && (oneTarget() || _targetType == SkillTargetType.TARGET_AREA || _targetType == SkillTargetType.TARGET_MULTIFACE) && target != null && activeChar != target && _castRange > 0 && activeChar.isInRange(target, _castRange))
			activeChar.turn(target, 10000);

		return ok;
	}

	private boolean checkCondition2(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(target != null && activeChar.getReflectionId() != target.getReflectionId())
		{
			if(sendMsg) {
				activeChar.sendPacket(Msg.CANNOT_SEE_TARGET);
			}
			return false;
		}

		if(!getWeaponDependancy(activeChar))
			return false;

		if(activeChar.isUnActiveSkill(_id))
			return false;

		if(first && activeChar.isSkillDisabled(this))
		{
			if(sendMsg && Config.ALT_SHOW_REUSE_MSG && activeChar.isPlayer())
				activeChar.sendPacket(new SystemMessage(48).addSkillName(_id, _displayLevel));
			return false;
		}

		if(first && activeChar.getCurrentMp() < (isMagic() ? activeChar.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, _mpConsume1 + _mpConsume2, target, this) : activeChar.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, _mpConsume1 + _mpConsume2, target, this)))
		{
			if(sendMsg)
				activeChar.sendPacket(Msg.NOT_ENOUGH_MP);
			return false;
		}

		if(activeChar.getCurrentHp() < _hpConsume + 1)
		{
			if(sendMsg)
				activeChar.sendPacket(Msg.NOT_ENOUGH_HP);
			return false;
		}

		if(!_isItemHandler && !_altUse && (isMagic() && activeChar.isMMuted() || !isMagic() && activeChar.isPMuted()))
			return false;

		if(activeChar.getIncreasedForce() < _condCharges || activeChar.getIncreasedForce() < _numCharges)
		{
			if(sendMsg)
				activeChar.sendPacket(new SystemMessage(113).addSkillName(_id, _displayLevel));
			return false;
		}

		Player player = activeChar.getPlayer();
		if(player != null)
		{
			if((_isItemHandler || _altUse) && player.isInOlympiadMode() && _id != 2165 && _id != 2170)
			{
				if(sendMsg)
					player.sendPacket(Msg.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
				return false;
			}
	
			if(player.isInVehicle() && !(this instanceof FishingSkill) && !(this instanceof ReelingPumping) && !isToggle())
				return false;

			if(player.inObserverMode())
			{
				if(sendMsg)
					activeChar.sendPacket(new SystemMessage(781));
				return false;
			}

			if(first && _itemConsume[0] > 0)
			{
				for(int i = 0; i < _itemConsume.length; ++i)
				{
					final ItemInstance requiredItems = player.getInventory().findItemByItemId(_itemConsumeId[i]);
					if(requiredItems == null || requiredItems.getCount() < _itemConsume[i])
					{
						if(sendMsg)
							player.sendPacket(Msg.INCORRECT_ITEM_COUNT);
						return false;
					}
				}
			}

			if(player.isFishing() && !isFishingSkill() && !altUse() && !activeChar.isSummon())
			{
				if(sendMsg && activeChar == player)
					player.sendPacket(Msg.ONLY_FISHING_SKILLS_ARE_AVAILABLE);
				return false;
			}
	
			if(_id == 60 && player.isMounted() || player.restrictSkillZone(_id))
			{
				if(sendMsg)
					player.sendPacket(new SystemMessage(113).addSkillName(_id, _displayLevel));
				return false;
			}
		}
		if(getFlyType() != FlyToLocation.FlyType.NONE)
		{
			if(activeChar.isImmobilized() || activeChar.isRooted())
			{
				if(sendMsg)
					activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
				return false;
			}

			if(first && target != null && getFlyType() == FlyToLocation.FlyType.CHARGE)
			{
				if(activeChar.isInRange(target.getLoc(), Math.min(150, getFlyRadius())))
				{
					if(sendMsg)
						activeChar.sendMessage("Not enough free space, the skill can't be used.");
					return false;
				}
				if(Config.ALLOW_GEODATA && !GeoEngine.canMoveToCoord(activeChar.getX(), activeChar.getY(), activeChar.getZ(), target.getX(), target.getY(), target.getZ(), activeChar.getGeoIndex()))
				{
					if(sendMsg)
						activeChar.sendMessage("The target is located where you can't charge.");
					return false;
				}
			}
		}

		SystemMessage msg = checkTarget(activeChar, target, target, forceUse, first, trigger);
		if(msg != null && activeChar.getPlayer() != null)
		{
			if(sendMsg)
				activeChar.getPlayer().sendPacket(msg);
			return false;
		}

		if(_preCondition.length == 0)
			return true;

		if(first)
		{
			final Env env = new Env();
			env.character = activeChar;
			env.skill = this;
			env.target = target;
			for(final Condition c : _preCondition)
			{
				if(c != null && !c.test(env))
				{
					if(sendMsg)
					{
						String cond_msg = c.getMessage();
						if(cond_msg != null)
							activeChar.sendMessage(cond_msg);
						else
						{
							SystemMessage cond_msgId = c.getSystemMsg();
							if(cond_msgId != null)
								activeChar.sendPacket(cond_msgId.addSkillName(_id, _displayLevel));
						}
					}
					return false;
				}
			}
		}
		return true;
	}

	public SystemMessage checkTarget(Creature activeChar, Creature target, Creature aimingTarget, boolean forceUse, boolean first, boolean trigger)
	{
		if(target == activeChar && isNotTargetAoE() || target == activeChar.getServitor() && _targetType == SkillTargetType.TARGET_PET_AURA)
			return null;
		if(target == null || isOffensive() && (target == activeChar || Config.NO_DAMAGE_NPC && activeChar.isPlayable() && target.isNpc() && !target.isDmg()))
			return Msg.TARGET_IS_INCORRECT;
		if(activeChar.getReflectionId() != target.getReflectionId()) {
			return Msg.CANNOT_SEE_TARGET;
		}
		if(!trigger) // TODO: Логично, но не вылазят ли косяки?
		{
			if (!first && target != activeChar && target == aimingTarget && getCastRange() > 0 && getCastRange() != 32767 && !activeChar.isInRange(target.getLoc(), getEffectRange()))
				return Msg.YOUR_TARGET_IS_OUT_OF_RANGE;
		}
		if(_skillType == SkillType.TAKECASTLE)
			return null;
		// Конусообразные скиллы
		if(!first && target != activeChar && (_targetType == SkillTargetType.TARGET_MULTIFACE || _targetType == SkillTargetType.TARGET_MULTIFACE_AURA || _targetType == SkillTargetType.TARGET_TUNNEL) && (_behind ? Util.isFacing(activeChar, target, _angle) : !Util.isFacing(activeChar, target, _angle)))
			return Msg.YOUR_TARGET_IS_OUT_OF_RANGE;
		if(target.isDead() != _corpse && _targetType != SkillTargetType.TARGET_AREA_AIM_CORPSE || _undeadOnly && !target.isUndead())
			return Msg.INCORRECT_TARGET;
		if(target.isMonster() && ((MonsterInstance) target).isDying())
			return Msg.INCORRECT_TARGET;
		if(_targetType != SkillTargetType.TARGET_UNLOCKABLE && target.isDoor() && !((DoorInstance) target).isAttackable(activeChar))
			return Msg.INCORRECT_TARGET;
		if(_altUse || _skillType.equals(SkillType.BEAST_FEED) || _targetType == SkillTargetType.TARGET_UNLOCKABLE || _targetType == SkillTargetType.TARGET_CHEST)
			return null;
		if(Config.CHECK_EPIC_CAN_DAMAGE && activeChar.isInZone(Zone.ZoneType.epic) != target.isInZone(Zone.ZoneType.epic))
			return Msg.TARGET_IS_INCORRECT;
		final Player player = activeChar.getPlayer();
		if(player != null)
		{
			final Player pcTarget = target.getPlayer();
			if(pcTarget != null)
			{
				if(isPvM())
					return Msg.TARGET_IS_INCORRECT;
				if(pcTarget.isInOlympiadMode() && (!player.isInOlympiadMode() || player.getOlympiadGameId() != pcTarget.getOlympiadGameId()))
					return Msg.TARGET_IS_INCORRECT;
				if(player.isInDuel() && pcTarget.isInDuel() && player.getEvent(DuelEvent.class) == pcTarget.getEvent(DuelEvent.class) && player.getTeam() != pcTarget.getTeam())
					return null;
				if(player.getTeam() > 0 && player.isChecksForTeam() && pcTarget.getTeam() == 0)
					return Msg.TARGET_IS_INCORRECT;
				if(pcTarget.getTeam() > 0 && pcTarget.isChecksForTeam() && player.getTeam() == 0)
					return Msg.TARGET_IS_INCORRECT;
				if(isOffensive())
				{
					if(Config.ALLOW_WINGS_MOD && !target.isAutoAttackable(player))
					{
						final ItemInstance item = player.getInventory().getPaperdollItem(16);
						if(item != null && item.getItemId() == 9585)
							return Msg.TARGET_IS_INCORRECT;
					}
					final boolean noUse = !Config.OSWEOC && hasEffects();
					if(noUse)
					{
						if(player == pcTarget)
							return Msg.INCORRECT_TARGET;
						if(player.getTeam() == 0 && !player.isInOlympiadMode())
						{
							if(player.isInParty() && (player.getParty() == pcTarget.getParty() || player.getParty().getCommandChannel() != null && pcTarget.isInParty() && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel()))
								return Msg.INCORRECT_TARGET;
							if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
								return Msg.INCORRECT_TARGET;
							if(player.getAllyId() != 0 && player.getAllyId() == pcTarget.getAllyId())
								return Msg.INCORRECT_TARGET;
							if(player.getAllyId() != 0 && player.getAllyId() == pcTarget.getAllyId())
								return Msg.INCORRECT_TARGET;
						}
					}
					if(player.isInOlympiadMode() && !player.isOlympiadCompStart())
						return Msg.INCORRECT_TARGET;
					if(player.getTeam() > 0 && player.isChecksForTeam() && pcTarget.getTeam() > 0 && pcTarget.isChecksForTeam() && player.getTeam() == pcTarget.getTeam() && player != pcTarget)
						return Msg.TARGET_IS_INCORRECT;
					if(isAoE() && getCastRange() != 32767 && !GeoEngine.canSeeTarget(activeChar, target)) {
						return Msg.CANNOT_SEE_TARGET;
					}
					if(activeChar.isInZonePeace() || target.isInZonePeace())
						return Msg.YOU_CANNOT_ATTACK_THE_TARGET_IN_THE_PEACE_ZONE;
					SystemMessage msg = null;
					for(final GlobalEvent e : activeChar.getEvents())
						if((msg = e.checkForAttack(target, activeChar, this, forceUse)) != null)
							return msg;
					for(final GlobalEvent e : activeChar.getEvents())
						if(e.canAttack(target, activeChar, this, forceUse, false))
							return null;
					if(activeChar.isInZoneBattle())
					{
						if(!noUse && !forceUse && (player.isInParty() && (player.getParty() == pcTarget.getParty() || player.getParty().getCommandChannel() != null && pcTarget.isInParty() && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel()) || player.getTeam() == 0 && !player.isInOlympiadMode() && (player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId() || player.getAllyId() != 0 && pcTarget.getAllyId() == player.getAllyId())))
							return Msg.INCORRECT_TARGET;
						return null;
					}
					else
					{
						if(isPvpSkill() || !forceUse || isAoE())
						{
							if(!noUse)
							{
								if(player == pcTarget)
									return Msg.INCORRECT_TARGET;
								if(player.isInParty() && (player.getParty() == pcTarget.getParty() || player.getParty().getCommandChannel() != null && pcTarget.isInParty() && player.getParty().getCommandChannel() == pcTarget.getParty().getCommandChannel()))
									return Msg.INCORRECT_TARGET;
								if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
									return Msg.INCORRECT_TARGET;
								if(player.getAllyId() != 0 && player.getAllyId() == pcTarget.getAllyId())
									return Msg.INCORRECT_TARGET;
								if(player.getAllyId() != 0 && player.getAllyId() == pcTarget.getAllyId())
									return Msg.INCORRECT_TARGET;
							}
							if(pcTarget.isDead() || target.isDead())
								return Msg.INCORRECT_TARGET;
						}
						if(activeChar.isInZone(Zone.ZoneType.Siege) && target.isInZone(Zone.ZoneType.Siege))
							return null;
						if(player.atMutualWarWith(pcTarget))
							return null;
						if(pcTarget.getPvpFlag() != 0)
							return null;
						if(pcTarget.getKarma() > 0)
							return null;
						if(forceUse && !isPvpSkill() && (!isAoE() || aimingTarget == target))
							return null;
						return Msg.INCORRECT_TARGET;
					}
				}
				else
				{
					if(pcTarget == player)
						return null;
					if(player.getTeam() > 0 && player.isChecksForTeam() && pcTarget.getTeam() > 0 && pcTarget.isChecksForTeam() && player.getTeam() != pcTarget.getTeam())
						return Msg.TARGET_IS_INCORRECT;
					if(forceUse)
						return null;
					if(player.isInParty() && player.getParty() == pcTarget.getParty())
						return null;
					if(player.getClanId() != 0 && player.getClanId() == pcTarget.getClanId())
						return null;
					if(player.atMutualWarWith(pcTarget))
						return Msg.INCORRECT_TARGET;
					if((pcTarget.getPvpFlag() != 0 || pcTarget.getKarma() > 0) && _targetType != SkillTargetType.TARGET_CORPSE_PLAYER)
						return Msg.INCORRECT_TARGET;
					return null;
				}
			}
		}
		if(!trigger || target != aimingTarget) // TODO: Логично, но не вылазят ли косяки?
		{
			if (isAoE() && isOffensive() && getCastRange() != 32767 && !GeoEngine.canSeeTarget(activeChar, target)) {
				return Msg.CANNOT_SEE_TARGET;
			}
		}
		if(!forceUse && !isOffensive() && target.isAutoAttackable(activeChar))
			return Msg.INCORRECT_TARGET;
		if(!target.isAttackable(activeChar))
			return Msg.INCORRECT_TARGET;
		return null;
	}

	public final Creature getAimingTarget(final Creature activeChar, final GameObject obj)
	{
		Creature target = obj == null || !obj.isCreature() ? null : (Creature) obj;
		switch(_targetType)
		{
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_PARTY:
			case TARGET_SELF:
			{
				return activeChar;
			}
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			{
				return activeChar;
			}
			case TARGET_HOLY:
			{
				return target != null && activeChar.isPlayer() && target.isArtefact() ? target : null;
			}
			case TARGET_FLAGPOLE:
			{
				return activeChar.isPlayer() && target instanceof StaticObjectInstance && ((StaticObjectInstance) target).getType() == 3 ? target : null;
			}
			case TARGET_UNLOCKABLE:
			{
				return target != null && (target.isDoor() || target.isChest() || target.isBox()) ? target : null;
			}
			case TARGET_CHEST:
			{
				return target != null && (target.isChest() || target.isBox()) ? target : null;
			}
			case TARGET_PET_AURA:
			case TARGET_PET:
			{
				target = activeChar.getServitor();
				return target != null && target.isDead() == _corpse ? target : null;
			}
			case TARGET_OWNER:
			{
				if(activeChar.isPet())
				{
					target = activeChar.getPlayer();
					return target != null && target.isDead() == _corpse ? target : null;
				}
				return null;
			}
			case TARGET_ENEMY_PET:
			{
				if(target == null || target == activeChar.getServitor() || !target.isSummon())
					return null;
				return target;
			}
			case TARGET_ONE:
			{
				return target != null && target.isDead() == _corpse && (target != activeChar || !isOffensive()) && (!_undeadOnly || target.isUndead()) ? target : null;
			}
			case TARGET_PARTY_ONE:
			{
				if(target == null)
					return null;
				if(target.getPlayer() != null && target.getPlayer().equals(activeChar))
					return target;
				if(target.getPlayer() != null && activeChar.getPlayer() != null && activeChar.getPlayer().isInParty() && activeChar.getPlayer().getParty().containsMember(target.getPlayer()) && target.isDead() == _corpse && (target != activeChar || !isOffensive()) && (!_undeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_PARTY_ONE_OTHER:
			{
				if(target == null)
					return null;
				if(target.getPlayer() != null && target.getPlayer().equals(activeChar))
					return null;
				if(target.getPlayer() != null && activeChar.getPlayer() != null && activeChar.getPlayer().isInParty() && activeChar.getPlayer().getParty().containsMember(target.getPlayer()) && target.isDead() == _corpse && (target != activeChar || !isOffensive()) && (!_undeadOnly || target.isUndead()))
					return target;
				return null;
			}
			case TARGET_AREA:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			{
				return target != null && target.isDead() == _corpse && (target != activeChar || !isOffensive()) && (!_undeadOnly || target.isUndead()) ? target : null;
			}
			case TARGET_AREA_AIM_CORPSE:
			{
				return target != null && target.isDead() ? target : null;
			}
			case TARGET_CORPSE:
			{
				if(target == null || !target.isDead())
					return null;
				if(target.isSummon() && target != activeChar.getServitor())
					return target;
				return target.isNpc() ? target : null;
			}
			case TARGET_CORPSE_PLAYER:
			{
				return target != null && target.isPlayable() && target.isDead() ? target : null;
			}
			case TARGET_SIEGE:
			{
				return target != null && !target.isDead() && (target.isDoor() || target instanceof ControlTowerInstance) ? target : null;
			}
			default:
			{
				activeChar.sendMessage("Target type of skill is not currently handled.");
				return null;
			}
		}
	}

	public Set<Creature> getTargets(final Creature activeChar, final Creature aimingTarget, final boolean forceUse)
	{
		Set<Creature> targets = new HashSet<>();
		if(oneTarget())
		{
			targets.add(aimingTarget);
			return targets;
		}

		switch(_targetType)
		{
			case TARGET_AREA:
			case TARGET_AREA_AIM_CORPSE:
			case TARGET_MULTIFACE:
			case TARGET_TUNNEL:
			{
				if(aimingTarget.isDead() == _corpse && (!_undeadOnly || aimingTarget.isUndead()))
					targets.add(aimingTarget);
				addTargetsToList(targets, aimingTarget, activeChar, forceUse);
				break;
			}
			case TARGET_AURA:
			case TARGET_MULTIFACE_AURA:
			{
				addTargetsToList(targets, activeChar, activeChar, forceUse);
				break;
			}
			case TARGET_PET_AURA:
			{
				if(activeChar.getServitor() == null)
					break;
				addTargetsToList(targets, activeChar.getServitor(), activeChar, forceUse);
				break;
			}
			case TARGET_ALLY:
			case TARGET_CLAN:
			case TARGET_CLAN_ONLY:
			case TARGET_PARTY:
			{
				if(activeChar.isMonster() || activeChar.isSiegeGuard())
				{
					targets.add(activeChar);
					for(final Creature c : World.getAroundCharacters(activeChar, _skillRadius, 600))
						if(!c.isDead() && (c.isMonster() || c.isSiegeGuard()))
							targets.add(c);
					break;
				}
				final Player player = activeChar.getPlayer();
				if(player == null)
					break;
				for(final Player target : World.getAroundPlayers(player, _skillRadius, 600))
				{
					boolean check = false;
					switch(_targetType)
					{
						case TARGET_PARTY:
						{
							check = player.isInParty() && player.getParty() == target.getParty();
							break;
						}
						case TARGET_CLAN:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.isInParty() && target.getParty() == player.getParty();
							break;
						}
						case TARGET_CLAN_ONLY:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId();
							break;
						}
						case TARGET_ALLY:
						{
							check = player.getClanId() != 0 && target.getClanId() == player.getClanId() || player.getAllyId() != 0 && target.getAllyId() == player.getAllyId();
							break;
						}
					}
					if(!check)
						continue;
					if(player.isInOlympiadMode() && target.isInOlympiadMode())
						continue;
					if(checkTarget(player, target, aimingTarget, forceUse, false, false) != null)
						continue;
					if(getCastRange() != 32767 && !activeChar.isInRange(target.getLoc(), getCastRange() + (getCastRange() < 200 ? 400 : 500)))
						continue;
					addTargetAndPetToList(targets, player, target);
				}
				addTargetAndPetToList(targets, player, player);
				break;
			}
		}
		return targets;
	}

	private void addTargetAndPetToList(final Set<Creature> targets, final Player actor, final Player target)
	{
		final Servitor pet = target.getServitor();
		boolean addPet = pet != null && actor.isInRange(pet, _skillRadius) && pet.isDead() == _corpse;
		if(addPet && isOffensive())
		{
			targets.add(pet);
			addPet = false;
		}
		if((actor == target || actor.isInRange(target, _skillRadius)) && target.isDead() == _corpse)
			targets.add(target);
		if(addPet)
			targets.add(pet);
	}

	private void addTargetsToList(final Set<Creature> targets, final Creature aimingTarget, final Creature activeChar, final boolean forceUse)
	{
		int count = 0;
		final int limit = _affect_min + Rnd.get(0, _affect_max);
		Polygon terr = null;
		if(_targetType == SkillTargetType.TARGET_TUNNEL)
		{
			final int radius = 100;
			final int zmin1 = activeChar.getZ() - 200;
			final int zmax1 = activeChar.getZ() + 200;
			final int zmin2 = aimingTarget.getZ() - 200;
			final int zmax2 = aimingTarget.getZ() + 200;
			final double angle = Util.convertHeadingToDegree(activeChar.getHeading());
			final double radian1 = Math.toRadians(angle - 90.0);
			final double radian2 = Math.toRadians(angle + 90.0);
			terr = new Polygon().add(activeChar.getX() + (int) (Math.cos(radian1) * radius), activeChar.getY() + (int) (Math.sin(radian1) * radius)).add(activeChar.getX() + (int) (Math.cos(radian2) * radius), activeChar.getY() + (int) (Math.sin(radian2) * radius)).add(aimingTarget.getX() + (int) (Math.cos(radian2) * radius), aimingTarget.getY() + (int) (Math.sin(radian2) * radius)).add(aimingTarget.getX() + (int) (Math.cos(radian1) * radius), aimingTarget.getY() + (int) (Math.sin(radian1) * radius)).setZmin(Math.min(zmin1, zmin2)).setZmax(Math.max(zmax1, zmax2));
		}

		for(final Creature target : aimingTarget.getAroundCharacters(_skillRadius, 300))
		{
			if(terr != null && !terr.isInside(target.getX(), target.getY(), target.getZ()))
				continue;
			if(target == null || activeChar == target)
				continue;
			if(activeChar.getPlayer() != null && activeChar.getPlayer() == target.getPlayer())
				continue;
			if(checkTarget(activeChar, target, aimingTarget, forceUse, false, false) != null)
				continue;
			if(activeChar.isNpc() && target.isNpc())
				continue;
			if(target.inObserverMode())
				continue;

			targets.add(target);

			++count;

			if(limit > 0)
			{
				if(count >= limit)
					break;
			}
			else
			{
				if(isOffensive() && count >= 20 && !activeChar.isRaid())
					break;
			}
		}
	}

	public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster)
	{
		this.getEffects(effector, effected, calcChance, applyOnCaster, false);
	}

	public final void getEffects(final Creature effector, final Creature effected, final boolean calcChance, final boolean applyOnCaster, final boolean skillReflected)
	{
		if(isPassive() || !hasEffects() || effector == null || effected == null)
			return;

		if((effected.isEffectImmune() || effected.isInvul()) && (effector != effected || isOffensive()))
			return;

		if(effected.isDoor() || effected.isAlikeDead())
			return;

		if(effected.isBlockBuff() && effector != effected && effector.isPlayable() && Config.BlockBuffList.contains(_id) && (!effected.isInOlympiadMode() || !Config.NoBlockBuffInOly.contains(_id)))
			return;

		ThreadPoolManager.getInstance().execute(new Runnable(){
			@Override
			public void run()
			{
				boolean success = false;
				boolean skillMastery = false;
				final int sps = effector.getChargedSpiritShot();
				if(effector.getSkillMastery(getId()) == 2)
				{
					skillMastery = true;
					effector.removeSkillMastery(getId());
				}

				for(final EffectTemplate et : getEffectTemplates()) {
					if(applyOnCaster != et._applyOnCaster)
						continue;

					if(et._counter == 0)
						continue;

					Creature target = et._applyOnCaster || et._isReflectable && skillReflected ? effector : effected;
					if(target.isAlikeDead())
						continue;

					if(target.isRaid() && et.getEffectType().isRaidImmune())
						continue;

					if(effector != effected && et.getPeriod() > 0L && ((effected.isBuffImmune() && !isOffensive()) || (effected.isDebuffImmune()  && isOffensive())))
						continue;

					if(isBlockedByChar(target, et))
						continue;

					if(et._stackOrder == -1) {
						if(!et._stackType.equals(EffectTemplate.NO_STACK)) {
							for(final Abnormal a : target.getAbnormalList().values()) {
								if (a.getStackType().equalsIgnoreCase(et._stackType))
									continue;
							}
						} else if (target.getAbnormalList().getEffectsBySkillId(getId()) != null)
							break;
					}

					Env env = new Env(effector, target, Skill.this);
					int chance = et.chance(getActivateRate());
					if ((calcChance || chance >= 0) && (!et._applyOnCaster || _id == 81)) {
						env.value = chance;
						if (!Formulas.calcSkillSuccess(env, sps, et.getEffectType().getResistType(), et.getEffectType().getAttributeType(), true, et._immuneResists))
							break;
					}

					if (_isReflectable && et._isReflectable && isOffensive() && target != effector && Rnd.chance(target.calcStat(isMagic() ? Stats.REFLECT_MAGIC_DEBUFF : Stats.REFLECT_PHYSIC_DEBUFF, 0.0, effector, Skill.this)) && !effector.isEffectImmune() && !effector.isInvul()) {
						target = effector;
						env.target = target;
					}

					if (success)
						env.value = Integer.MAX_VALUE;

					Abnormal a2 = et.getEffect(env);
					if (a2 != null) {
						if (chance > 0)
							success = true;
						if (a2.getCount() == 1 && a2.getPeriod() == 0L) {
							if (a2.checkCondition()) {
								a2.onStart();
								a2.onActionTime();
								a2.onExit();
							}
						} else {
							int count = et._counter;
							long period = et.getPeriod();
							if (skillMastery && a2.getEffectType() != EffectType.Stun && a2.getEffectType() != EffectType.Symbol && a2.getEffectType() != EffectType.Cubic)
								if (count > 1)
									count *= 2;
								else
									period *= 2L;
							a2.setCount(count);
							a2.setPeriod(period);
							a2.schedule();
						}
					}
				}

				if(calcChance && showActivate()) {
					if (success)
						effector.sendPacket(new SystemMessage(1595).addSkillName(_displayId, _displayLevel));
					else
						effector.sendPacket(new SystemMessage(1597).addSkillName(_displayId, _displayLevel));
				}
			}
		});
	}

	public boolean isBlockedByChar(final Creature effected, final EffectTemplate et)
	{
		for(FuncTemplate func : et.getAttachedFuncs()) {
			if (func != null && effected.checkBlockedStat(func._stat))
				return true;
		}
		return false;
	}

	public final void attachFunc(final EffectTemplate effect)
	{
		if(_effectTemplates == null)
			_effectTemplates = new EffectTemplate[] { effect };
		else
		{
			final int len = _effectTemplates.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			System.arraycopy(_effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			_effectTemplates = tmp;
		}
	}

	public final void attachFunc(final Condition c)
	{
		_preCondition = ArrayUtils.add(_preCondition, c);
	}

	@Override
	public String toString()
	{
		return _name + "[id=" + _id + ",lvl=" + _level + "]";
	}

	public int getEffectPoint()
	{
		return _effectPoint;
	}

	public NextAction getNextAction()
	{
		return _nextAction;
	}

	public boolean getCorpse()
	{
		return _corpse;
	}

	public int getDelayedEffect()
	{
		return _delayedEffect;
	}

	public int reuseCode()
	{
		return reuseCode;
	}

	public final Func[] getStatFuncs()
	{
		return getStatFuncs(this);
	}

	@Override
	public int hashCode()
	{
		return hashCode;
	}

	@Override
	public boolean equals(final Object obj)
	{
		return this == obj || obj != null && this.getClass() == obj.getClass() && hashCode() == obj.hashCode();
	}

	public EffectTemplate[] getEffectTemplates()
	{
		return _effectTemplates;
	}

	public boolean hasEffects()
	{
		return _effectTemplates != null && _effectTemplates.length > 0;
	}

	public boolean isSaveable()
	{
		return _isSaveable;
	}

	public boolean showActivate()
	{
		return _showActivate;
	}

	public boolean isValidateable()
	{
		return _isValidateable;
	}

	public int getForceId()
	{
		return _forceId;
	}

	public int getChance()
	{
		return _chance;
	}

	public int getSymbolId()
	{
		return _symbolId;
	}

	public AddedSkill[] getAddedSkills()
	{
		return _addedSkills;
	}

	public Skill getFirstAddedSkill()
	{
		if(_addedSkills.length == 0)
			return null;
		return _addedSkills[0].getSkill();
	}

	public int getNpcId()
	{
		return _npcId;
	}

	public int getNegatePower()
	{
		return _negatePower;
	}

	public int getNumCharges()
	{
		return _numCharges;
	}

	public int getCondCharges()
	{
		return _condCharges;
	}

	public int getMatak()
	{
		return _matak;
	}

	public boolean isUsingWhileCasting()
	{
		return _isUsingWhileCasting;
	}

	public boolean isBehind()
	{
		return _behind;
	}

	public boolean isHideStartMessage()
	{
		return _hideStartMessage;
	}

	public boolean isHideUseMessage()
	{
		return _hideUseMessage;
	}

	public double getSimpleDamage(final Creature attacker, final Creature target)
	{
		if(isMagic())
		{
			final double mAtk = attacker.getMAtk(target, this);
			final double mdef = target.getMDef(null, this);
			final double power = this.getPower();
			final int sps = attacker.getChargedSpiritShot() > 0 && isSSPossible() ? attacker.getChargedSpiritShot() * 2 : 1;
			return 91.0 * power * Math.sqrt(sps * mAtk) / mdef;
		}
		final double pAtk = attacker.getPAtk(target);
		final double pdef = target.getPDef(attacker);
		final double power = this.getPower();
		final int ss = attacker.getChargedSoulShot() && isSSPossible() ? 2 : 1;
		return ss * (pAtk + power) * 70.0 / pdef;
	}

	public long getReuseForMonsters()
	{
		long min = 1000L;
		switch(_skillType)
		{
			case CANCEL:
			case DEBUFF:
			case PARALYZE:
			case NEGATE_STATS:
			case NEGATE_EFFECTS:
			{
				min = 10000L;
				break;
			}
			case MUTE:
			case ROOT:
			case SLEEP:
			case STUN:
			{
				min = 5000L;
				break;
			}
		}
		return Math.max(Math.max(_hitTime + _coolTime, _reuseDelay), min);
	}

	public double getAbsorbPart()
	{
		return _absorbPart;
	}

	public int getAbsorbPartStatic()
	{
		return _absorbPartStatic;
	}

	public static void broadcastUseAnimation(final Skill skill, final Creature user, final Set<Creature> targets)
	{
		int displayId = 0;
		int displayLevel = 0;
		if(skill.getEffectTemplates() != null)
		{
			displayId = skill.getEffectTemplates()[0]._displayId;
			displayLevel = skill.getEffectTemplates()[0]._displayLevel;
		}
		if(displayId == 0)
			displayId = skill.getDisplayId();
		if(displayLevel == 0)
			displayLevel = skill.getDisplayLevel();
		for(final Creature cha : targets)
			user.broadcastPacket(new MagicSkillUse(user, cha, displayId, displayLevel, 0, 0L));
	}

	public boolean isSSPossible()
	{
		return _isUseSS == Ternary.TRUE || _isUseSS == Ternary.DEFAULT && !_isItemHandler && !isMusic() && isActive() && (getTargetType() != SkillTargetType.TARGET_SELF || isMagic());
	}

	public FlyToLocation.FlyType getFlyType()
	{
		return _flyType;
	}

	public int getFlyRadius()
	{
		return _flyRadius;
	}

	public void setCubicSkill(final boolean value)
	{
		_isCubicSkill = value;
	}

	public boolean isCubicSkill()
	{
		return _isCubicSkill;
	}

	public boolean isBlessNoblesse()
	{
		return _blessNoblesse;
	}

	public boolean isSalvation()
	{
		return _salvation;
	}

	public boolean stopActor()
	{
		return _stopActor;
	}

	public boolean isNotBroadcastable()
	{
		return false;
	}

	public static class AddedSkill
	{
		public static final AddedSkill[] EMPTY_ARRAY;
		public int id;
		public int level;
		private Skill _skill;

		public AddedSkill(final int id, final int level)
		{
			this.id = id;
			this.level = level;
		}

		public Skill getSkill()
		{
			if(_skill == null)
				_skill = SkillTable.getInstance().getInfo(id, level);
			return _skill;
		}

		static
		{
			EMPTY_ARRAY = new AddedSkill[0];
		}
	}

	public enum SkillOpType
	{
		OP_ACTIVE,
		OP_PASSIVE,
		OP_TOGGLE
	}

	public enum Ternary
	{
		TRUE,
		FALSE,
		DEFAULT;
	}

	public enum SkillMagicType
	{
		PHYSIC,
		MAGIC,
		SPECIAL,
		MUSIC;
	}

	public enum TriggerActionType
	{
		ADD,
		ATTACK,
		CRIT,
		OFFENSIVE_PHYSICAL_SKILL_USE,
		OFFENSIVE_MAGICAL_SKILL_USE,
		SUPPORT_MAGICAL_SKILL_USE,
		SUPPORT_SKILL_USE,
		UNDER_ATTACK,
		UNDER_MISSED_ATTACK,
		UNDER_SKILL_ATTACK,
		DIE;
	}

	public enum NextAction
	{
		DEFAULT,
		NONE,
		ATTACK,
		CAST,
		MOVE;
	}

	public enum Element
	{
		FIRE,
		WATER,
		WIND,
		EARTH,
		SACRED,
		UNHOLY,
		NONE;
	}

	public enum SkillTargetType
	{
		TARGET_NONE,
		TARGET_SELF,
		TARGET_ONE,
		TARGET_PARTY,
		TARGET_PARTY_ONE,
		TARGET_PARTY_ONE_OTHER,
		TARGET_CLAN,
		TARGET_CLAN_ONLY,
		TARGET_ALLY,
		TARGET_PET,
		TARGET_OWNER,
		TARGET_ENEMY_PET,
		TARGET_AREA,
		TARGET_AURA,
		TARGET_PET_AURA,
		TARGET_CORPSE,
		TARGET_MULTIFACE,
		TARGET_MULTIFACE_AURA,
		TARGET_TUNNEL,
		TARGET_CORPSE_PLAYER,
		TARGET_ITEM,
		TARGET_AREA_AIM_CORPSE,
		TARGET_UNLOCKABLE,
		TARGET_CHEST,
		TARGET_HOLY,
		TARGET_SIEGE,
		TARGET_FLAGPOLE;
	}

	public enum SkillType
	{
		AGGRESSION(Aggression.class),
		AIEFFECTS(AIeffects.class),
		BALANCE(Balance.class),
		BLEED(Continuous.class),
		BUFF(Continuous.class),
		CALL(Call.class),
		CANCEL(Cancel.class),
		CHARGE(Charge.class),
		CLAN_GATE(ClanGate.class),
		COMBATPOINTHEAL(CombatPointHeal.class),
		COMBATPOINTHEAL_PERCENT(CombatPointHealPercent.class),
		CONT(Toggle.class),
		CPDAM(CPDam.class),
		CPHOT(Continuous.class),
		CRAFT(Craft.class),
		DEATH_PENALTY(DeathPenalty.class),
		DEBUFF(Continuous.class),
		DOT(Continuous.class),
		DRAIN(Drain.class),
		DRAIN_SOUL(DrainSoul.class),
		ENCHANT_ARMOR,
		ENCHANT_WEAPON,
		EXTRACT(Extract.class),
		FATALBLOW(FatalBlow.class),
		FEED_PET,
		FISHING(FishingSkill.class),
		HEAL(Heal.class),
		HEAL_PERCENT(HealPercent.class),
		MANAHEAL_PERCENT(ManaHealPercent.class),
		HOT(Continuous.class),
		LETHAL_SHOT(LethalShot.class),
		LUCK,
		MANAHEAL(ManaHeal.class),
		MDAM(MDam.class),
		MDAM_ELEMENTAL(Elemental.class),
		MDOT(Continuous.class),
		MPHOT(Continuous.class),
		MUTE(Disablers.class),
		NEGATE_EFFECTS(NegateEffects.class),
		NEGATE_SKILLS(NegateSkills.class),
		NEGATE_STATS(NegateStats.class),
		PARALYZE(Disablers.class),
		PASSIVE,
		PDAM(PDam.class),
		PET_SUMMON(PetSummon.class),
		POISON(Continuous.class),
		PUMPING(ReelingPumping.class),
		RECALL(Recall.class),
		REELING(ReelingPumping.class),
		RESURRECT(Resurrect.class),
		ROOT(Disablers.class),
		SEED(Seed.class),
		SIEGEFLAG(SiegeFlag.class),
		SLEEP(Disablers.class),
		SOULSHOT,
		SPIRITSHOT,
		SPHEAL(SPHeal.class),
		SPOIL(Spoil.class),
		SOWING(Sowing.class),
		HARVESTING(Harvesting.class),
		STUN(Disablers.class),
		SUMMON(Summon.class),
		RIDE(Ride.class),
		DESTROY_SUMMON(DestroySummon.class),
		SUMMON_ITEM(SummonItem.class),
		SWEEP(Sweep.class),
		TAKECASTLE(TakeCastle.class),
		TELEPORT_NPC(TeleportNpc.class),
		UNLOCK(Unlock.class),
		WATCHER_GAZE(Continuous.class),
		BEAST_FEED(BeastFeed.class),
		MANADAM(ManaDam.class),
		FORCE_BUFF(Continuous.class),
		DELETE_HATE(DeleteHate.class),
		DELETE_HATE_OF_ME(DeleteHateOfMe.class),
		SHIFT_AGGRESSION(ShiftAggression.class),
		DISCORD(Continuous.class),
		EFFECT(Effect.class),
		PACKAGE_ITEM(PackageItem.class),
		HARDCODED(Effect.class),
		NOTDONE;

		private final Class<? extends Skill> clazz;

		public Skill makeSkill(final StatsSet set)
		{
			try
			{
				final Constructor<? extends Skill> c = clazz.getConstructor(StatsSet.class);
				return c.newInstance(set);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}

		private SkillType()
		{
			clazz = Default.class;
		}

		private SkillType(final Class<? extends Skill> clazz)
		{
			this.clazz = clazz;
		}
	}
}
