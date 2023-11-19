package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class ChangeMoveType extends L2GameServerPacket
{
	public static int WALK;
	public static int RUN;
	private int _chaId;
	private boolean _running;

	public ChangeMoveType(final Creature cha)
	{
		_chaId = cha.getObjectId();
		_running = cha.isRunning();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(46);
		writeD(_chaId);
		writeD(_running ? 1 : 0);
		writeD(0);
	}

	static
	{
		ChangeMoveType.WALK = 0;
		ChangeMoveType.RUN = 1;
	}
}
