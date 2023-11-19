package l2s.gameserver.network.l2.s2c;

public class SetupGauge extends L2GameServerPacket
{
	public static final int BLUE = 0;
	public static final int RED = 1;
	public static final int CYAN = 2;
	private int _dat1;
	private int _time;

	public SetupGauge(final int dat1, final int time)
	{
		_dat1 = dat1;
		_time = time;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(109);
		writeD(_dat1);
		writeD(_time);
		writeD(_time);
	}
}
