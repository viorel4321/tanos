package l2s.gameserver.skills.funcs;

import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;
import l2s.gameserver.skills.conditions.Condition;

public abstract class Func implements Comparable<Func>
{
	public static final Func[] EMPTY_FUNC_ARRAY;
	public final Stats stat;
	public final int order;
	public final Object owner;
	public final double value;
	protected Condition cond;

	public Func(final Stats stat, final int order, final Object owner)
	{
		this(stat, order, owner, 0.0);
	}

	public Func(final Stats stat, final int order, final Object owner, final double value)
	{
		this.stat = stat;
		this.order = order;
		this.owner = owner;
		this.value = value;
	}

	public void setCondition(final Condition cond)
	{
		this.cond = cond;
	}

	public Condition getCondition()
	{
		return cond;
	}

	public abstract void calc(final Env p0);

	@Override
	public int compareTo(final Func f) throws NullPointerException
	{
		return order - f.order;
	}

	static
	{
		EMPTY_FUNC_ARRAY = new Func[0];
	}
}
