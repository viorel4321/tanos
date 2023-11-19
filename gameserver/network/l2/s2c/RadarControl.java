package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.Location;

public class RadarControl extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _type;
	private int _showRadar;

	public RadarControl(final int showRadar, final int type, final Location loc)
	{
		this(showRadar, type, loc.x, loc.y, loc.z);
	}

	public RadarControl(final int showRadar, final int type, final int x, final int y, final int z)
	{
		_showRadar = showRadar;
		_type = type;
		_x = x;
		_y = y;
		_z = z;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(235);
		writeD(_showRadar);
		writeD(_type);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
