package l2s.gameserver.data;

import java.lang.reflect.Constructor;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.templates.CreatureTemplate;

public final class BoatHolder extends AbstractHolder
{
	public static final CreatureTemplate TEMPLATE;
	private static BoatHolder _instance;
	private final TIntObjectHashMap<Vehicle> _boats;

	public BoatHolder()
	{
		_boats = new TIntObjectHashMap<Vehicle>();
	}

	public static BoatHolder getInstance()
	{
		return BoatHolder._instance;
	}

	public void spawnAll()
	{
		log();
		final TIntObjectIterator<Vehicle> iterator = _boats.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			iterator.value().spawnMe();
			this.info("Spawning: " + iterator.value().getName());
		}
	}

	public Vehicle initBoat(final String name, final String clazz)
	{
		try
		{
			final Class<?> cl = Class.forName("l2s.gameserver.model.entity." + clazz);
			final Constructor<?> constructor = cl.getConstructor(Integer.TYPE, CreatureTemplate.class);
			final Vehicle boat = (Vehicle) constructor.newInstance(IdFactory.getInstance().getNextId(), BoatHolder.TEMPLATE);
			boat.setName(name);
			addBoat(boat);
			return boat;
		}
		catch(Exception e)
		{
			this.error("Fail to init boat: " + clazz, e);
			return null;
		}
	}

	public Vehicle getBoat(final String name)
	{
		final TIntObjectIterator<Vehicle> iterator = _boats.iterator();
		while(iterator.hasNext())
		{
			iterator.advance();
			if(iterator.value().getName().equals(name))
				return iterator.value();
		}
		return null;
	}

	public Vehicle getBoat(final int objectId)
	{
		return _boats.get(objectId);
	}

	public void addBoat(final Vehicle boat)
	{
		_boats.put(boat.getObjectId(), boat);
	}

	public void removeBoat(final Vehicle boat)
	{
		_boats.remove(boat.getObjectId());
	}

	@Override
	public int size()
	{
		return _boats.size();
	}

	@Override
	public void clear()
	{
		_boats.clear();
	}

	static
	{
		TEMPLATE = new CreatureTemplate(CreatureTemplate.getEmptyStatsSet());
		BoatHolder._instance = new BoatHolder();
	}
}
