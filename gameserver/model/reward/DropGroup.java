package l2s.gameserver.model.reward;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.base.Experience;
import l2s.gameserver.model.base.ItemToDrop;
import l2s.gameserver.model.instances.MonsterInstance;

public class DropGroup implements Cloneable
{
	private int _id;
	private double _chance;
	private boolean _isAdena;
	private boolean _fixedQty;
	private boolean _notRate;
	private List<DropData> _items;

	public static Map.Entry<Double, Integer> balanceChanceAndMult(Double calcChance)
	{
		Integer dropmult = 1;
		if(calcChance > 1000000.0)
		{
			if(calcChance % 1000000.0 == 0.0)
				dropmult = (int) (calcChance / 1000000.0);
			else
				dropmult = (int) Math.ceil(calcChance / 1000000.0);
			calcChance /= dropmult;
		}
		return new AbstractMap.SimpleEntry<Double, Integer>(calcChance, dropmult);
	}

	public DropGroup(final int id)
	{
		_isAdena = false;
		_fixedQty = false;
		_notRate = false;
		_items = new LazyArrayList<DropData>();
		_id = id;
	}

	public void addDropItem(final DropData _item)
	{
		if(_item.getItem().isAdena())
			_isAdena = true;
		if(_item.getItem().isArrow() || _item.getItem().isHerb() || Config.NO_RATE_RECIPES && _item.getItem().isRecipe() || ArrayUtils.contains(Config.NO_RATE_ITEMS, _item.getItemId()))
			_notRate = true;
		if(_item.getItem().isEquipment() || _item.getItem().isKeyMatherial())
			_fixedQty = true;
		_item.setChanceInGroup(_chance);
		_chance += _item.getChance();
		_items.add(_item);
	}

	@Override
	public DropGroup clone()
	{
		final DropGroup ret = new DropGroup(_id);
		for(final DropData i : _items)
			ret.addDropItem(i.clone());
		return ret;
	}

	public boolean fixedQty()
	{
		return _fixedQty;
	}

	public double getChance()
	{
		return _chance;
	}

	public List<DropData> getDropItems(final boolean copy)
	{
		if(!copy)
			return _items;
		final List<DropData> temp = new LazyArrayList<DropData>();
		temp.addAll(_items);
		return temp;
	}

	public int getId()
	{
		return _id;
	}

	public List<DropData> getRatedItems(final double mod)
	{
		if(mod == 1.0 || _notRate)
			return _items;
		final List<DropData> ret = new LazyArrayList<DropData>();
		for(final DropData i : _items)
			ret.add(i.clone());
		if(Config.ALT_SINGLE_DROP && ret.size() == 1)
		{
			final DropData j = ret.get(0);
			if(j.getChance() == 1000000.0)
			{
				j.setMinDrop((int) (j.getMinDrop() * mod));
				j.setMaxDrop((int) (j.getMaxDrop() * mod));
			}
			else
			{
				final double c = j.getChance() * mod;
				int n = (int) (c / 1000000.0);
				if(n > 0)
				{
					j.setChance(1000000.0);
					final double rc = c % 1000000.0 / 10000.0;
					if(Rnd.chance(rc))
						++n;
					j.setMinDrop(j.getMinDrop() * n);
					j.setMaxDrop(j.getMaxDrop() * n);
				}
				else
					j.setChance(c);
			}
			return ret;
		}
		final double perItemChance = 1000000.0 / ret.size();
		double gChance = 0.0;
		for(final DropData k : ret)
		{
			final double avgQty = (k.getMinDrop() + k.getMaxDrop()) / 2.0;
			final double newChance = mod * k.getChance() * avgQty;
			int min;
			int max;
			final int avgCount = max = min = (int) Math.ceil(newChance / perItemChance);
			final long shift = Math.min(Math.round(avgCount * 1.0 / 3.0), avgCount - 1);
			if(shift > 0L)
			{
				min -= (int) shift;
				max += (int) shift;
			}
			k.setMinDrop(min);
			k.setMaxDrop(max);
			k.setChance(newChance / avgCount);
			k.setChanceInGroup(gChance);
			gChance += k.getChance();
		}
		return ret;
	}

	public boolean isAdena()
	{
		return _isAdena;
	}

	public boolean notRate()
	{
		return _notRate;
	}

	public List<ItemToDrop> roll(final int diff, final boolean isSpoil, final MonsterInstance monster, final Player player, final double mod)
	{
		if(_isAdena)
			return rollAdena(diff, player, mod);
		if(isSpoil)
			return rollSpoil(diff, player, mod);
		if(monster.isRaid() || _notRate || _fixedQty || monster.getChampion() > 0 && Config.RATE_DROP_ITEMS * player.getRateItems() <= 10.0f)
			return rollFixedQty(diff, monster, player, mod);
		double cmod = 0.0;
		if(monster.isBox() || monster.isChest())
			cmod = mod * (monster.isBox() ? Config.RATE_DROP_BOX * player.getRateItems() : Config.RATE_DROP_CHEST * player.getRateItems());
		else
			cmod = mod * (monster.isRB() ? Config.RATE_DROP_RAIDBOSS : monster.isEpicBoss() ? Config.RATE_DROP_EPICBOSS : Config.RATE_DROP_ITEMS * player.getRateItems());
		if(cmod > Config.RATE_BREAKPOINT)
		{
			final long iters = Math.min((long) Math.ceil(cmod / Config.RATE_BREAKPOINT), Config.MAX_DROP_ITERATIONS);
			final List<ItemToDrop> ret = new LazyArrayList<ItemToDrop>();
			for(int i = 0; i < iters; ++i)
				ret.addAll(rollNormal(diff, monster, player, mod / iters));
			return ret;
		}
		return rollNormal(diff, monster, player, mod);
	}

