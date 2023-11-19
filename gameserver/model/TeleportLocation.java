package l2s.gameserver.model;

import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;
import l2s.gameserver.utils.Location;

public class TeleportLocation extends Location
{
	private static final long serialVersionUID = 1L;
	private final int _price;
	private final ItemTemplate _item;
	private final String _name;
	private final int _castleId;

	public TeleportLocation(final int item, final int price, final String name, final int castleId)
	{
		_price = price;
		_name = name;
		_item = ItemTable.getInstance().getTemplate(item);
		_castleId = castleId;
	}

	public int getPrice()
	{
		return _price;
	}

	public ItemTemplate getItem()
	{
		return _item;
	}

	public String getName()
	{
		return _name;
	}

	public int getCastleId()
	{
		return _castleId;
	}
}
