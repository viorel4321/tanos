package l2s.gameserver.model;

public class ManufactureItem
{
	private int _recipeId;
	private int _cost;

	public ManufactureItem(final int recipeId, final int cost)
	{
		_recipeId = recipeId;
		_cost = cost;
	}

	public int getRecipeId()
	{
		return _recipeId;
	}

	public int getCost()
	{
		return _cost;
	}
}
