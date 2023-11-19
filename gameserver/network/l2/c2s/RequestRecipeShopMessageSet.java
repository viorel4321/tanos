package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestRecipeShopMessageSet extends L2GameClientPacket
{
	private String _name;

	@Override
	public void readImpl()
	{
		_name = readS(16);
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null || _name.length() > 16)
			return;
		if(activeChar.isInDuel())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getCreateList() != null)
			activeChar.getCreateList().setStoreName(_name);
	}
}
