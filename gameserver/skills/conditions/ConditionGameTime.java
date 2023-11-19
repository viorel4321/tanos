package l2s.gameserver.skills.conditions;

import l2s.gameserver.GameTimeController;
import l2s.gameserver.skills.Env;

public class ConditionGameTime extends Condition
{
	private final CheckGameTime _check;
	private final boolean _required;

	public ConditionGameTime(final CheckGameTime check, final boolean required)
	{
		_check = check;
		_required = required;
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		switch(_check)
		{
			case NIGHT:
			{
				return GameTimeController.getInstance().isNowNight() == _required;
			}
			default:
			{
				return !_required;
			}
		}
	}

	public enum CheckGameTime
	{
		NIGHT;
	}
}
