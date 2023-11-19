package l2s.gameserver.utils;

import gnu.trove.TIntCollection;
import gnu.trove.impl.sync.TSynchronizedIntList;
import gnu.trove.impl.sync.TSynchronizedIntObjectMap;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;

import java.util.Collection;

public class MultiValueIntegerMap
{
	private TIntObjectMap<TIntList> map = new TSynchronizedIntObjectMap<>(new TIntObjectHashMap<>());

	public TIntSet keySet()
	{
		return map.keySet();
	}

	public Collection<TIntList> values()
	{
		return map.valueCollection();
	}

	public TIntList allValues()
	{
		final TIntList result = new TIntArrayList();
		for(TIntObjectIterator<TIntList> iterator = map.iterator(); iterator.hasNext();) {
			iterator.advance();
			result.addAll(iterator.value());
		}
		return result;
	}

	public TIntObjectIterator<TIntList> iterator()
	{
		return map.iterator();
	}

	public TIntList remove(int key)
	{
		return map.remove(key);
	}

	public TIntList get(int key)
	{
		return map.get(key);
	}

	public boolean containsKey(final Integer key)
	{
		return map.containsKey(key);
	}

	public void clear()
	{
		map.clear();
	}

	public int size()
	{
		return map.size();
	}

	public boolean isEmpty()
	{
		return map.isEmpty();
	}

	public Integer remove(final Integer key, final Integer value)
	{
		final TIntList valuesForKey = map.get(key);
		if(valuesForKey == null)
			return null;
		final boolean removed = valuesForKey.remove(value);
		if(!removed)
			return null;
		if(valuesForKey.isEmpty())
			this.remove(key);
		return value;
	}

	public int removeValue(int value)
	{
		final TIntList toRemove = new TIntArrayList(1);
		for(TIntObjectIterator<TIntList> iterator = map.iterator(); iterator.hasNext();) {
			iterator.advance();
			iterator.value().remove(value);
			if(iterator.value().isEmpty())
				toRemove.add(iterator.key());
		}

		for(int key : toRemove.toArray())
			remove(key);

		return value;
	}

	public Integer put(int key, int value)
	{
		TIntList coll = map.get(key);
		if(coll == null)
		{
			coll = new TSynchronizedIntList(new TIntArrayList());
			map.put(key, coll);
		}
		coll.add(value);
		return value;
	}

	public boolean containsValue(int value)
	{
		for(TIntObjectIterator<TIntList> iterator = map.iterator(); iterator.hasNext();) {
			iterator.advance();
			if (iterator.value().contains(value))
				return true;
		}
		return false;
	}

	public boolean containsValue(int key, int value)
	{
		final TIntList coll = map.get(key);
		return coll != null && coll.contains(value);
	}

	public int size(final Integer key)
	{
		final TIntList coll = map.get(key);
		if(coll == null)
			return 0;
		return coll.size();
	}

	public boolean putAll(int key, TIntCollection values)
	{
		if(values == null || values.size() == 0)
			return false;

		boolean result = false;
		TIntList coll = map.get(key);
		if(coll == null)
		{
			coll = new TSynchronizedIntList(new TIntArrayList());
			coll.addAll(values);
			if(coll.size() > 0)
			{
				map.put(key, coll);
				result = true;
			}
		}
		else
			result = coll.addAll(values);

		return result;
	}

	public int totalSize()
	{
		int total = 0;
		for(TIntObjectIterator<TIntList> iterator = map.iterator(); iterator.hasNext();) {
			iterator.advance();
			total += iterator.value().size();
		}
		return total;
	}
}
