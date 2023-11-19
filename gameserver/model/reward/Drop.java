package l2s.gameserver.model.reward;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import l2s.commons.collections.LazyArrayList;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ItemToDrop;
import l2s.gameserver.model.instances.MonsterInstance;

public class Drop
{
	public static final int MAX_CHANCE = 1000000;
	private List<DropGroup> _drop;
	private List<DropGroup> _spoil;

	public void addData(final DropData d)
	{
		if(d.isSweep())
			addSpoil(d);
		else
			addDrop(d);
	}

	public void addDrop(final DropData d)
	{
		if(_drop == null)
			_drop = new LazyArrayList<DropGroup>();
		if(_drop.size() != 0)
			for(final DropGroup g : _drop)
				if(g.getId() == d.getGroupId())
				{
					g.addDropItem(d);
					return;
				}
		final DropGroup temp = new DropGroup(d.getGroupId());
		temp.addDropItem(d);
		_drop.add(temp);
	}

	public void addSpoil(final DropData s)
	{
		if(_spoil == null)
			_spoil = new LazyArrayList<DropGroup>();
		final DropGroup temp = new DropGroup(0);
		temp.addDropItem(s);
		_spoil.add(temp);
	}

	public List<DropGroup> getNormal()
	{
		return _drop;
	}

	public List<DropGroup> getSpoil()
	{
		return _spoil;
	}

	public List<ItemToDrop> rollDrop(final int diff, final MonsterInstance monster, final Player player, final double mod)
	{
		final List<ItemToDrop> temp = new LazyArrayList<ItemToDrop>();
		if(_drop != null)
			for(final DropGroup g : _drop)
			{
				final Collection<ItemToDrop> tdl = g.roll(diff, false, monster, player, mod);
				if(tdl != null)
					if(Config.INTEGRAL_DROP)
					{
						final Map<Integer, ItemToDrop> sort = new HashMap<Integer, ItemToDrop>();
						for(final ItemToDrop itd : tdl)
							if(!sort.containsKey(itd.itemId))
								sort.put(itd.itemId, itd);
							else
							{
								final ItemToDrop itemToDrop = itd;
								itemToDrop.count += sort.get(itd.itemId).count;
								sort.remove(itd.itemId);
								sort.put(itd.itemId, itd);
							}
						if(sort.isEmpty())
							continue;
						for(final ItemToDrop itd : sort.values())
							temp.add(itd);
					}
					else
						for(final ItemToDrop itd2 : tdl)
							temp.add(itd2);
			}
		return temp;
	}

	public List<ItemToDrop> rollSpoil(final int diff, final MonsterInstance monster, final Player player, final double mod)
	{
		final List<ItemToDrop> temp = new LazyArrayList<ItemToDrop>();
		if(_spoil != null)
			for(final DropGroup g : _spoil)
			{
				final List<ItemToDrop> tdl = g.roll(diff, true, monster, player, mod);
				if(tdl != null)
					for(final ItemToDrop itd : tdl)
						if(itd.count > 0)
							temp.add(itd);
			}
		return temp;
	}

	public boolean validate()
	{
		if(_drop == null)
			return false;
		for(final DropGroup g : _drop)
		{
			int sum_chance = 0;
			for(final DropData d : g.getDropItems(false))
				sum_chance += (int) d.getChance();
			if(sum_chance <= 1000000)
				return true;
			final double mod = 1000000 / sum_chance;
			for(final DropData d2 : g.getDropItems(false))
			{
				final double group_chance = d2.getChance() * mod;
				d2.setChance(group_chance);
				g.setChance(1000000.0);
			}
		}
		return false;
	}
}
