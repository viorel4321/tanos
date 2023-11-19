package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class Appearing extends L2GameClientPacket
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
		if(activeChar.isLogoutStarted())
		{
			activeChar.sendActionFailed();
			return;
		}
		if(activeChar.getObserverMode() == 1 && activeChar.getOlympiadObserveId() == -1)
		{
			activeChar.appearObserverMode();
			return;
		}
		if(activeChar.getObserverMode() == 2)
		{
			activeChar.returnFromObserverMode();
			return;
		}
		if(!activeChar.isTeleporting())
		{
			activeChar.sendActionFailed();
			return;
		}
		activeChar.onTeleported();
	}
}
