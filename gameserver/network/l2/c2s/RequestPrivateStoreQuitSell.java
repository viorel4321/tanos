package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestPrivateStoreQuitSell extends L2GameClientPacket
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
		if(activeChar.getTradeList() != null)
			activeChar.getTradeList().removeAll();
		activeChar.setPrivateStoreType((short) 0);
		activeChar.standUp();
		activeChar.broadcastUserInfo(true);
	}
}
