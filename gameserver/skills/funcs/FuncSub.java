package l2s.gameserver.skills.funcs;

import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public class FuncSub extends Func
{
	public FuncSub(final Stats stat, final int order, final Object owner, final double value)
	{
		super(stat, order, owner, value);
	}

	@Override
	public void calc(final Env env)
	{
		if(cond == null || cond.test(env))
			env.value -= value;
	}
}
