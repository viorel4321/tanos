package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.RecipeController;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.RecipeList;

public class RecipeItemMakeInfo extends L2GameServerPacket
{
	private int _id;
	private int _status;
	private int _CurMP;
	private int _MaxMP;

	public RecipeItemMakeInfo(final int id, final Player pl, final int status)
	{
		_id = id;
		_status = status;
		_CurMP = (int) pl.getCurrentMp();
		_MaxMP = pl.getMaxMp();
	}

	@Override
	protected final void writeImpl()
	{
		final RecipeList recipeList = RecipeController.getInstance().getRecipeList(_id);
		if(recipeList == null)
			return;
		writeC(215);
		writeD(_id);
		writeD(recipeList.isDwarvenRecipe() ? 0 : 1);
		writeD(_CurMP);
		writeD(_MaxMP);
		writeD(_status);
	}
}
