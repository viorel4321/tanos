package l2s.gameserver.ai;

import l2s.gameserver.model.instances.NpcInstance;

public class SiegeGuardRanger extends SiegeGuard
{
	public SiegeGuardRanger(final NpcInstance actor)
	{
		super(actor);
	}

	@Override
	public boolean getIsMobile()
	{
		return false;
	}

	@Override
	protected boolean createNewTask()
	{
		return defaultFightTask();
	}

	@Override
	public int getRatePHYS()
	{
		return 25;
	}

	@Override
	public int getRateDOT()
	{
		return 50;
	}

	@Override
	public int getRateDEBUFF()
	{
		return 25;
	}

	@Override
	public int getRateDAM()
	{
		return 75;
	}

	@Override
	public int getRateSTUN()
	{
		return 75;
	}

	@Override
	public int getRateBUFF()
	{
		return 5;
	}

	@Override
	public int getRateHEAL()
	{
		return 50;
	}
}
