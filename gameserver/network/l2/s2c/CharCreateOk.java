package l2s.gameserver.network.l2.s2c;

public class CharCreateOk extends L2GameServerPacket
{
	public static final CharCreateOk STATIC_PACKET;

	@Override
	protected final void writeImpl()
	{
		writeC(25);
		writeD(1);
	}

	static
	{
		STATIC_PACKET = new CharCreateOk();
	}
}
