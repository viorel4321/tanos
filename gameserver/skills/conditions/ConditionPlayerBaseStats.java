package l2s.gameserver.skills.conditions;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.skills.Env;

class ConditionPlayerBaseStats extends Condition
{
	private final BaseStat _stat;
	private final byte _value;

	ConditionPlayerBaseStats(final Creature player, final BaseStat stat, final byte value)
	{
		_stat = stat;
		_value = value;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		if(!env.character.isPlayer())
			return false;
		final Player player = (Player) env.character;
		switch(_stat)
		{
			case Int:
			{
				return player.getINT() >= _value;
			}
			case Str:
			{
				return player.getSTR() >= _value;
			}
			case Con:
			{
				return player.getCON() >= _value;
			}
			case Dex:
			{
				return player.getDEX() >= _value;
			}
			case Men:
			{
				return player.getMEN() >= _value;
			}
			case Wit:
			{
				return player.getWIT() >= _value;
			}
			default:
			{
				return false;
			}
		}
	}
}
