package l2s.gameserver.network.l2.s2c;

public class PartySmallWindowDeleteAll extends L2GameServerPacket
{
	public static final PartySmallWindowDeleteAll STATIC_PACKET;

	@Override
	protected final void writeImpl()
	{
		writeC(80);
	}

	static
	{
		STATIC_PACKET = new PartySmallWindowDeleteAll();
	}
}
