package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.RecipeController;
import l2s.gameserver.model.Player;

public class RequestRecipeBookOpen extends L2GameClientPacket
{
	private boolean isDwarvenCraft;

	public RequestRecipeBookOpen()
	{
		isDwarvenCraft = true;
	}

	@Override
	public void readImpl()
	{
		if(_buf.hasRemaining())
			isDwarvenCraft = readD() == 0;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		RecipeController.getInstance().requestBookOpen(activeChar, isDwarvenCraft);
	}
}
