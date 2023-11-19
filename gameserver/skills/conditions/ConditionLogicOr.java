package l2s.gameserver.skills.conditions;

import l2s.gameserver.skills.Env;

public class ConditionLogicOr extends Condition
{
	private static final Condition[] emptyConditions;
	public Condition[] _conditions;

	public ConditionLogicOr()
	{
		_conditions = ConditionLogicOr.emptyConditions;
	}

	public void add(final Condition condition)
	{
		if(condition == null)
			return;
		if(getListener() != null)
			condition.setListener(this);
		final int len = _conditions.length;
		final Condition[] tmp = new Condition[len + 1];
		System.arraycopy(_conditions, 0, tmp, 0, len);
		tmp[len] = condition;
		_conditions = tmp;
	}

	@Override
	public void setListener(final ConditionListener listener)
	{
		if(listener != null)
			for(final Condition c : _conditions)
				c.setListener(this);
		else
			for(final Condition c : _conditions)
				c.setListener(null);
		super.setListener(listener);
	}

	@Override
	protected boolean testImpl(final Env env)
	{
		for(final Condition c : _conditions)
			if(c.test(env))
				return true;
		return false;
	}

	static
	{
		emptyConditions = new Condition[0];
	}
}
