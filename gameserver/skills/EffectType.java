package l2s.gameserver.skills;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.effects.*;

public enum EffectType
{
	AntiSummon(EffectAntiSummon.class, (AbnormalEffect) null, true),
	BattleForce(EffectForce.class, (AbnormalEffect) null, false),
	Betray(EffectBetray.class, (AbnormalEffect) null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	BlockBuff(EffectBlockBuff.class, (AbnormalEffect) null, true),
	BlockStat(EffectBlockStat.class, (AbnormalEffect) null, true),
	Buff(EffectBuff.class, (AbnormalEffect) null, false),
	Charge(EffectCharge.class, (AbnormalEffect) null, false),
	CharmOfCourage(EffectCharmOfCourage.class, (AbnormalEffect) null, true),
	CombatPointHealOverTime(EffectCombatPointHealOverTime.class, (AbnormalEffect) null, true),
	CPHealPercent(EffectCPHealPercent.class, (AbnormalEffect) null, true),
	Cubic(EffectCubic.class, (AbnormalEffect) null, true),
	DamOverTime(EffectDamOverTime.class, (AbnormalEffect) null, false),
	DamOverTimeLethal(EffectDamOverTimeLethal.class, (AbnormalEffect) null, false),
	DebuffImmunity(EffectDebuffImmunity.class, (AbnormalEffect) null, true),
	DestroySummon(EffectDestroySummon.class, (AbnormalEffect) null, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Discord(EffectDiscord.class, AbnormalEffect.CONFUSED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	DispelDisease(EffectDispelDisease.class, (AbnormalEffect) null, true),
	EffectImmunity(EffectEffectImmunity.class, (AbnormalEffect) null, true),
	FakeDeath(EffectFakeDeath.class, (AbnormalEffect) null, true),
	Fear(EffectFear.class, AbnormalEffect.AFFRAID, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Grow(EffectGrow.class, AbnormalEffect.GROW, false),
	Hate(EffectHate.class, (AbnormalEffect) null, false),
	Heal(EffectHeal.class, (AbnormalEffect) null, false),
	HealOverTime(EffectHealOverTime.class, (AbnormalEffect) null, false),
	HealPercent(EffectHealPercent.class, (AbnormalEffect) null, false),
	ImobileBuff(EffectImobileBuff.class, (AbnormalEffect) null, true),
	Interrupt(EffectInterrupt.class, (AbnormalEffect) null, true),
	Invisible(EffectInvisible.class, (AbnormalEffect) null, false),
	Invulnerable(EffectInvulnerable.class, (AbnormalEffect) null, false),
	LDManaDamOverTime(EffectLDManaDamOverTime.class, (AbnormalEffect) null, true),
	ManaDamOverTime(EffectManaDamOverTime.class, (AbnormalEffect) null, true),
	ManaHeal(EffectManaHeal.class, (AbnormalEffect) null, false),
	ManaHealOverTime(EffectManaHealOverTime.class, (AbnormalEffect) null, false),
	ManaHealPercent(EffectManaHealPercent.class, (AbnormalEffect) null, false),
	Meditation(EffectMeditation.class, AbnormalEffect.FLOATING_ROOT, false),
	Mute(EffectMute.class, AbnormalEffect.MUTED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MuteAll(EffectMuteAll.class, AbnormalEffect.MUTED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	MutePhisycal(EffectMutePhisycal.class, AbnormalEffect.MUTED, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	NegateMusic(EffectNegateMusic.class, (AbnormalEffect) null, false),
	Paralyze(EffectParalyze.class, AbnormalEffect.HOLD_1, Stats.PARALYZE_RESIST, Stats.PARALYZE_POWER, true),
	Petrification(EffectPetrification.class, AbnormalEffect.HOLD_2, Stats.PARALYZE_RESIST, Stats.PARALYZE_POWER, true),
	RandomHate(EffectRandomHate.class, (AbnormalEffect) null, true),
	Relax(EffectRelax.class, (AbnormalEffect) null, true),
	RemoveTarget(EffectRemoveTarget.class, AbnormalEffect.NULL, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	Root(EffectRoot.class, AbnormalEffect.ROOT, Stats.ROOT_RESIST, Stats.ROOT_POWER, true),
	Seed(EffectSeed.class, (AbnormalEffect) null, true),
	SilentMove(EffectSilentMove.class, AbnormalEffect.STEALTH, true),
	Sleep(EffectSleep.class, AbnormalEffect.SLEEP, Stats.SLEEP_RESIST, Stats.SLEEP_POWER, true),
	SpellForce(EffectForce.class, (AbnormalEffect) null, false),
	Stun(EffectStun.class, AbnormalEffect.STUN, Stats.STUN_RESIST, Stats.STUN_POWER, true),
	Symbol(EffectSymbol.class, (AbnormalEffect) null, false),
	Turner(EffectTurner.class, AbnormalEffect.NULL, Stats.MENTAL_RESIST, Stats.MENTAL_POWER, true),
	UnAggro(EffectUnAggro.class, (AbnormalEffect) null, true),
	Poison(EffectDamOverTime.class, (AbnormalEffect) null, Stats.POISON_RESIST, Stats.POISON_POWER, false),
	PoisonLethal(EffectDamOverTimeLethal.class, (AbnormalEffect) null, Stats.POISON_RESIST, Stats.POISON_POWER, false),
	Bleed(EffectDamOverTime.class, (AbnormalEffect) null, Stats.BLEED_RESIST, Stats.BLEED_POWER, false),
	Debuff(EffectBuff.class, (AbnormalEffect) null, false),
	WatcherGaze(EffectBuff.class, (AbnormalEffect) null, false);

	private final Constructor<? extends Abnormal> _constructor;
	private final AbnormalEffect _abnormal;
	private final Stats _resistType;
	private final Stats _attributeType;
	private final boolean _isRaidImmune;

	private EffectType(final Class<? extends Abnormal> clazz, final AbnormalEffect abnormal, final boolean isRaidImmune)
	{
		this(clazz, abnormal, null, null, isRaidImmune);
	}

	private EffectType(final Class<? extends Abnormal> clazz, final AbnormalEffect abnormal, final Stats resistType, final Stats attributeType, final boolean isRaidImmune)
	{
		try
		{
			_constructor = clazz.getConstructor(Env.class, EffectTemplate.class);
		}
		catch(NoSuchMethodException e)
		{
			throw new Error(e);
		}
		_abnormal = abnormal;
		_resistType = resistType;
		_attributeType = attributeType;
		_isRaidImmune = isRaidImmune;
	}

	public AbnormalEffect getAbnormal()
	{
		return _abnormal;
	}

	public Stats getResistType()
	{
		return _resistType;
	}

	public Stats getAttributeType()
	{
		return _attributeType;
	}

	public boolean isRaidImmune()
	{
		return _isRaidImmune;
	}

	public Abnormal makeEffect(final Env env, final EffectTemplate template) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		return _constructor.newInstance(env, template);
	}
}
