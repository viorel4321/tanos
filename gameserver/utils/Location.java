package l2s.gameserver.utils;

import java.io.Serializable;

import org.dom4j.Element;

import l2s.commons.geometry.Point3D;
import l2s.commons.util.Rnd;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.World;
import l2s.gameserver.templates.spawn.SpawnRange;

public class Location extends Point3D implements SpawnRange, Serializable
{
	private static final long serialVersionUID = 1L;
	public int x;
	public int y;
	public int z;
	public int h;
	public int id;

	public Location()
	{
		id = 0;
		x = 0;
		y = 0;
		z = 0;
		h = 0;
	}

	public Location(final int locX, final int locY, final int locZ)
	{
		id = 0;
		x = locX;
		y = locY;
		z = locZ;
	}

	public Location(final int locX, final int locY, final int locZ, final int heading)
	{
		id = 0;
		x = locX;
		y = locY;
		z = locZ;
		h = heading;
	}

	public Location(final int locX, final int locY, final int locZ, final int heading, final int npcId)
	{
		id = 0;
		x = locX;
		y = locY;
		z = locZ;
		h = heading;
		id = npcId;
	}

	public Location(final GameObject obj)
	{
		id = 0;
		x = obj.getX();
		y = obj.getY();
		z = obj.getZ();
		h = obj.getHeading();
	}

	public Location(final int[] point)
	{
		id = 0;
		x = point[0];
		y = point[1];
		z = point[2];
		try
		{
			h = point[3];
		}
		catch(Exception e)
		{
			h = 0;
		}
	}

	public Location(final String s) throws IllegalArgumentException
	{
		id = 0;
		final String[] xyzh = s.replaceAll(",", " ").replaceAll(";", " ").replaceAll("  ", " ").trim().split(" ");
		if(xyzh.length < 3)
			throw new IllegalArgumentException("Can't parse location from string: " + s);
		x = Integer.parseInt(xyzh[0]);
		y = Integer.parseInt(xyzh[1]);
		z = Integer.parseInt(xyzh[2]);
		h = xyzh.length < 4 ? 0 : Integer.parseInt(xyzh[3]);
	}

	public boolean equals(final Location loc)
	{
		return loc != null && loc.getX() == x && loc.getY() == y && loc.getZ() == z;
	}

	@Override
	public boolean equals(final int _x, final int _y, final int _z)
	{
		return _x == x && _y == y && _z == z;
	}

	public boolean equals(final int _x, final int _y, final int _z, final int _h)
	{
		return _x == x && _y == y && _z == z && h == _h;
	}

	public Location changeZ(final int zDiff)
	{
		z += zDiff;
		return this;
	}

	public Location correctGeoZ(int geoIndex)
	{
		z = GeoEngine.correctGeoZ(x, y, z, geoIndex);
		return this;
	}

	public Location setX(final int x)
	{
		this.x = x;
		return this;
	}

	public Location setY(final int y)
	{
		this.y = y;
		return this;
	}

	public Location setZ(final int z)
	{
		this.z = z;
		return this;
	}

	public Location setH(final int h)
	{
		this.h = h;
		return this;
	}

	public Location setXY(final int x, final int y)
	{
		this.x = x;
		this.y = y;
		return this;
	}

