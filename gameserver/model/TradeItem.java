package l2s.gameserver.model;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public final class TradeItem
{
	private int _objectId;
	private int _itemId;
	private int _price;
	private int _storePrice;
	private int _count;
	private int _enchantLevel;
	private int _currentvalue;
	private int _lastRechargeTime;
	private int _rechargeTime;

	public TradeItem()
	{}

	public TradeItem(final ItemInstance original)
	{
		_objectId = original.getObjectId();
		_itemId = original.getItemId();
		_count = original.getIntegerLimitedCount();
		_enchantLevel = original.getEnchantLevel();
	}

	public void setObjectId(final int id)
	{
		_objectId = id;
	}

	public int getObjectId()
	{
		return _objectId;
	}

	public void setItemId(final int id)
	{
		_itemId = id;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public ItemTemplate getItem()
	{
		return ItemTable.getInstance().getTemplate(_itemId);
	}

	public void setOwnersPrice(final int price)
	{
		_price = price;
	}

	public int getOwnersPrice()
	{
		return _price;
	}

	public void setStorePrice(final int price)
	{
		_storePrice = price;
	}

	public int getStorePrice()
	{
		return _storePrice;
	}

	public void setCount(final int count)
	{
		_count = count;
	}

	public int getCount()
	{
		return _count;
	}

	public void setEnchantLevel(final int enchant)
	{
		_enchantLevel = enchant;
	}

	public int getEnchantLevel()
	{
		return _enchantLevel;
	}

	public void setCurrentValue(final int tempvalue)
	{
		_currentvalue = tempvalue;
	}

	public int getCurrentValue()
	{
		return _currentvalue;
	}

	@Override
	public int hashCode()
	{
		return _objectId + _itemId;
	}

	public void setRechargeTime(final int rechargeTime)
	{
		_rechargeTime = rechargeTime;
	}

	public int getRechargeTime()
	{
		return _rechargeTime;
	}

	public boolean isCountLimited()
	{
		return _currentvalue > 0;
	}

	public void setLastRechargeTime(final int lastRechargeTime)
	{
		_lastRechargeTime = lastRechargeTime;
	}

	public int getLastRechargeTime()
	{
		return _lastRechargeTime;
	}
}
