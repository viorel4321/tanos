package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.GameClient;

public class CharacterSelected extends L2GameClientPacket
{
	private int _index;

	@Override
	protected void readImpl()
	{
		_index = readD();
	}

	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		client.playerSelected(_index);
	}
}
