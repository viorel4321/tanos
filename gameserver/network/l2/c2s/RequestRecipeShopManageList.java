package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.ManufactureList;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.RecipeShopManageList;

public class RequestRecipeShopManageList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

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
		if(activeChar.isAlikeDead())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getCreateList() == null)
			activeChar.setCreateList(new ManufactureList());
		if(activeChar.getPrivateStoreType() == 5)
		{
			activeChar.sendPacket(new RecipeShopManageList(activeChar, true));
			activeChar.standUp();
		}
	}
}
