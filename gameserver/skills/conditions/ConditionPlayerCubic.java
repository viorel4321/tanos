package l2s.gameserver.skills.conditions;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public class ConditionPlayerCubic extends Condition
{
	private int _id;

	public ConditionPlayerCubic(final int id)
	{
		_id = id;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		if(env.target == null || !env.target.isPlayer())
			return false;
		final Player targetPlayer = (Player) env.target;
		if(targetPlayer.getCubic(_id) != null)
			return true;
		final int size = (int) targetPlayer.calcStat(Stats.CUBICS_LIMIT, 1.0);
		if(targetPlayer.getCubics().size() >= size)
		{
			if(env.character == targetPlayer)
				targetPlayer.sendPacket(Msg.CUBIC_SUMMONING_FAILED);
			return false;
		}
		return true;
	}
}
