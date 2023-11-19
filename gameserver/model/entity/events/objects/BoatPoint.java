package l2s.gameserver.model.entity.events.objects;

import org.dom4j.Element;

import l2s.gameserver.utils.Location;

public class BoatPoint extends Location
{
	private static final long serialVersionUID = 1L;
	private int _speed1;
	private int _speed2;
	private final int _fuel;
	private boolean _teleport;

	public BoatPoint(final int x, final int y, final int z, final int h, final int speed1, final int speed2, final int fuel, final boolean teleport)
	{
		super(x, y, z, h);
		_speed1 = speed1;
		_speed2 = speed2;
		_fuel = fuel;
		_teleport = teleport;
	}

	public int getSpeed1()
	{
		return _speed1;
	}

	public int getSpeed2()
	{
		return _speed2;
	}

	public int getFuel()
	{
		return _fuel;
	}

	public boolean isTeleport()
	{
		return _teleport;
	}

	public static BoatPoint parse(final Element element)
	{
		final int speed1 = element.attributeValue("speed1") == null ? 0 : Integer.parseInt(element.attributeValue("speed1"));
		final int speed2 = element.attributeValue("speed2") == null ? 0 : Integer.parseInt(element.attributeValue("speed2"));
		final int x = Integer.parseInt(element.attributeValue("x"));
		final int y = Integer.parseInt(element.attributeValue("y"));
		final int z = Integer.parseInt(element.attributeValue("z"));
		final int h = element.attributeValue("h") == null ? 0 : Integer.parseInt(element.attributeValue("h"));
		final int fuel = element.attributeValue("fuel") == null ? 0 : Integer.parseInt(element.attributeValue("fuel"));
		final boolean teleport = Boolean.parseBoolean(element.attributeValue("teleport"));
		return new BoatPoint(x, y, z, h, speed1, speed2, fuel, teleport);
	}

	public void setSpeed1(final int speed1)
	{
		_speed1 = speed1;
	}

	public void setSpeed2(final int speed2)
	{
		_speed2 = speed2;
	}

	public void setTeleport(final boolean teleport)
	{
		_teleport = teleport;
	}
}
