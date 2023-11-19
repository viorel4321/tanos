package l2s.gameserver.network.l2.s2c;

public class ServerClose extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(38);
	}
}
