package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.MonsterInstance;
import l2s.gameserver.skills.Env;

public class ConditionTargetAggro extends Condition
{
	private final boolean _isAggro;

	public ConditionTargetAggro(final boolean isAggro)
	{
		_isAggro = isAggro;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		final Creature target = env.target;
		if(target == null)
			return false;
		if(target.isMonster())
			return ((MonsterInstance) target).isAggressive() == _isAggro;
		return target.isPlayer() && ((Player) target).getKarma() > 0;
	}
}
