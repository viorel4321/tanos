package l2s.gameserver.model;

public class Recipe
{
	private int _itemId;
	private int _quantity;

	public Recipe(final int itemId, final int quantity)
	{
		_itemId = itemId;
		_quantity = quantity;
	}

	public int getItemId()
	{
		return _itemId;
	}

	public int getQuantity()
	{
		return _quantity;
	}
}
