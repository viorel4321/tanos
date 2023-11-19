package l2s.gameserver.model.reward;

import java.util.List;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.ItemToDrop;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class DropData implements Cloneable
{
	private ItemTemplate _item;
	private int _mindrop;
	private int _maxdrop;
	private boolean _sweep;
	private double _chance;
	private double _chanceInGroup;
	private int _groupId;
	private int _minLevel;
	private int _maxLevel;

	public DropData()
	{}

	public DropData(final int id, final int min, final int max, final double chance)
	{
		this(id, min, max, chance, 0, 0);
	}

	public DropData(final int id, final int min, final int max, final double chance, final int minLevel)
	{
		this(id, min, max, chance, minLevel, 0);
	}

	public DropData(final int id, final int min, final int max, final double chance, final int minLevel, final int maxLevel)
	{
		_item = ItemTable.getInstance().getTemplate(id);
		_mindrop = min;
		_maxdrop = max;
		_chance = chance;
		_minLevel = minLevel;
		_maxLevel = maxLevel;
	}

	@Override
	public DropData clone()
	{
		return new DropData(getItemId(), getMinDrop(), getMaxDrop(), getChance(), getMinLevel(), getMaxLevel());
	}

	@Override
	public boolean equals(final Object o)
	{
		if(o instanceof DropData)
		{
			final DropData drop = (DropData) o;
			return drop.getItemId() == getItemId();
		}
		return false;
	}

	public double getChance()
	{
		return _chance;
	}

	public double getChanceInGroup()
	{
		return _chanceInGroup;
	}

	public int getGroupId()
	{
		return _groupId;
	}

	public ItemTemplate getItem()
	{
		return _item;
	}

	public int getItemId()
	{
		return _item.getItemId();
	}

	public int getMaxDrop()
	{
		return _maxdrop;
	}

	public int getMaxLevel()
	{
		return _maxLevel;
	}

	public int getMinDrop()
	{
		return _mindrop;
	}

	public int getMinLevel()
	{
		return _minLevel;
	}

	public String getName()
	{
		return getItem().getName();
	}

	@Override
	public int hashCode()
	{
		return _item.getItemId();
	}

	public boolean isSweep()
	{
		return _sweep;
	}

	public List<ItemToDrop> roll(final Player player, final double mod, final boolean isRaid, final boolean isBox, final boolean isChest)
	{
		final float rate = (isRaid ? Config.RATE_DROP_RAIDBOSS : isBox ? Config.RATE_DROP_BOX : isChest ? Config.RATE_DROP_CHEST : Config.RATE_DROP_ITEMS) * (player != null ? player.getRateItems() : 1.0f);
		final float adenarate = Config.getRateAdena(player);
		double calcChance = mod * _chance * (_item.isAdena() ? 1.0f : rate);
		int dropmult = 1;
		if(calcChance > 1000000.0)
			if(calcChance % 1000000.0 == 0.0)
				dropmult = (int) (calcChance / 1000000.0);
			else
			{
				dropmult = (int) Math.ceil(calcChance / 1000000.0);
				calcChance /= dropmult;
			}
		final List<ItemToDrop> ret = new LazyArrayList<ItemToDrop>();
		for(int i = 1; i <= dropmult; ++i)
			if(Rnd.get(1000000) <= calcChance)
			{
				final ItemToDrop t = new ItemToDrop(_item.getItemId());
				final float mult = _item.isAdena() ? adenarate : 1.0f;
				if(getMinDrop() >= getMaxDrop())
					t.count = (int) (getMinDrop() * mult);
				else
					t.count = (int) (Rnd.get(getMinDrop(), getMaxDrop()) * mult);
				ret.add(t);
			}
		return ret;
	}

	public void setChance(final double chance)
	{
		_chance = chance;
	}

	public void setChanceInGroup(final double chance)
	{
		_chanceInGroup = chance;
	}

	public void setGroupId(final int gId)
	{
		_groupId = gId;
	}

	public void setItemId(final int itemId)
	{
		_item = ItemTable.getInstance().getTemplate(itemId);
	}

	public void setMaxDrop(final int maxdrop)
	{
		_maxdrop = maxdrop;
	}

	public void setMinDrop(final int mindrop)
	{
		_mindrop = mindrop;
	}

	public void setSweep(final boolean sweep)
	{
		_sweep = sweep;
	}

	@Override
	public String toString()
	{
		return "ItemID: " + getItem() + " Min: " + getMinDrop() + " Max: " + getMaxDrop() + " Chance: " + getChance() / 10000.0 + "%";
	}
}
