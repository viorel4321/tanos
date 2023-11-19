package l2s.gameserver.geodata.utils;

import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.DropItem;
import l2s.gameserver.utils.Location;

public class GeodataUtils
{
	public static final byte EAST = 1, WEST = 2, SOUTH = 4, NORTH = 8, NSWE_ALL = 15, NSWE_NONE = 0;

	/*public static void debug2DLine(Player player, int x, int y, int tx, int ty, int z)
	{
		int gx = GeoEngine.getMapX(x);
		int gy = GeoEngine.getMapY(y);

		int tgx = GeoEngine.getMapX(tx);
		int tgy = GeoEngine.getMapY(ty);

		ExServerPrimitivePacket prim = new ExServerPrimitivePacket("Debug2DLine", x, y, z);
		prim.addLine(Color.BLUE, GeoEngine.getWorldX(gx), GeoEngine.getWorldY(gy), z, GeoEngine.getWorldX(tgx), GeoEngine.getWorldY(tgy), z);

		LinePointIterator iter = new LinePointIterator(gx, gy, tgx, tgy);

		while(iter.next())
		{
			int wx = GeoEngine.getWorldX(iter.x());
			int wy = GeoEngine.getWorldY(iter.y());

			prim.addPoint(Color.RED, wx, wy, z);
		}
		player.sendPacket(prim);
	}

	public static void debug3DLine(Player player, int x, int y, int z, int tx, int ty, int tz)
	{
		int gx = GeoEngine.getMapX(x);
		int gy = GeoEngine.getMapY(y);

		int tgx = GeoEngine.getMapX(tx);
		int tgy = GeoEngine.getMapY(ty);

		ExServerPrimitivePacket prim = new ExServerPrimitivePacket("Debug3DLine", x, y, z);
		prim.addLine(Color.BLUE, GeoEngine.getWorldX(gx), GeoEngine.getWorldY(gy), z, GeoEngine.getWorldX(tgx), GeoEngine.getWorldY(tgy), tz);

		LinePointIterator3D iter = new LinePointIterator3D(gx, gy, z, tgx, tgy, tz);
		iter.next();
		int prevX = iter.x();
		int prevY = iter.y();
		int wx = GeoEngine.getWorldX(prevX);
		int wy = GeoEngine.getWorldY(prevY);
		int wz = iter.z();
		prim.addPoint(Color.RED, wx, wy, wz);

		while(iter.next())
		{
			int curX = iter.x();
			int curY = iter.y();

			if((curX != prevX) || (curY != prevY))
			{
				wx = GeoEngine.getWorldX(curX);
				wy = GeoEngine.getWorldY(curY);
				wz = iter.z();

				prim.addPoint(Color.RED, wx, wy, wz);

				prevX = curX;
				prevY = curY;
			}
		}
		player.sendPacket(prim);
	}*/

	private static int getDirectionColor(int x, int y, int z, int geoIndex, byte NSWE)
	{
		// TODO: Цвет зависящий от положения персонажа и высоты слоя.
		if((GeoEngine.getLowerNSWE(x, y, z, geoIndex) & NSWE) != 0)
		{
			return 734;
		}
		return 1061;
	}

	public static void debugGrid(Player player, int geoRadius)
	{
		if(geoRadius < 0)
			throw new IllegalArgumentException("geoRadius < 0");

		final int blocksPerPacket = 10;

		Location playerGeoLoc = player.getLoc().clone().world2geo();
		for(int dx = -geoRadius; dx <= geoRadius; ++dx)
		{
			for(int dy = -geoRadius; dy <= geoRadius; ++dy)
			{
				int gx = playerGeoLoc.getX() + dx;
				int gy = playerGeoLoc.getY() + dy;

				int geoIndex = player.getGeoIndex();
				Location worldLoc = new Location(gx, gy, playerGeoLoc.getZ() + Config.MIN_LAYER_HEIGHT).geo2world();
				int x = worldLoc.getX();
				int y = worldLoc.getY();
				int z = GeoEngine.getLowerHeight(worldLoc, geoIndex);

				// north arrow
				int col = getDirectionColor(x, y, z, geoIndex, NORTH);
				sendPoint(player, col, x, y - 5, z);
				//sendPoint(player, col, x - 2, y - 6, z, x + 2, y - 6, z);
				//sendPoint(player, col, x - 3, y - 5, z, x + 3, y - 5, z);
				//sendPoint(player, col, x - 4, y - 4, z, x + 4, y - 4, z);

				// east arrow
				col = getDirectionColor(x, y, z, geoIndex, EAST);
				sendPoint(player, col, x + 5, y, z);
				//sendPoint(player, col, x + 6, y - 2, z, x + 6, y + 2, z);
				//sendPoint(player, col, x + 5, y - 3, z, x + 5, y + 3, z);
				//sendPoint(player, col, x + 4, y - 4, z, x + 4, y + 4, z);

				// south arrow
				col = getDirectionColor(x, y, z, geoIndex, SOUTH);
				sendPoint(player, col, x, y + 5, z);
				//sendPoint(player, col, x - 2, y + 6, z, x + 2, y + 6, z);
				//sendPoint(player, col, x - 3, y + 5, z, x + 3, y + 5, z);
				//sendPoint(player, col, x - 4, y + 4, z, x + 4, y + 4, z);

				col = getDirectionColor(x, y, z, geoIndex, WEST);
				sendPoint(player, col, x - 5, y, z);
				//sendPoint(player, col, x - 6, y - 2, z, x - 6, y + 2, z);
				//sendPoint(player, col, x - 5, y - 3, z, x - 5, y + 3, z);
				//sendPoint(player, col, x - 4, y - 4, z, x - 4, y + 4, z);
			}
		}
	}

	private static void sendPoint(Player player, int color, int x, int y, int z) { 
		player.sendPacket(new DropItem(player.getObjectId(), IdFactory.getInstance().getNextId(), color, x, y, z, false, 1));
	}
}