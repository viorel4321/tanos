package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestOlympiadObserverEnd extends L2GameClientPacket
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
		if(activeChar.inObserverMode() && activeChar.getOlympiadObserveId() != -1)
			activeChar.leaveOlympiadObserverMode();
	}
}
