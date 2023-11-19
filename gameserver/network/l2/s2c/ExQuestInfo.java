package l2s.gameserver.network.l2.s2c;

public class ExQuestInfo extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(25);
	}
}
