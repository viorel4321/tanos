package l2s.gameserver.network.l2.s2c;

public class ExClosePartyRoom extends L2GameServerPacket
{
	public static L2GameServerPacket STATIC;

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(9);
	}

	static
	{
		ExClosePartyRoom.STATIC = new ExClosePartyRoom();
	}
}
