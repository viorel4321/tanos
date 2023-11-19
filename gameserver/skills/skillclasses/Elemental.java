package l2s.gameserver.skills.skillclasses;

import java.util.Set;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.skills.effects.EffectSeed;
import l2s.gameserver.templates.StatsSet;

public class Elemental extends Skill
{
	private Integer[] seed;
	private Integer[] seedcount;
	private final boolean seedAny;
	private int seedCount;

	public Elemental(final StatsSet set)
	{
		super(set);
		seed = new Integer[3];
		seedcount = new Integer[3];
		seedAny = set.getBool("seed_any", false);
		if(seedAny)
		{
			seedCount = set.getInteger("seedCount");
			return;
		}
		for(int i = 0; i < 3; ++i)
		{
			seed[i] = set.getInteger(("seed" + (i + 1)), 0);
			seedcount[i] = set.getInteger(("seedcount" + (i + 1)), 1);
		}
	}

	@Override
	public boolean checkCondition(Creature activeChar, Creature target, boolean forceUse, boolean dontMove, boolean first, boolean sendMsg, boolean trigger)
	{
		if(seedAny)
		{
			int count = 0;
			for(final Abnormal e : activeChar.getAbnormalList().values())
				if(e instanceof EffectSeed)
					count += ((EffectSeed) e).getPower();
			if(count < seedCount)
				return false;
		}
		else
			for(int i = 0; i < 3; ++i)
				if(seed[i] != 0)
				{
					final Abnormal eff = activeChar.getAbnormalList().getEffectByIndexAndType(seed[i], EffectType.Seed);
					if(eff == null || ((EffectSeed) eff).getPower() < seedcount[i])
						return false;
				}
		return super.checkCondition(activeChar, target, forceUse, dontMove, first, sendMsg, trigger);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		final int sps = isSSPossible() ? activeChar.getChargedSpiritShot() : 0;
		if(sps != 0)
			activeChar.unChargeShots(true);
		for(final Creature target : targets)
			if(target != null)
			{
				if(target.isDead())
					continue;
				final boolean reflected = target.checkReflectSkill(activeChar, this);
				final Creature realTarget = reflected ? activeChar : target;
				double damage = Formulas.calcMagicDam(activeChar, realTarget, this, sps);
				if(damage < 1.0)
					damage = 1.0;
				realTarget.reduceCurrentHp(damage, activeChar, this, 0, false, true, true, false, false, false, false, true);
				this.getEffects(activeChar, target, getActivateRate() > 0, false, reflected);
			}
		activeChar.getAbnormalList().stop(EffectType.Seed);
	}
}
