package l2s.gameserver.network.l2.s2c;

public class CameraMode extends L2GameServerPacket
{
	int _mode;

	public CameraMode(final int mode)
	{
		_mode = mode;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(241);
		writeD(_mode);
	}
}
