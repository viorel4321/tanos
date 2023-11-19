package l2s.gameserver.model;

public final class SkillLearn
{
	private final int _id;
	private final int _level;
	private final int _spCost;
	private final int _minLevel;
	private final int _costid;
	private final int _costcount;

	public SkillLearn(final int id, final int lvl, final int minLvl, final int cost, final int costid, final int costcount)
	{
		_id = id;
		_level = lvl;
		_minLevel = minLvl;
		_spCost = cost;
		_costid = costid;
		_costcount = costcount;
	}

	public int getCostCount()
	{
		return _costcount;
	}

	public int getId()
	{
		return _id;
	}

	public int getIdCost()
	{
		return _costid;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public int getSpCost()
	{
		return _spCost;
	}
}
