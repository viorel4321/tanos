package l2s.gameserver.model;

import l2s.commons.geometry.*;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.geodata.GeoEngine;
import l2s.gameserver.templates.spawn.SpawnRange;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.PositionUtils;
import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import java.util.ArrayList;
import java.util.List;

public class Territory implements Shape, SpawnRange
{
	private final static int RANDOM_LOC_FIND_ATTEMPTS = 100;

	protected final Point3D max = new Point3D();
	protected final Point3D min = new Point3D();
	private final List<Shape> include = new ArrayList<Shape>(1);
	private final List<Shape> exclude = new ArrayList<Shape>(1);
	private Zone _zone;
	private int _id;

	protected int radius = 0;

	public Territory(final int id)
	{
		_id = id;
	}

	public Territory add(final Shape shape)
	{
		if(include.isEmpty())
		{
			max.x = shape.getXmax();
			max.y = shape.getYmax();
			max.z = shape.getZmax();
			min.x = shape.getXmin();
			min.y = shape.getYmin();
			min.z = shape.getZmin();
		}
		else
		{
			max.x = Math.max(max.x, shape.getXmax());
			max.y = Math.max(max.y, shape.getYmax());
			max.z = Math.max(max.z, shape.getZmax());
			min.x = Math.min(min.x, shape.getXmin());
			min.y = Math.min(min.y, shape.getYmin());
			min.z = Math.min(min.z, shape.getZmin());
		}
		include.add(shape);
		radius = Math.max(radius, shape.getRadius());
		return this;
	}

	public Territory addBanned(final Shape shape)
	{
		exclude.add(shape);
		return this;
	}

	public List<Shape> getTerritories()
	{
		return include;
	}

	public List<Shape> getBannedTerritories()
	{
		return exclude;
	}

	@Override
	public boolean isInside(int x, int y, CoordsConverter c)
	{
		for(Shape shape : include)
		{
			if(shape.isInside(x, y, c))
				return !isExcluded(x, y, c);
		}
		return false;
	}

	@Override
	public boolean isInside(int x, int y, int z, CoordsConverter c)
	{
		if(x < c.convertX(this.min.x) || x > c.convertX(this.max.x) || y < c.convertY(this.min.y) || y > c.convertY(this.max.y) || z < this.min.z || z > this.max.z)
			return false;

		for(Shape shape : include)
		{
			if(shape.isInside(x, y, z, c))
				return !isExcluded(x, y, z, c);
		}
		return false;
	}

