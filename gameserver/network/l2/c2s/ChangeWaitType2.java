package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class ChangeWaitType2 extends L2GameClientPacket
{
	private boolean _typeStand;

	@Override
	public void readImpl()
	{
		_typeStand = readD() == 1;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_typeStand)
			activeChar.standUp();
		else
			activeChar.sitDown(0);
	}
}
