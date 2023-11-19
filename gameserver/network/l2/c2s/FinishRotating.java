package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.StopRotation;

public class FinishRotating extends L2GameClientPacket
{
	private int _degree;
	private int _unknown;

	@Override
	public void readImpl()
	{
		_degree = readD();
		_unknown = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.broadcastPacket(new StopRotation(activeChar.getObjectId(), _degree, 0));
	}
}
