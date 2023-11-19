package l2s.gameserver.skills;

import java.util.NoSuchElementException;

public enum Stats
{
	MAX_HP("maxHp", 0.0, Double.POSITIVE_INFINITY, 1.0),
	MAX_MP("maxMp", 0.0, Double.POSITIVE_INFINITY, 1.0),
	MAX_CP("maxCp", 0.0, Double.POSITIVE_INFINITY, 1.0),
	REGENERATE_HP_RATE("regHp"),
	REGENERATE_CP_RATE("regCp"),
	REGENERATE_MP_RATE("regMp"),
	RECHARGE_MP_RATE("gainMp"),
	RUN_SPEED("runSpd"),
	WALK_SPEED("walkSpd"),
	POWER_DEFENCE("pDef"),
	MAGIC_DEFENCE("mDef"),
	POWER_ATTACK("pAtk"),
	MAGIC_ATTACK("mAtk"),
	POWER_ATTACK_SPEED("pAtkSpd"),
	MAGIC_ATTACK_SPEED("mAtkSpd"),
	MAGIC_REUSE_RATE("mReuse"),
	PHYSIC_REUSE_RATE("pReuse"),
	MUSIC_REUSE_RATE("musicReuse"),
	ATK_REUSE("atkReuse"),
	ATK_BASE("atkBaseSpeed"),
	CRITICAL_DAMAGE("cAtk", 0.0, Double.POSITIVE_INFINITY, 100.0),
	CRITICAL_DAMAGE_STATIC("cAtkStatic"),
	EVASION_RATE("rEvas"),
	ACCURACY_COMBAT("accCombat"),
	CRITICAL_BASE("baseCrit", 0.0, Double.POSITIVE_INFINITY, 100.0),
	CRITICAL_RATE("rCrit", 0.0, Double.POSITIVE_INFINITY, 100.0),
	MCRITICAL_RATE("mCritRate"),
	MCRITICAL_DAMAGE("mCritDamage", 0.0, 10.0, 2.5),
	PHYSICAL_DAMAGE("physDamage"),
	PHYSICAL_SKILL_DAMAGE("physSkillDamage"),
	MAGIC_DAMAGE("magicDamage"),
	CAST_INTERRUPT("concentration", 0.0, 100.0),
	SHIELD_DEFENCE("sDef"),
	SHIELD_RATE("rShld", 0.0, 90.0),
	SHIELD_ANGLE("shldAngle", 0.0, 360.0, 60.0),
	POWER_ATTACK_RANGE("pAtkRange", 0.0, 1500.0),
	MAGIC_ATTACK_RANGE("mAtkRange", 0.0, 1500.0),
	POLE_ATTACK_ANGLE("poleAngle", 0.0, 180.0),
	POLE_TARGET_COUNT("poleTargetCount"),
	STAT_STR("STR", 1.0, 99.0),
	STAT_CON("CON", 1.0, 99.0),
	STAT_DEX("DEX", 1.0, 99.0),
	STAT_INT("INT", 1.0, 99.0),
	STAT_WIT("WIT", 1.0, 99.0),
	STAT_MEN("MEN", 1.0, 99.0),
	BREATH("breath"),
	FALL_DAMAGE("fall"),
	FALL_SAFE("fallSafe"),
	EXP_LOST("expLost"),
	BLEED_RESIST("bleedResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	POISON_RESIST("poisonResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	STUN_RESIST("stunResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	ROOT_RESIST("rootResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	MENTAL_RESIST("mentalResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	SLEEP_RESIST("sleepResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	PARALYZE_RESIST("paralyzeResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	CANCEL_RESIST("cancelResist", -200.0, 300.0),
	DEBUFF_RESIST("debuffResist", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY),
	DEATH_VULNERABILITY("deathVuln", 10.0, 190.0, 100.0),
	BLEED_POWER("bleedPower", -200.0, 200.0),
	POISON_POWER("poisonPower", -200.0, 200.0),
	STUN_POWER("stunPower", -200.0, 200.0),
	ROOT_POWER("rootPower", -200.0, 200.0),
	MENTAL_POWER("mentalPower", -200.0, 200.0),
	SLEEP_POWER("sleepPower", -200.0, 200.0),
	PARALYZE_POWER("paralyzePower", -200.0, 200.0),
	CANCEL_POWER("cancelPower", -200.0, 200.0),
	DEBUFF_POWER("debuffPower", -200.0, 200.0),
	FATALBLOW_RATE("blowRate"),
	FIRE_RECEPTIVE("fireRcpt"),
	WIND_RECEPTIVE("windRcpt"),
	WATER_RECEPTIVE("waterRcpt"),
	EARTH_RECEPTIVE("earthRcpt"),
	UNHOLY_RECEPTIVE("unholyRcpt"),
	SACRED_RECEPTIVE("sacredRcpt"),
	ATTACK_ELEMENT_FIRE("attackFire"),
	ATTACK_ELEMENT_WATER("attackWater"),
	ATTACK_ELEMENT_WIND("attackWind"),
	ATTACK_ELEMENT_EARTH("attackEarth"),
	ATTACK_ELEMENT_SACRED("attackSacred"),
	ATTACK_ELEMENT_UNHOLY("attackUnholy"),
	SWORD_WPN_RECEPTIVE("swordWpnRcpt"),
	DUAL_WPN_RECEPTIVE("dualWpnRcpt"),
	BLUNT_WPN_RECEPTIVE("bluntWpnRcpt"),
	DAGGER_WPN_RECEPTIVE("daggerWpnRcpt"),
	BOW_WPN_RECEPTIVE("bowWpnRcpt"),
	POLE_WPN_RECEPTIVE("poleWpnRcpt"),
	FIST_WPN_RECEPTIVE("fistWpnRcpt"),
	REFLECT_DAMAGE_PERCENT("reflectDam"),
	ABSORB_DAMAGE_PERCENT("absorbDam"),
	ABSORB_DAMAGEMP_PERCENT("absorbDamMp"),
	TRANSFER_DAMAGE_PERCENT("transferDam"),
	REFLECT_PHYSIC_SKILL("reflectPhysicSkill"),
	REFLECT_MAGIC_SKILL("reflectMagicSkill"),
	REFLECT_PHYSIC_DEBUFF("reflectPhysicDebuff"),
	REFLECT_MAGIC_DEBUFF("reflectMagicDebuff"),
	PSKILL_EVASION("pSkillEvasion"),
	COUNTER_ATTACK("counterAttack"),
	CANCEL_TARGET("cancelTarget"),
	BLESS_NOBLESSE("blessNoblesse"),
	SALVATION("salvation"),
	HEAL_EFFECTIVNESS("hpEff"),
	HEAL_POWER("healPower"),
	MANAHEAL_EFFECTIVNESS("mpEff"),
	MP_MAGIC_SKILL_CONSUME("mpConsum"),
	MP_PHYSICAL_SKILL_CONSUME("mpConsumePhysical"),
	MP_DANCE_SKILL_CONSUME("mpDanceConsume"),
	MP_USE_BOW("cheapShot"),
	MP_USE_BOW_CHANCE("cheapShotChance"),
	SS_USE_BOW("miser"),
	SS_USE_BOW_CHANCE("miserChance"),
	ACTIVATE_RATE("activateRate"),
	SKILL_MASTERY("skillMastery"),
	MAX_LOAD("maxLoad"),
	INVENTORY_LIMIT("inventoryLimit"),
	STORAGE_LIMIT("storageLimit"),
	TRADE_LIMIT("tradeLimit"),
	COMMON_RECIPE_LIMIT("CommonRecipeLimit"),
	DWARVEN_RECIPE_LIMIT("DwarvenRecipeLimit"),
	BUFF_LIMIT("buffLimit"),
	CUBICS_LIMIT("cubicsLimit", 0.0, 3.0, 1.0),
	GRADE_EXPERTISE_LEVEL("gradeExpertiseLevel"),
	EXP("ExpMultiplier"),
	SP("SpMultiplier"),
	DROP("DropMultiplier"),
	SPOIL("SpoilMultiplier");

	public static final int NUM_STATS;
	private String _value;
	private double _min;
	private double _max;
	private double _init;

	public String getValue()
	{
		return _value;
	}

	public double getInit()
	{
		return _init;
	}

	private Stats(final String s)
	{
		this(s, 0.0, Double.POSITIVE_INFINITY, 0.0);
	}

	private Stats(final String s, final double min, final double max)
	{
		this(s, min, max, 0.0);
	}

	private Stats(final String s, final double min, final double max, final double init)
	{
		_value = s;
		_min = min;
		_max = max;
		_init = init;
	}

	public double validate(final double val)
	{
		if(val < _min)
			return _min;
		if(val > _max)
			return _max;
		return val;
	}

	public static Stats valueOfXml(final String name)
	{
		for(final Stats s : values())
			if(s.getValue().equals(name))
				return s;
		throw new NoSuchElementException("Unknown name '" + name + "' for enum BaseStats");
	}

	@Override
	public String toString()
	{
		return _value;
	}

	static
	{
		NUM_STATS = values().length;
	}
}
