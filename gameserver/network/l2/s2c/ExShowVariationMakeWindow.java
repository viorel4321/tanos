package l2s.gameserver.network.l2.s2c;

public class ExShowVariationMakeWindow extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(80);
	}
}
