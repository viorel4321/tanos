package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.GameClient;

public class GameGuardReply extends L2GameClientPacket
{
	private int[] _reply;

	public GameGuardReply()
	{
		_reply = new int[4];
	}

	@Override
	public void readImpl()
	{
		final byte[] b = new byte[this.getByteBuffer().remaining()];
		readB(b);
	}

	@Override
	public void runImpl()
	{
		//
	}
}
