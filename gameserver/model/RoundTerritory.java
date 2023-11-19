package l2s.gameserver.model;

import l2s.commons.geometry.Polygon;
import l2s.commons.util.Rnd;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public class RoundTerritory extends Territory
{
	protected final int _centerX;
	protected final int _centerY;
	protected final int _radius;

	public RoundTerritory(final int id, final int centerX, final int centerY, final int radius, final int zMin, final int zMax)
	{
		super(id);
		_centerX = centerX;
		_centerY = centerY;
		_radius = radius;
		final Polygon shape = new Polygon();
		shape.add(_centerX - _radius, _centerY - _radius).add(_centerX + _radius, _centerY - _radius).add(_centerX + _radius, _centerY + _radius).add(_centerX - _radius, _centerY + _radius).setZmin(zMin).setZmax(zMax);
		if(!shape.validate())
			System.out.println("Invalid territory in zone with coords: " + centerX + " " + centerY + " " + radius);
		add(shape);
	}

	public int getRadius()
	{
		return _radius;
	}

	@Override
	public void doEnter(final GameObject obj)
	{
		super.doEnter(obj);
	}

	@Override
	public void doLeave(final GameObject obj, final boolean notify)
	{
		super.doLeave(obj, notify);
	}

	@Override
	public boolean isInside(final int x, final int y)
	{
		return Util.checkIfInRange(_radius, _centerX, _centerY, x, y);
	}

	@Override
	public boolean isInside(final int x, final int y, final int z)
	{
		return isInside(x, y) && z >= getZmin() && z <= getZmax();
	}

	@Override
	public Location getRandomLoc(int geoIndex)
	{
		int[] xy;
		for(xy = getRandomXY(); !isInside(xy[0], xy[1]); xy = getRandomXY())
		{
			//
		}
		return new Location(xy[0], xy[1], getZmin(), getZmax());
	}

	private int[] getRandomXY()
	{
		return new int[] { Rnd.get(getXmin(), getXmax()), Rnd.get(getYmin(), getYmax()) };
	}
}
