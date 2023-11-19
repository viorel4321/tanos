package l2s.gameserver.network.l2.s2c;

public class ActionFail extends L2GameServerPacket
{
	public static final L2GameServerPacket STATIC;

	@Override
	protected void writeImpl()
	{
		writeC(37);
	}

	static
	{
		STATIC = new ActionFail();
	}
}
