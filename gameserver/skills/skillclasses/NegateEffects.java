package l2s.gameserver.skills.skillclasses;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Formulas;
import l2s.gameserver.templates.StatsSet;

public class NegateEffects extends Skill
{
	private Map<EffectType, Integer> _negateEffects;
	private Map<SkillType, Integer> _negateSkillType;
	private Map<String, Integer> _negateStackType;
	private final boolean _onlyPhysical;
	private final boolean _negateDebuffs;

	public NegateEffects(final StatsSet set)
	{
		super(set);
		_negateEffects = new HashMap<EffectType, Integer>();
		_negateSkillType = new HashMap<SkillType, Integer>();
		_negateStackType = new HashMap<String, Integer>();
		final String[] negateEffectsString = set.getString("negateEffects", "").split(";");
		for(int i = 0; i < negateEffectsString.length; ++i)
			if(!negateEffectsString[i].isEmpty())
			{
				final String[] entry = negateEffectsString[i].split(":");
				_negateEffects.put(Enum.valueOf(EffectType.class, entry[0]), entry.length > 1 ? (int) Integer.decode(entry[1]) : Integer.MAX_VALUE);
			}
		final String[] negateStackTypeString = set.getString("negateStackType", "").split(";");
		for(int j = 0; j < negateStackTypeString.length; ++j)
			if(!negateStackTypeString[j].isEmpty())
			{
				final String[] entry2 = negateStackTypeString[j].split(":");
				_negateStackType.put(entry2[0], entry2.length > 1 ? (int) Integer.decode(entry2[1]) : Integer.MAX_VALUE);
			}
		final String[] negateSkillTypeString = set.getString("negateSkillType", "").split(";");
		for(int k = 0; k < negateSkillTypeString.length; ++k)
			if(!negateSkillTypeString[k].isEmpty())
			{
				final String[] entry3 = negateSkillTypeString[k].split(":");
				_negateSkillType.put(Enum.valueOf(SkillType.class, entry3[0]), entry3.length > 1 ? (int) Integer.decode(entry3[1]) : Integer.MAX_VALUE);
			}
		_onlyPhysical = set.getBool("onlyPhysical", false);
		_negateDebuffs = set.getBool("negateDebuffs", true);
	}

	@Override
	public void onEndCast(final Creature activeChar, final Set<Creature> targets)
	{
		for(final Creature target : targets)
			if(target != null)
			{
				if(isOffensive())
				{
					if(target.isInvul())
						continue;
					if(target.isEffectImmune())
						continue;
				}
				if(!_negateDebuffs && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate()))
					activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getDisplayLevel()));
				else
				{
					if(!_negateEffects.isEmpty())
						for(final Map.Entry<EffectType, Integer> e : _negateEffects.entrySet())
							this.negateEffectAtPower(target, e.getKey(), e.getValue());
					if(!_negateStackType.isEmpty())
						for(final Map.Entry<String, Integer> e2 : _negateStackType.entrySet())
							this.negateEffectAtPower(target, e2.getKey(), e2.getValue());
					if(!_negateSkillType.isEmpty())
						for(final Map.Entry<SkillType, Integer> e3 : _negateSkillType.entrySet())
							this.negateEffectAtPower(target, e3.getKey(), e3.getValue());
					this.getEffects(activeChar, target, getActivateRate() > 0, false);
				}
			}
		if(_numCharges > 0)
			activeChar.setIncreasedForce(activeChar.getIncreasedForce() - _numCharges);
		if(isSSPossible())
			activeChar.unChargeShots(isMagic());
	}

	private void negateEffectAtPower(final Creature target, final EffectType type, final int power)
	{
		for(final Abnormal e : target.getAbnormalList().values())
		{
			final Skill skill = e.getSkill();
			if((!_onlyPhysical || !skill.isMagic()) && skill.isCancelable())
			{
				if(skill.isOffensive() && !_negateDebuffs)
					continue;
				if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
					continue;
				if(e.getEffectType() != type || e.getStackOrder() > power)
					continue;
				e.exit();
			}
		}
	}

	private void negateEffectAtPower(final Creature target, final String stackType, final int power)
	{
		for(final Abnormal e : target.getAbnormalList().values())
		{
			final Skill skill = e.getSkill();
			if((!_onlyPhysical || !skill.isMagic()) && skill.isCancelable())
			{
				if(skill.isOffensive() && !_negateDebuffs)
					continue;
				if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
					continue;
				if(!e.checkStackType(stackType) || e.getStackOrder() > power)
					continue;
				e.exit();
			}
		}
	}

	private void negateEffectAtPower(final Creature target, final SkillType type, final int power)
	{
		for(final Abnormal e : target.getAbnormalList().values())
		{
			final Skill skill = e.getSkill();
			if((!_onlyPhysical || !skill.isMagic()) && skill.isCancelable())
			{
				if(skill.isOffensive() && !_negateDebuffs)
					continue;
				if(!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel()))
					continue;
				if(skill.getSkillType() != type || e.getStackOrder() > power)
					continue;
				e.exit();
			}
		}
	}
}
