package l2s.gameserver.skills.conditions;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Env;

public class ConditionPlayerEncumbered extends Condition
{
	private final int _maxWeightPercent;
	private final int _maxLoadPercent;

	public ConditionPlayerEncumbered(final int remainingWeightPercent, final int remainingLoadPercent)
	{
		_maxWeightPercent = 100 - remainingWeightPercent;
		_maxLoadPercent = 100 - remainingLoadPercent;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		if(env.character == null || !env.character.isPlayer())
			return false;
		if(((Player) env.character).getWeightPercents() >= _maxWeightPercent || ((Player) env.character).getUsedInventoryPercents() >= _maxLoadPercent)
		{
			env.character.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
			return false;
		}
		return true;
	}
}
