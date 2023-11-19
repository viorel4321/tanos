package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.RecipeController;
import l2s.gameserver.model.Player;

public class RequestRecipeItemMakeSelf extends L2GameClientPacket
{
	private int _id;

	@Override
	public void readImpl()
	{
		_id = readD();
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
		RecipeController.getInstance().requestMakeItem(activeChar, _id);
	}
}
