package l2s.gameserver.cache;

import java.util.HashMap;
import java.util.Map;

public abstract class InfoCache
{
	private static final Map<Integer, String> _droplistCache;

	public static void addToDroplistCache(final int id, final String list)
	{
		InfoCache._droplistCache.put(id, list);
	}

	public static String getFromDroplistCache(final int id)
	{
		return InfoCache._droplistCache.get(id);
	}

	static
	{
		_droplistCache = new HashMap<Integer, String>();
	}
}
