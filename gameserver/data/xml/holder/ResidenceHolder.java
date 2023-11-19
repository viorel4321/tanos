package l2s.gameserver.data.xml.holder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.TreeIntObjectMap;

import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.model.GameObject;
import l2s.gameserver.model.entity.residence.Residence;

@SuppressWarnings("unchecked")
public final class ResidenceHolder extends AbstractHolder
{
	private static ResidenceHolder _instance;
	private IntObjectMap<Residence> _residences;
	private Map<Class<?>, List<Residence>> _fastResidencesByType;

	public static ResidenceHolder getInstance()
	{
		return ResidenceHolder._instance;
	}

	private ResidenceHolder()
	{
		_residences = new TreeIntObjectMap<Residence>();
		_fastResidencesByType = new HashMap<Class<?>, List<Residence>>(4);
	}

	public void addResidence(final Residence r)
	{
		_residences.put(r.getId(), r);
	}

	public <R extends Residence> R getResidence(final int id)
	{
		return (R) _residences.get(id);
	}

	public <R extends Residence> R getResidence(final Class<R> type, final int id)
	{
		final Residence r = getResidence(id);
		if(r == null || r.getClass() != type)
			return null;
		return (R) r;
	}

	public <R extends Residence> List<R> getResidenceList(final Class<R> t)
	{
		return (List<R>) _fastResidencesByType.get(t);
	}

	public Collection<Residence> getResidences()
	{
		return _residences.valueCollection();
	}

	public <R extends Residence> R getResidenceByObject(Class<? extends Residence> type, GameObject object)
	{
		return (R) getResidenceByCoord(type, object.getX(), object.getY(), object.getZ());
	}

	public <R extends Residence> R getResidenceByCoord(Class<R> type, int x, int y, int z)
	{
		Collection<Residence> residences = type == null ? getResidences() : (Collection<Residence>) getResidenceList(type);
		if (residences == null)
		{
			return null;
		}
		for(Residence residence : residences)
		{
			if(residence.checkIfInZone(x, y, z))
				return (R) residence;
		}
		return null;
	}

	public <R extends Residence> R findNearestResidence(Class<R> clazz, int x, int y, int z, int offset)
	{
		Residence residence = getResidenceByCoord(clazz, x, y, z);
		if(residence == null)
		{
			double closestDistance = offset;
			for(final Residence r : getResidenceList(clazz))
			{
				final double distance = r.getZone().findDistanceToZone(x, y, z, false);
				if(closestDistance > distance)
				{
					closestDistance = distance;
					residence = r;
				}
			}
		}
		return (R) residence;
	}

	public void callInit()
	{
		for(final Residence r : getResidences())
			r.init();
	}

	private void buildFastLook()
	{
		for(final Residence residence : _residences.valueCollection())
		{
			List<Residence> list = _fastResidencesByType.get(residence.getClass());
			if(list == null)
				_fastResidencesByType.put(residence.getClass(), list = new ArrayList<Residence>());
			list.add(residence);
		}
	}

	@Override
	public void log()
	{
		buildFastLook();
		this.info("total size: " + _residences.size());
		for(final Map.Entry<Class<?>, List<Residence>> entry : _fastResidencesByType.entrySet())
			this.info("loaded " + entry.getValue().size() + " " + entry.getKey().getSimpleName().toLowerCase() + "(s).");
	}

	@Override
	public int size()
	{
		return 0;
	}

	@Override
	public void clear()
	{
		_residences.clear();
		_fastResidencesByType.clear();
	}

	static
	{
		ResidenceHolder._instance = new ResidenceHolder();
	}
}
