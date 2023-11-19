package l2s.gameserver.skills.funcs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.conditions.Condition;

public final class FuncTemplate
{
	public static final FuncTemplate[] EMPTY_ARRAY = new FuncTemplate[0];

	public Condition _applyCond;
	public Class<?> _func;
	public Constructor<?> _constructor;
	public Stats _stat;
	public int _order;
	public double _value;

	public FuncTemplate(final Condition applyCond, final String func, final Stats stat, final int order, final double value)
	{
		_applyCond = applyCond;
		_stat = stat;
		_order = order;
		_value = value;
		try
		{
			_func = Class.forName("l2s.gameserver.skills.funcs.Func" + func);
		}
		catch(ClassNotFoundException e)
		{
			throw new RuntimeException(e);
		}
		try
		{
			_constructor = _func.getConstructor(Stats.class, Integer.TYPE, Object.class, Double.TYPE);
		}
		catch(NoSuchMethodException e2)
		{
			throw new RuntimeException(e2);
		}
	}

	public Func getFunc(final Object owner)
	{
		try
		{
			final Func f = (Func) _constructor.newInstance(_stat, _order, owner, _value);
			if(_applyCond != null)
				f.setCondition(_applyCond);
			return f;
		}
		catch(IllegalAccessException e)
		{
			e.printStackTrace();
			return null;
		}
		catch(InstantiationException e2)
		{
			e2.printStackTrace();
			return null;
		}
		catch(InvocationTargetException e3)
		{
			e3.printStackTrace();
			return null;
		}
	}
}
