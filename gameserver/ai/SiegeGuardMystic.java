package l2s.gameserver.ai;

import l2s.gameserver.model.instances.NpcInstance;

public class SiegeGuardMystic extends SiegeGuard
{
	public SiegeGuardMystic(final NpcInstance actor)
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
		return _damSkills.length == 0 ? 25 : 0;
	}

	@Override
	public int getRateDOT()
	{
		return 25;
	}

	@Override
	public int getRateDEBUFF()
	{
		return 25;
	}

	@Override
	public int getRateDAM()
	{
		return 100;
	}

	@Override
	public int getRateSTUN()
	{
		return 10;
	}

	@Override
	public int getRateBUFF()
	{
		return 10;
	}

	@Override
	public int getRateHEAL()
	{
		return 20;
	}
}
