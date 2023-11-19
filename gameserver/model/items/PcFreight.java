package l2s.gameserver.model.items;

import l2s.gameserver.model.Player;

public class PcFreight extends Warehouse
{
	private final int _ownerId;

	public PcFreight(Player owner)
	{
		_ownerId = owner.getObjectId();
	}

	public PcFreight(int ownerId)
	{
		_ownerId = ownerId;
	}

	@Override
	public int getOwnerId()
	{
		return _ownerId;
	}

	@Override
	public ItemInstance.ItemLocation getLocationType()
	{
		return ItemInstance.ItemLocation.FREIGHT;
	}
}
