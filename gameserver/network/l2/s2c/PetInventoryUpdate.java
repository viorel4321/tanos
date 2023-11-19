package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;

import l2s.gameserver.model.items.ItemInstance;

public class PetInventoryUpdate extends L2GameServerPacket
{
	private ArrayList<ItemInstance> _items;

	public PetInventoryUpdate()
	{
		_items = new ArrayList<ItemInstance>();
	}

	public PetInventoryUpdate(final ArrayList<ItemInstance> items)
	{
		_items = items;
	}

	public PetInventoryUpdate addNewItem(final ItemInstance item)
	{
		item.setLastChange((byte) 1);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addModifiedItem(final ItemInstance item)
	{
		item.setLastChange((byte) 2);
		_items.add(item);
		return this;
	}

	public PetInventoryUpdate addRemovedItem(final ItemInstance item)
	{
		item.setLastChange((byte) 3);
		_items.add(item);
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(179);
		final int count = _items.size();
		writeH(count);
		for(final ItemInstance temp : _items)
		{
			writeH(temp.getLastChange());
			writeH(temp.getTemplate().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getTemplate().getType2());
			writeH(0);
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(0);
		}
	}
}
