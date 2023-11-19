package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;

public class ExVariationItemAdd extends L2GameServerPacket
{
	ItemInstance _item;
	int _slot;

	public ExVariationItemAdd(final ItemInstance item, final int slot)
	{
		_slot = slot;
		_item = item;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(82);
		writeD(_item.getObjectId());
		writeD(_item.getItemId());
		writeD(_slot);
	}
}
