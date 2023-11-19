package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.StartRotation;

public class StartRotating extends L2GameClientPacket
{
	private int _degree;
	private int _side;

	@Override
	public void readImpl()
	{
		_degree = readD();
		_side = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.setHeading(_degree);
		activeChar.broadcastPacket(new StartRotation(activeChar.getObjectId(), _degree, _side, 0));
	}
}
