package l2s.gameserver.network.l2.s2c;

public class ExOlympiadMatchEnd extends L2GameServerPacket
{
	public static final ExOlympiadMatchEnd STATIC_PACKET;

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(44);
	}

	static
	{
		STATIC_PACKET = new ExOlympiadMatchEnd();
	}
}
