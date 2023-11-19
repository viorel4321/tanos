package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.skills.EffectType;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.effects.EffectForce;

public class ConditionForceBuff extends Condition
{
	private int _battleForces;
	private int _spellForces;

	public ConditionForceBuff(final int[] forces)
	{
		_battleForces = forces[0];
		_spellForces = forces[1];
	}

	public ConditionForceBuff(final int battle, final int spell)
	{
		_battleForces = battle;
		_spellForces = spell;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		final Creature character = env.character;
		if(character == null)
			return false;
		if(character.getAccessLevel() >= 100)
			return true;
		if(_battleForces > 0)
		{
			final Abnormal battleForce = character.getAbnormalList().getEffectByType(EffectType.BattleForce);
			if(battleForce == null || ((EffectForce) battleForce).forces < _battleForces)
				return false;
		}
		if(_spellForces > 0)
		{
			final Abnormal spellForce = character.getAbnormalList().getEffectByType(EffectType.SpellForce);
			if(spellForce == null || ((EffectForce) spellForce).forces < _spellForces)
				return false;
		}
		return true;
	}
}
