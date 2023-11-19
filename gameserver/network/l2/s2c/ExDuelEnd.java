package l2s.gameserver.network.l2.s2c;

public class ExDuelEnd extends L2GameServerPacket
{
	int _duelType;

	public ExDuelEnd(final int duelType)
	{
		_duelType = duelType;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(78);
		writeD(_duelType);
	}
}
