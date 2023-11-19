package l2s.gameserver.skills;

import java.util.Arrays;

import l2s.commons.lang.ArrayUtils;
import l2s.gameserver.model.Creature;
import l2s.gameserver.skills.funcs.Func;
import l2s.gameserver.skills.funcs.FuncOwner;

public final class Calculator
{
	private Func[] _functions;
	public final Stats _stat;
	public final Creature _character;

	public Calculator(final Stats stat, final Creature character)
	{
		_stat = stat;
		_character = character;
		_functions = Func.EMPTY_FUNC_ARRAY;
	}

	public int size()
	{
		return _functions.length;
	}

	public void addFunc(final Func f)
	{
		_functions = ArrayUtils.add(_functions, f);
		Arrays.sort(_functions);
	}

	public void removeFunc(final Func f)
	{
		_functions = ArrayUtils.remove(_functions, f);
		if(_functions.length == 0)
			_functions = Func.EMPTY_FUNC_ARRAY;
		else
			Arrays.sort(_functions);
	}

	public void removeOwner(final Object owner)
	{
		final Func[] functions;
		final Func[] tmp = functions = _functions;
		for(final Func element : functions)
			if(element.owner == owner)
				removeFunc(element);
	}

	public void calc(final Env env)
	{
		final Func[] funcs = _functions;
		boolean overrideLimits = false;
		for(final Func func : funcs)
		{
			if(func != null)
			{
				if(func.owner instanceof FuncOwner)
				{
					if(!((FuncOwner) func.owner).isFuncEnabled())
						break;
					if(((FuncOwner) func.owner).overrideLimits())
						overrideLimits = true;
				}
				if(func.getCondition() == null || func.getCondition().test(env))
					func.calc(env);
			}
		}
		if(!overrideLimits)
			env.value = _stat.validate(env.value);
	}

	public Func[] getFunctions()
	{
		return _functions;
	}
}
