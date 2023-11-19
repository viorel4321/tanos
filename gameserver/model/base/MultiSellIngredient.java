package l2s.gameserver.model.base;

import l2s.gameserver.tables.ItemTable;

public class MultiSellIngredient implements Cloneable
{
	private int _itemId;
	private int _itemCount;
	private int _itemEnchant;
	private boolean _mantainIngredient;

	public MultiSellIngredient(final int itemId, final int itemCount)
	{
		this(itemId, itemCount, 0);
	}

	public MultiSellIngredient(final int itemId, final int itemCount, final int enchant)
	{
		_itemId = itemId;
		_itemCount = itemCount;
		_itemEnchant = enchant;
		_mantainIngredient = false;
	}

	@Override
	public MultiSellIngredient clone()
	{
		final MultiSellIngredient mi = new MultiSellIngredient(_itemId, _itemCount, _itemEnchant);
		mi.setMantainIngredient(_mantainIngredient);
		return mi;
	}

	public void setItemId(final int itemId)
	{
		_itemId = itemId;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public void setItemCount(final int itemCount)
	{
		_itemCount = itemCount;
	}

	public int getItemCount()
	{
		return _itemCount;
	}

	public boolean isStackable()
	{
		return _itemId <= 0 || ItemTable.getInstance().getTemplate(_itemId).isStackable();
	}

	public void setItemEnchant(final int itemEnchant)
	{
		_itemEnchant = itemEnchant;
	}

	public int getItemEnchant()
	{
		return _itemEnchant;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = 31 * result + (_itemCount ^ _itemCount >>> 32);
		result = 31 * result + _itemEnchant;
		result = 31 * result + _itemId;
		return result;
	}

	@Override
	public boolean equals(final Object obj)
	{
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(this.getClass() != obj.getClass())
			return false;
		final MultiSellIngredient other = (MultiSellIngredient) obj;
		return _itemId == other._itemId && _itemCount == other._itemCount && _itemEnchant == other._itemEnchant;
	}

	public boolean getMantainIngredient()
	{
		return _mantainIngredient;
	}

	public void setMantainIngredient(final boolean mantainIngredient)
	{
		_mantainIngredient = mantainIngredient;
	}
}
