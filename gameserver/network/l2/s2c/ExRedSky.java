package l2s.gameserver.network.l2.s2c;

public class ExRedSky extends L2GameServerPacket
{
	private int _duration;

	public ExRedSky(final int duration)
	{
		_duration = duration;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(64);
		writeD(_duration);
	}
}
