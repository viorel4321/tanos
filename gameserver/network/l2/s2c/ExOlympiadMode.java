package l2s.gameserver.network.l2.s2c;

public class ExOlympiadMode extends L2GameServerPacket
{
	private int _mode;

	public ExOlympiadMode(final int mode)
	{
		_mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(43);
		writeC(_mode);
	}
}
