package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.RecipeController;
import l2s.gameserver.model.Player;

public class RequestRecipeShopMakeItem extends L2GameClientPacket
{
	private int _id;
	private int _recipeId;
	private int _unknow;

	@Override
	public void readImpl()
	{
		_id = readD();
		_recipeId = readD();
		_unknow = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isInDuel())
		{
			activeChar.sendActionFailed();
			return;
		}
		final Player manufacturer = (Player) activeChar.getVisibleObject(_id);
		if(manufacturer == null || manufacturer.getPrivateStoreType() != 5 || manufacturer.getDistance(activeChar) > 150.0)
			return;
		RecipeController.getInstance().requestManufactureItem(manufacturer, activeChar, _recipeId);
	}
}
