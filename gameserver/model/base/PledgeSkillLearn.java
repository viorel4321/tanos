package l2s.gameserver.model.base;

public final class PledgeSkillLearn
{
	private final int _id;
	private final int _level;
	private final int _repCost;
	private final int _baseLvl;
	private final int _itemId;

	public PledgeSkillLearn(final int id, final int lvl, final int baseLvl, final int cost, final int itemId)
	{
		_id = id;
		_level = lvl;
		_baseLvl = baseLvl;
		_repCost = cost;
		_itemId = itemId;
	}

	public int getBaseLevel()
	{
		return _baseLvl;
	}

	public int getId()
	{
		return _id;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getRepCost()
	{
		return _repCost;
	}
}
