package l2s.gameserver.network.l2.s2c;

public class ExMailArrived extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(45);
	}
}
