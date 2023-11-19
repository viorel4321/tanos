package l2s.gameserver.network.l2.s2c;

public class ExMPCCClose extends L2GameServerPacket
{
	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(38);
	}
}
