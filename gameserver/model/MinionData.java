package l2s.gameserver.model;

public class MinionData
{
	private int _minionId;
	private int _minionAmountMin;
	private int _minionAmountMax;

	public void setMinionId(final int id)
	{
		_minionId = id;
	}

	public int getMinionId()
	{
		return _minionId;
	}

	public void setAmount(final int min, final int max)
	{
		_minionAmountMin = min;
		_minionAmountMax = max;
	}

	public int getAmountMin()
	{
		return _minionAmountMin;
	}

	public int getAmountMax()
	{
		return _minionAmountMax;
	}
}
