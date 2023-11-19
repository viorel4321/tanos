package l2s.gameserver.model.items;

import l2s.gameserver.model.pledge.Clan;

public final class ClanWarehouse extends Warehouse
{
	private Clan _clan;

	public ClanWarehouse(final Clan clan)
	{
		_clan = clan;
	}

	@Override
	public int getOwnerId()
	{
		return _clan.getClanId();
	}

	@Override
	public ItemInstance.ItemLocation getLocationType()
	{
		return ItemInstance.ItemLocation.CLANWH;
	}
}
