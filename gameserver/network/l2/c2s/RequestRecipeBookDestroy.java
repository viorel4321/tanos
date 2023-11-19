package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.RecipeController;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.RecipeList;
import l2s.gameserver.network.l2.s2c.RecipeBookItemList;

public class RequestRecipeBookDestroy extends L2GameClientPacket
{
	private int _RecipeID;

	@Override
	public void readImpl()
	{
		_RecipeID = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getPrivateStoreType() == 5)
		{
			activeChar.sendPacket(Msg.YOU_MAY_NOT_ALTER_YOUR_RECIPE_BOOK_WHILE_ENGAGED_IN_MANUFACTURING);
			return;
		}
		final RecipeList rp = RecipeController.getInstance().getRecipeList(_RecipeID);
		if(rp == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.unregisterRecipe(_RecipeID);
		final RecipeBookItemList response = new RecipeBookItemList(rp.isDwarvenRecipe(), (int) activeChar.getCurrentMp());
		response.setRecipes(activeChar.getDwarvenRecipeBook());
		activeChar.sendPacket(response);
	}
}