	@Override
	public boolean isOnPerimeter(int x, int y, CoordsConverter c)
	{
		for(Shape shape : include)
		{
			if(shape.isOnPerimeter(x, y, c))
			{
				if(!isExcluded(x, y, c))
					return true;
			}
		}
		for(Shape shape : exclude)
		{
			if(shape.isOnPerimeter(x, y, c))
			{
				if(isInside(x, y, c))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean isOnPerimeter(int x, int y, int z, CoordsConverter c)
	{
		if(x < c.convertX(this.min.x) || x > c.convertX(this.max.x) || y < c.convertY(this.min.y) || y > c.convertY(this.max.y) || z < this.min.z || z > this.max.z)
			return false;

		for(Shape shape : include)
		{
			if(shape.isOnPerimeter(x, y, z))
			{
				if(!isExcluded(x, y, z, c))
					return true;
			}
		}
		for(Shape shape : exclude)
		{
			if(shape.isOnPerimeter(x, y, z, c))
			{
				if(isInside(x, y, z, c))
					return true;
			}
		}
		return false;
	}

	public boolean isInside(GameObject obj)
	{
		return isInside(obj.getLoc());
	}

	public boolean isInside(Location loc)
	{
		return isInside(loc.x, loc.y, loc.z);
	}

	public boolean isExcluded(int x, int y, CoordsConverter c)
	{
		Shape shape;
		for(int i = 0; i < exclude.size(); i++)
		{
			shape = exclude.get(i);
			if(shape.isInside(x, y, c))
				return true;
		}
		return false;
	}

	public boolean isExcluded(int x, int y, int z, CoordsConverter c)
	{
		Shape shape;
		for(int i = 0; i < exclude.size(); i++)
		{
			shape = exclude.get(i);
			if(shape.isInside(x, y, z, c))
				return true;
		}
		return false;
	}

	@Override
	public int getXmax()
	{
		return max.x;
	}

	@Override
	public int getXmin()
	{
		return min.x;
	}

	@Override
	public int getYmax()
	{
		return max.y;
	}

	@Override
	public int getYmin()
	{
		return min.y;
	}

	@Override
	public int getZmax()
	{
		return max.z;
	}

	@Override
	public int getZmin()
	{
		return min.z;
	}

	public static Location getRandomLoc(Territory territory, int geoIndex)
	{
		Location pos = new Location();

		List<Shape> territories = territory.getTerritories();

		for(int i = 1; i <= RANDOM_LOC_FIND_ATTEMPTS; i++)
		{
			Shape shape = territories.get(Rnd.get(territories.size()));

			pos.x = Rnd.get(shape.getXmin(), shape.getXmax());
			pos.y = Rnd.get(shape.getYmin(), shape.getYmax());

			int minZ = Math.min(shape.getZmin(), shape.getZmax());
			int maxZ = Math.max(shape.getZmin(), shape.getZmax());

			if(territory.isInside(pos.x, pos.y))
			{
				if(minZ == maxZ)
				{
					minZ -= 200;
					maxZ += 200;
				}
				pos.z = maxZ;

				if(!Config.ALLOW_GEODATA)
					break;

				// Не спаунить в колонны, стены и прочее.
				IntSet zSet = new HashIntSet();

				int tempz = maxZ;
				loop: for(int l = 1; l <= GeoEngine.MAX_LAYERS; l++)
				{
					tempz = GeoEngine.getLowerHeight(pos.x, pos.y, tempz, geoIndex);
					if(tempz < minZ) // Дошли до дна.
						break;

					if(!zSet.contains(tempz))
					{
						int geoX = GeoEngine.getGeoX(pos.x);
						int geoY = GeoEngine.getGeoY(pos.y);

						// Если местность подозрительная - пропускаем
						for(int x = geoX - 1; x <= geoX + 1; x++)
						{
							for(int y = geoY - 1; y <= geoY + 1; y++)
							{
								if(GeoEngine.NgetLowerNSWE(x, y, (short) (tempz + Config.MIN_LAYER_HEIGHT), geoIndex) != GeoEngine.NSWE_ALL)
									continue loop;
							}
						}
						zSet.add(tempz);
					}

					tempz -= Config.MIN_LAYER_HEIGHT; // Опускаемся ниже.
				}


				if(zSet.isEmpty())
					continue;

				pos.z = Rnd.get(zSet.toArray());
				break;
			}

			if(i == RANDOM_LOC_FIND_ATTEMPTS)
			{
				pos.z = GeoEngine.correctGeoZ(pos.x, pos.y, maxZ, geoIndex);
				break;
			}
		}
		pos.h = Location.getRandomHeading();
		return pos;
	}

	public void doEnter(final GameObject object)
	{
		if(_zone != null)
			if(object.isPlayable())
				_zone.doEnter(object);
			else if((_zone.getZoneSkill() != null && _zone.getZoneTarget() == Zone.ZoneTarget.npc || Config.CHECK_EPIC_CAN_DAMAGE && _zone.getType() == Zone.ZoneType.epic) && object.isNpc())
				_zone.doEnter(object);
	}

	public void doLeave(final GameObject object, final boolean notify)
	{
		if(_zone != null)
			if(object.isPlayable())
				_zone.doLeave(object, notify);
			else if((_zone.getZoneSkill() != null && _zone.getZoneTarget() == Zone.ZoneTarget.npc || Config.CHECK_EPIC_CAN_DAMAGE && _zone.getType() == Zone.ZoneType.epic) && object.isNpc())
				_zone.doLeave(object, notify);
	}

	public final int getId()
	{
		return _id;
	}

	public void setZone(final Zone zone)
	{
		_zone = zone;
	}

	public Zone getZone()
	{
		return _zone;
	}

	public boolean isWorldTerritory()
	{
		return getZone() != null && getZone().getLoc() == this;
	}

	public double getDistance(Location loc)
	{
		Point2D nearestPoint = getNearestPoint(loc.getX(), loc.getY());
		return PositionUtils.getDistance(nearestPoint.x, nearestPoint.y, loc.getX(), loc.getY());
	}

	@Override
	public Location getRandomLoc(int geoIndex)
	{
		return getRandomLoc(this, geoIndex);
	}

	@Override
	public Point2D getCenter()
	{
		return GeometryUtils.getLineCenter(min.x, min.y, max.x, max.y);
	}

	@Override
	public Point2D getNearestPoint(int x, int y)
	{
		Point2D nearestPoint = new Point2D();
		for(Shape shape : include)
		{
			Point2D n = shape.getNearestPoint(x, y);
			if(GeometryUtils.calculateDistance(n.x, n.y, x, y) < GeometryUtils.calculateDistance(nearestPoint.x, nearestPoint.y, x, y))
			{
				if(!isExcluded(x, y, CoordsConverter.DEFAULT_CONVERTER))
					nearestPoint = n;
			}
		}
		for(Shape shape : exclude)
		{
			Point2D n = shape.getNearestPoint(x, y);
			if(GeometryUtils.calculateDistance(n.x, n.y, x, y) < GeometryUtils.calculateDistance(nearestPoint.x, nearestPoint.y, x, y))
			{
				if(isInside(x, y))
					nearestPoint = n;
			}
		}
		return nearestPoint;
	}

	@Override
	public int getRadius()
	{
		return radius;
	}

	@Override
	public Point2D[] getPoints()
	{
		List<Point2D> points = new ArrayList<Point2D>();
		for(Shape shape : include)
		{
			for(Point2D point : shape.getPoints())
				points.add(point);
		}
		for(Shape shape : exclude)
		{
			for(Point2D point : shape.getPoints())
				points.add(point);
		}
		return points.toArray(new Point2D[points.size()]);
	}
}
