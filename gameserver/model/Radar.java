package l2s.gameserver.model;

import java.util.Vector;

import l2s.gameserver.network.l2.s2c.RadarControl;
import l2s.gameserver.utils.Location;

public final class Radar
{
	private Player player;
	private Vector<RadarMarker> markers;

	public Radar(final Player p)
	{
		player = p;
		markers = new Vector<RadarMarker>();
	}

	public void addMarker(final int x, final int y, final int z)
	{
		final RadarMarker newMarker = new RadarMarker(x, y, z);
		markers.add(newMarker);
		player.sendPacket(new RadarControl(2, 2, newMarker));
		player.sendPacket(new RadarControl(0, 1, newMarker));
	}

	public void removeMarker(final int x, final int y, final int z)
	{
		final RadarMarker newMarker = new RadarMarker(x, y, z);
		markers.remove(newMarker);
		player.sendPacket(new RadarControl(1, 1, newMarker));
	}

	public void removeAllMarkers()
	{
		for(final RadarMarker tempMarker : markers)
			player.sendPacket(new RadarControl(2, 2, tempMarker));
		markers.removeAllElements();
	}

	public void loadMarkers()
	{
		player.sendPacket(new RadarControl(2, 2, player.getX(), player.getY(), player.getZ()));
		for(final RadarMarker tempMarker : markers)
			player.sendPacket(new RadarControl(0, 1, tempMarker));
	}

	public class RadarMarker extends Location
	{
		public int type;

		public RadarMarker(final int type_, final int x_, final int y_, final int z_)
		{
			super(x_, y_, z_);
			type = type_;
		}

		public RadarMarker(final int x_, final int y_, final int z_)
		{
			super(x_, y_, z_);
			type = 1;
		}

		public boolean equals(final Object obj)
		{
			try
			{
				final RadarMarker temp = (RadarMarker) obj;
				return temp.x == x && temp.y == y && temp.z == z && temp.type == type;
			}
			catch(Exception e)
			{
				return false;
			}
		}
	}
}
