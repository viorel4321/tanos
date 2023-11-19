package l2s.gameserver.model;

public class RecipeList
{
	private Recipe[] _recipes;
	private int _id;
	private int _level;
	private int _recipeId;
	private String _recipeName;
	private int _successRate;
	private int _mpCost;
	private int _itemId;
	private int _count;
	private boolean _isdwarvencraft;

	public RecipeList(final int id, final int level, final int recipeId, final String recipeName, final int successRate, final int mpCost, final int itemId, final int count, final boolean isdwarvencraft)
	{
		_id = id;
		_recipes = new Recipe[0];
		_level = level;
		_recipeId = recipeId;
		_recipeName = recipeName;
		_successRate = successRate;
		_mpCost = mpCost;
		_itemId = itemId;
		_count = count;
		_isdwarvencraft = isdwarvencraft;
	}

	public void addRecipe(final Recipe recipe)
	{
		final int len = _recipes.length;
		final Recipe[] tmp = new Recipe[len + 1];
		System.arraycopy(_recipes, 0, tmp, 0, len);
		tmp[len] = recipe;
		_recipes = tmp;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getRecipeId()
	{
		return _recipeId;
	}

	public String getRecipeName()
	{
		return _recipeName;
	}

	public int getSuccessRate()
	{
		return _successRate;
	}

	public int getMpCost()
	{
		return _mpCost;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getCount()
	{
		return _count;
	}

	public Recipe[] getRecipes()
	{
		return _recipes;
	}

	public boolean isDwarvenRecipe()
	{
		return _isdwarvencraft;
	}
}
