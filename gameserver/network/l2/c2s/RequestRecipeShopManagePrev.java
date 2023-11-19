package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.RecipeShopSellList;

public class RequestRecipeShopManagePrev extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || activeChar.getTarget() == null)
			return;
		if(activeChar.isInDuel())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.isAlikeDead())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(!activeChar.getTarget().isPlayer())
		{
			activeChar.sendActionFailed();
			return;
		}
		final Player target = (Player) activeChar.getTarget();
		activeChar.sendPacket(new RecipeShopSellList(activeChar, target));
	}
}
