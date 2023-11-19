package l2s.gameserver.listener;

import l2s.gameserver.skills.Calculator;
import l2s.gameserver.skills.Env;
import l2s.gameserver.skills.Stats;

public abstract class StatsChangeListener
{
	public final Stats _stat;
	protected Calculator _calculator;

	public StatsChangeListener(final Stats stat)
	{
		_stat = stat;
	}

	public void setCalculator(final Calculator calculator)
	{
		_calculator = calculator;
	}

	public abstract void statChanged(final Double p0, final double p1, final double p2, final Env p3);
}
