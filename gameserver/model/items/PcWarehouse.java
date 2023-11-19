package l2s.gameserver.model.items;

import l2s.gameserver.model.Player;

public class PcWarehouse extends Warehouse
{
	private final Player _owner;

	public PcWarehouse(final Player owner)
	{
		_owner = owner;
	}

	public Player getOwner()
	{
		return _owner;
	}

	@Override
	public int getOwnerId()
	{
		final Player owner = getOwner();
		return owner == null ? 0 : owner.getObjectId();
	}

	@Override
	public ItemInstance.ItemLocation getLocationType()
	{
		return ItemInstance.ItemLocation.WAREHOUSE;
	}
}
