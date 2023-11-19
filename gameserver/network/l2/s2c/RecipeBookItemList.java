package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.RecipeList;

public class RecipeBookItemList extends L2GameServerPacket
{
	private Collection<RecipeList> _recipes;
	private final boolean _isDwarvenCraft;
	private final int _CurMP;

	public RecipeBookItemList(final boolean isDwarvenCraft, final int CurMP)
	{
		_isDwarvenCraft = isDwarvenCraft;
		_CurMP = CurMP;
	}

	public void setRecipes(final Collection<RecipeList> recipeBook)
	{
		_recipes = recipeBook;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(214);
		writeD(_isDwarvenCraft ? 0 : 1);
		writeD(_CurMP);
		if(_recipes == null)
			writeD(0);
		else
		{
			writeD(_recipes.size());
			for(final RecipeList recipe : _recipes)
			{
				writeD(recipe.getId());
				writeD(1);
			}
		}
	}
}
