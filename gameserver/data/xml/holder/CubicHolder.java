package l2s.gameserver.data.xml.holder;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.data.xml.AbstractHolder;
import l2s.gameserver.templates.CubicTemplate;

public final class CubicHolder extends AbstractHolder
{
	private static CubicHolder _instance;
	private final TIntObjectHashMap<CubicTemplate> _cubics;

	public static CubicHolder getInstance()
	{
		return CubicHolder._instance;
	}

	private CubicHolder()
	{
		_cubics = new TIntObjectHashMap<CubicTemplate>(10);
	}

	public void addCubicTemplate(final CubicTemplate template)
	{
		_cubics.put(hash(template.getId(), template.getLevel()), template);
	}

	public CubicTemplate getTemplate(final int id, final int level)
	{
		return _cubics.get(hash(id, level));
	}

	public int hash(final int id, final int level)
	{
		return id * 10000 + level;
	}

	@Override
	public int size()
	{
		return _cubics.size();
	}

	@Override
	public void clear()
	{
		_cubics.clear();
	}

	static
	{
		CubicHolder._instance = new CubicHolder();
	}
}