	private List<ItemToDrop> rollAdena(final int diff, final Player player, double mod)
	{
		final float rate = Config.getRateAdena(player);
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9.0);
		double chance = _chance;
		if(mod > 10.0)
		{
			mod *= _chance / 1000000.0;
			chance = 1000000.0;
		}
		if(mod <= 0.0 || Rnd.get(1, 1000000) > chance)
			return null;
		final double mult = rate * mod;
		final List<ItemToDrop> ret = new LazyArrayList<ItemToDrop>(1);
		rollFinal(_items, ret, mult, _chance);
		for(final ItemToDrop i : ret)
			i.isAdena = true;
		return ret;
	}

	private void rollFinal(final List<DropData> items, final List<ItemToDrop> ret, final double mult, final double chanceSum)
	{
		final int chance = Rnd.get(0, (int) chanceSum);
		for(final DropData i : items)
		{
			if(chance < i.getChanceInGroup())
				continue;
			boolean notlast = false;
			for(final DropData t : items)
				if(t.getChanceInGroup() > i.getChanceInGroup() && chance > t.getChanceInGroup())
				{
					notlast = true;
					break;
				}
			if(notlast)
				continue;
			final ItemToDrop t2 = new ItemToDrop(i.getItemId());
			if(i.getMinDrop() >= i.getMaxDrop())
				t2.count = (int) Math.round(i.getMinDrop() * mult);
			else
				t2.count = (int) Math.round(Rnd.get(i.getMinDrop(), i.getMaxDrop()) * mult);
			ret.add(t2);
			break;
		}
	}

	public List<ItemToDrop> rollFixedQty(final int diff, final MonsterInstance monster, final Player player, double mod)
	{
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9.0);
		if(mod <= 0.0)
			return null;
		double rate;
		if(_notRate)
			rate = Math.min(mod, 1.0);
		else if(monster.isRB())
			rate = Config.RATE_DROP_RAIDBOSS * mod;
		else if(monster.isEpicBoss())
			rate = Config.RATE_DROP_EPICBOSS * mod;
		else if(monster.isBox() || monster.isChest())
			rate = monster.isBox() ? Config.RATE_DROP_BOX * mod : Config.RATE_DROP_CHEST * mod;
		else
			rate = Config.RATE_DROP_ITEMS * player.getRateItems() * mod;
		double calcChance = _chance * rate;
		final Map.Entry<Double, Integer> e = balanceChanceAndMult(calcChance);
		calcChance = e.getKey();
		final int dropmult = e.getValue();
		final List<ItemToDrop> ret = new LazyArrayList<ItemToDrop>();
		for(int n = 0; n < dropmult; ++n)
			if(Rnd.get(1, 1000000) < calcChance)
				rollFinal(_items, ret, 1.0, _chance);
		return ret;
	}

	public List<ItemToDrop> rollNormal(final int diff, final MonsterInstance monster, final Player player, double mod)
	{
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9.0);
		if(mod <= 0.0)
			return null;
		float rate;
		if(monster.isRB())
			rate = Config.RATE_DROP_RAIDBOSS * player.getRateItems();
		else if(monster.isEpicBoss())
			rate = Config.RATE_DROP_EPICBOSS * player.getRateItems();
		else if(monster.isBox() || monster.isChest())
			rate = monster.isBox() ? Config.RATE_DROP_BOX * player.getRateItems() : Config.RATE_DROP_CHEST * player.getRateItems();
		else
			rate = Config.RATE_DROP_ITEMS * player.getRateItems();
		double calcChance = 0.0;
		double rollChance = 0.0;
		final double mult = 1.0;
		final List<DropData> items = getRatedItems(rate * mod);
		for(final DropData i : items)
			calcChance += i.getChance();
		rollChance = calcChance;
		if(Rnd.get(1, 1000000) > calcChance)
			return null;
		final List<ItemToDrop> ret = new LazyArrayList<ItemToDrop>();
		rollFinal(items, ret, mult, rollChance);
		return ret;
	}

	private List<ItemToDrop> rollSpoil(final int diff, final Player player, double mod)
	{
		final float rate = Config.RATE_DROP_SPOIL * player.getRateSpoil();
		if(Config.DEEPBLUE_DROP_RULES && diff > 0)
			mod *= Experience.penaltyModifier(diff, 9.0);
		if(mod <= 0.0)
			return null;
		double calcChance = _chance * rate * mod;
		final Map.Entry<Double, Integer> e = balanceChanceAndMult(calcChance);
		calcChance = e.getKey();
		final int dropmult = e.getValue();
		if(Rnd.get(1, 1000000) > calcChance)
			return null;
		final List<ItemToDrop> ret = new LazyArrayList<ItemToDrop>(1);
		rollFinal(_items, ret, dropmult, _chance);
		return ret;
	}

	public void setChance(final double chance)
	{
		_chance = chance;
	}
}
