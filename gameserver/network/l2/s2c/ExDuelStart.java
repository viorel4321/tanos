package l2s.gameserver.network.l2.s2c;

public class ExDuelStart extends L2GameServerPacket
{
	int _duelType;

	public ExDuelStart(final int duelType)
	{
		_duelType = duelType;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(77);
		writeD(_duelType);
	}
}
