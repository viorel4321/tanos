package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class ChangeMoveType2 extends L2GameClientPacket
{
	private boolean _typeRun;

	@Override
	public void readImpl()
	{
		_typeRun = readD() == 1;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_typeRun)
			activeChar.setRunning();
		else
			activeChar.setWalking();
	}
}
