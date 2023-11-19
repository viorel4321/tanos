package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.Location;

public class Earthquake extends L2GameServerPacket
{
	private Location _loc;
	private int _intensity;
	private int _duration;

	public Earthquake(final Location loc, final int intensity, final int duration)
	{
		_loc = loc;
		_intensity = intensity;
		_duration = duration;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(196);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeD(_intensity);
		writeD(_duration);
		writeD(0);
	}
}
