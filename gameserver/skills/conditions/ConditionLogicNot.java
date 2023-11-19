package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionLogicNot extends Condition
{
	private final Condition _condition;

	public ConditionLogicNot(final Condition condition)
	{
		_condition = condition;
		if(getListener() != null)
			_condition.setListener(this);
	}

	@Override
	public void setListener(final ConditionListener listener)
	{
		if(listener != null)
			_condition.setListener(this);
		else
			_condition.setListener(null);
		super.setListener(listener);
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		return !_condition.test(env);
	}
}