	public Location setXYZ(final int x, final int y, final int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Location setId(final int _id)
	{
		id = _id;
		return this;
	}

	public void set(final int _x, final int _y, final int _z)
	{
		x = _x;
		y = _y;
		z = _z;
	}

	public void set(final int _x, final int _y, final int _z, final int _h)
	{
		x = _x;
		y = _y;
		z = _z;
		h = _h;
	}

	public void set(final Location loc)
	{
		x = loc.x;
		y = loc.y;
		z = loc.z;
		h = loc.h;
	}

	public Location rnd(final int min, final int max, final boolean change)
	{
		Location loc = coordsRandomize(this, min, max);
		for(int i = 0; i < 10; i++)
		{
			loc = GeoEngine.moveCheck(x, y, z, loc.x, loc.y, 0);
			if(loc != null)
			{
				if(change)
				{
					x = loc.x;
					y = loc.y;
					z = loc.z;
					return this;
				}
				return loc;
			}
		}
		if(change)
			return this;
		return clone();
	}

	public Location world2geo()
	{
		x = x - World.MAP_MIN_X >> 4;
		y = y - World.MAP_MIN_Y >> 4;
		return this;
	}

	public Location geo2world()
	{
		x = (x << 4) + World.MAP_MIN_X + 8;
		y = (y << 4) + World.MAP_MIN_Y + 8;
		return this;
	}

	public double distance(final Location loc)
	{
		return this.distance(loc.x, loc.y);
	}

	public double distance(final int x, final int y)
	{
		final long dx = this.x - x;
		final long dy = this.y - y;
		return Math.sqrt(dx * dx + dy * dy);
	}

	public double distance3D(final Location loc)
	{
		if(loc == null)
			return 0.0;
		return this.distance3D(loc.x, loc.y, loc.z);
	}

	public double distance3D(final int x, final int y, final int z)
	{
		final long dx = this.x - x;
		final long dy = this.y - y;
		final long dz = this.z - z;
		return Math.sqrt(dx * dx + dy * dy + dz * dz);
	}

	@Override
	public Location clone()
	{
		return new Location(x, y, z, h, id);
	}

	@Override
	public int getX()
	{
		return x;
	}

	@Override
	public int getY()
	{
		return y;
	}

	@Override
	public int getZ()
	{
		return z;
	}

	@Override
	public final String toString()
	{
		return x + "," + y + "," + z + "," + h;
	}

	public boolean isNull()
	{
		return x == 0 || y == 0 || z == 0;
	}

	@Override
	public Location getRandomLoc(int geoIndex)
	{
		return this;
	}

	public static Location parseLoc(final String s) throws IllegalArgumentException
	{
		final String[] xyzh = s.split("[\\s,;]+");
		if(xyzh.length < 3)
			throw new IllegalArgumentException("Can't parse location from string: " + s);
		final int x = Integer.parseInt(xyzh[0]);
		final int y = Integer.parseInt(xyzh[1]);
		final int z = Integer.parseInt(xyzh[2]);
		final int h = xyzh.length < 4 ? 0 : Integer.parseInt(xyzh[3]);
		return new Location(x, y, z, h);
	}

	public static Location parse(final Element element)
	{
		final int x = Integer.parseInt(element.attributeValue("x"));
		final int y = Integer.parseInt(element.attributeValue("y"));
		final int z = Integer.parseInt(element.attributeValue("z"));
		final int h = element.attributeValue("h") == null ? 0 : Integer.parseInt(element.attributeValue("h"));
		return new Location(x, y, z, h);
	}

	public static Location findFrontPosition(final GameObject obj, final GameObject obj2, final int radiusmin, final int radiusmax, final int geoIndex)
	{
		if(radiusmax == 0 || radiusmax < radiusmin)
			return new Location(obj);
		final double collision = obj.getCollisionRadius() + obj2.getCollisionRadius();
		int minangle = 0;
		int maxangle = 360;
		if(!obj.equals(obj2))
		{
			final double angle = Util.calculateAngleFrom(obj, obj2);
			minangle = (int) angle - 45;
			maxangle = (int) angle + 45;
		}
		final Location pos = new Location();
		for(int i = 0; i < 100; ++i)
		{
			final int randomRadius = Rnd.get(radiusmin, radiusmax);
			final int randomAngle = Rnd.get(minangle, maxangle);
			pos.x = obj.getX() + (int) ((collision + randomRadius) * Math.cos(Math.toRadians(randomAngle)));
			pos.y = obj.getY() + (int) ((collision + randomRadius) * Math.sin(Math.toRadians(randomAngle)));
			pos.z = obj.getZ();
			final int tempz = GeoEngine.getLowerHeight(pos.x, pos.y, pos.z, obj.getGeoIndex());
			if(Math.abs(pos.z - tempz) < 200 && GeoEngine.getLowerNSWE(pos.x, pos.y, tempz, geoIndex) == 15)
			{
				pos.z = tempz;
				if(!obj.equals(obj2))
					pos.h = Util.getHeadingTo(pos, obj2.getLoc());
				else
					pos.h = obj.getHeading();
				return pos;
			}
		}
		return new Location(obj);
	}

	public static Location findAroundPosition(final int x, final int y, final int z, final int radiusmin, final int radiusmax, final int geoIndex)
	{
		for(int i = 0; i < 100; ++i)
		{
			final Location pos = coordsRandomize(x, y, z, 0, radiusmin, radiusmax);
			final int tempz = GeoEngine.getLowerHeight(pos.x, pos.y, pos.z, geoIndex);
			if(GeoEngine.canMoveToCoord(x, y, z, pos.x, pos.y, tempz, geoIndex) && GeoEngine.canMoveToCoord(pos.x, pos.y, tempz, x, y, z, geoIndex))
			{
				pos.z = tempz;
				return pos;
			}
		}
		return new Location(x, y, z);
	}

	public static Location findAroundPosition(Location loc, int radius, int geoIndex)
	{
		return findAroundPosition(loc.x, loc.y, loc.z, 0, radius, geoIndex);
	}

	public static Location findAroundPosition(Location loc, int radiusmin, int radiusmax, int geoIndex)
	{
		return findAroundPosition(loc.x, loc.y, loc.z, radiusmin, radiusmax, geoIndex);
	}

	public static Location findPointToStay(final int x, final int y, final int z, final int radiusmin, final int radiusmax, final int geoIndex)
	{
		for(int i = 0; i < 100; ++i)
		{
			final Location pos = coordsRandomize(x, y, z, 0, radiusmin, radiusmax);
			final int tempz = GeoEngine.getLowerHeight(pos.x, pos.y, pos.z, geoIndex);
			if(Math.abs(pos.z - tempz) < 200 && GeoEngine.getLowerNSWE(pos.x, pos.y, tempz, geoIndex) == 15)
			{
				pos.z = tempz;
				return pos;
			}
		}
		return new Location(x, y, z);
	}

	public static Location findPointToStay(final Location loc, final int radiusmin, final int radiusmax, int geoIndex)
	{
		return findPointToStay(loc.x, loc.y, loc.z, radiusmin, radiusmax, geoIndex);
	}

	public static Location coordsRandomize(final Location loc, final int radiusmin, final int radiusmax)
	{
		return coordsRandomize(loc.x, loc.y, loc.z, loc.h, radiusmin, radiusmax);
	}

	public static Location coordsRandomize(final int x, final int y, final int z, final int heading, final int radiusmin, final int radiusmax)
	{
		if(radiusmax == 0 || radiusmax < radiusmin)
			return new Location(x, y, z, heading);
		final int radius = Rnd.get(radiusmin, radiusmax);
		final double angle = Rnd.nextDouble() * 2.0 * 3.141592653589793;
		return new Location((int) (x + radius * Math.cos(angle)), (int) (y + radius * Math.sin(angle)), z, heading);
	}

	public static int getRandomHeading()
	{
		return Rnd.get(0xFFFF);
	}
}
