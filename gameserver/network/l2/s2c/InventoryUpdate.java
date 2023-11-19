package l2s.gameserver.network.l2.s2c;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import l2s.gameserver.model.items.ItemInstance;

public class InventoryUpdate extends L2GameServerPacket
{
	private final List<ItemInfo> _items;

	public InventoryUpdate()
	{
		_items = Collections.synchronizedList(new Vector<ItemInfo>());
	}

	public InventoryUpdate(final List<ItemInstance> items)
	{
		_items = Collections.synchronizedList(new Vector<ItemInfo>());
		for(final ItemInstance item : items)
			_items.add(new ItemInfo(item));
	}

	public InventoryUpdate addNewItem(final ItemInstance item)
	{
		item.setLastChange((byte) 1);
		_items.add(new ItemInfo(item));
		return this;
	}

	public InventoryUpdate addModifiedItem(final ItemInstance item)
	{
		item.setLastChange((byte) 2);
		_items.add(new ItemInfo(item));
		return this;
	}

	public InventoryUpdate addRemovedItem(final ItemInstance item)
	{
		item.setLastChange((byte) 3);
		_items.add(new ItemInfo(item));
		return this;
	}

	public InventoryUpdate addItem(final ItemInstance item)
	{
		if(item == null)
			return null;
		switch(item.getLastChange())
		{
			case 1:
			{
				addNewItem(item);
				break;
			}
			case 2:
			{
				addModifiedItem(item);
				break;
			}
			case 3:
			{
				addRemovedItem(item);
				break;
			}
		}
		return this;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(39);
		writeH(_items.size());
		for(final ItemInfo temp : _items)
		{
			writeH(temp.getLastChange());
			writeH(temp.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getCount());
			writeH(temp.getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(temp.getVariationId1());
			writeH(temp.getVariationId2());
			writeD(temp.getShadowLifeTime());
		}
	}

	private class ItemInfo
	{
		private final short lastChange;
		private final short type1;
		private final int objectId;
		private final int itemId;
		private final int count;
		private final short type2;
		private final short customType1;
		private final boolean isEquipped;
		private final int bodyPart;
		private final short enchantLevel;
		private final short customType2;
		private final int variationId1;
		private final int variationId2;
		private final int shadowLifeTime;
		private final int equipSlot;

		private ItemInfo(final ItemInstance item)
		{
			lastChange = (short) item.getLastChange();
			type1 = (short) item.getTemplate().getType1();
			objectId = item.getObjectId();
			itemId = item.getItemId();
			count = item.getIntegerLimitedCount();
			type2 = (short) item.getTemplate().getType2();
			customType1 = (short) item.getCustomType1();
			isEquipped = item.isEquipped();
			bodyPart = item.getTemplate().getBodyPart();
			enchantLevel = (short) item.getEnchantLevel();
			customType2 = (short) item.getCustomType2();
			variationId1 = item.getVariation1Id();
			variationId2 = item.getVariation2Id();
			shadowLifeTime = item.isShadowItem() ? item.getLifeTimeRemaining() : -1;
			equipSlot = item.getEquipSlot();
		}

		public short getLastChange()
		{
			return lastChange;
		}

		public short getType1()
		{
			return type1;
		}

		public int getObjectId()
		{
			return objectId;
		}

		public int getItemId()
		{
			return itemId;
		}

		public int getCount()
		{
			return count;
		}

		public short getType2()
		{
			return type2;
		}

		public short getCustomType1()
		{
			return customType1;
		}

		public boolean isEquipped()
		{
			return isEquipped;
		}

		public int getBodyPart()
		{
			return bodyPart;
		}

		public short getEnchantLevel()
		{
			return enchantLevel;
		}

		public int getVariationId1() {
			return variationId1;
		}

		public int getVariationId2() {
			return variationId2;
		}

		public int getShadowLifeTime()
		{
			return shadowLifeTime;
		}

		public short getCustomType2()
		{
			return customType2;
		}

		public int getEquipSlot()
		{
			return equipSlot;
		}
	}
}
