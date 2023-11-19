package l2s.gameserver.templates.player;

import java.util.ArrayList;
import java.util.List;

import l2s.commons.util.Rnd;
import l2s.gameserver.model.base.ClassId;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Location;

public class PlayerTemplate extends CreatureTemplate
{
	public final ClassId classId;
	public final Race race;
	public final String className;
	public final boolean isMale;
	public final int classBaseLevel;
	private List<ItemTemplate> _items;
	public final int baseSafeFallHeight;
	private Location[] points;
	private final double[] _hpTable;
	private final double[] _mpTable;
	private final double[] _cpTable;

	public PlayerTemplate(final StatsSet set)
	{
		super(set);
		_items = new ArrayList<ItemTemplate>();
		classId = ClassId.values()[set.getInteger("classId")];
		race = Race.values()[set.getInteger("raceId")];
		className = set.getString("className");
		isMale = set.getBool("isMale", true);
		classBaseLevel = set.getInteger("baseLvl");
		baseSafeFallHeight = set.getInteger("baseSafeFall", 333);
		if(!isMale)
		{
			collisionRadius = (float) set.getDouble("radiusFemale");
			collisionHeight = (float) set.getDouble("heightFemale");
		}
		final String[] hpTable = set.getString("hpTable").split(";");
		_hpTable = new double[hpTable.length];
		for(int i = 0; i < hpTable.length; ++i)
			_hpTable[i] = Double.parseDouble(hpTable[i]);
		final String[] mpTable = set.getString("mpTable").split(";");
		_mpTable = new double[mpTable.length];
		for(int j = 0; j < mpTable.length; ++j)
			_mpTable[j] = Double.parseDouble(mpTable[j]);
		final String[] cpTable = set.getString("cpTable").split(";");
		_cpTable = new double[cpTable.length];
		for(int k = 0; k < cpTable.length; ++k)
			_cpTable[k] = Double.parseDouble(cpTable[k]);
		final String sp = set.getString("startPoints", "");
		if(!sp.isEmpty())
		{
			final String[] locs = sp.split(";");
			points = new Location[locs.length];
			int l = 0;
			for(final String loc : locs)
			{
				points[l] = Location.parseLoc(loc);
				++l;
			}
		}
	}

	public void addItem(final int itemId)
	{
		final ItemTemplate item = ItemTable.getInstance().getTemplate(itemId);
		if(item != null)
			_items.add(item);
	}

	public ItemTemplate[] getItems()
	{
		return (ItemTemplate[]) _items.toArray((Object[]) new ItemTemplate[_items.size()]);
	}

	public Location getStartLoc()
	{
		return points[Rnd.get(points.length)];
	}

	public double getBaseHpMax(final int level)
	{
		return _hpTable[level - 1];
	}

	public double getBaseMpMax(final int level)
	{
		return _mpTable[level - 1];
	}

	public double getBaseCpMax(final int level)
	{
		return _cpTable[level - 1];
	}
}
