package l2s.gameserver.model.items;

import java.util.concurrent.ConcurrentLinkedQueue;

import l2s.gameserver.model.items.listeners.PaperdollListener;

@Deprecated
public class PcInventoryDummy extends PcInventory
{
	public static final PcInventoryDummy instance;
	static final ItemInstance[] noitems;
	static final ConcurrentLinkedQueue<ItemInstance> noitems_list;

	public PcInventoryDummy()
	{
		super(null);
	}

	@Override
	public ItemInstance[] getItems()
	{
		return PcInventoryDummy.noitems;
	}

	@Override
	public ConcurrentLinkedQueue<ItemInstance> getItemsList()
	{
		return PcInventoryDummy.noitems_list;
	}

	@Override
	public ItemInstance addAdena(final long adena)
	{
		return null;
	}

	@Override
	public synchronized void deleteMe()
	{}

	@Override
	public void updateDatabase(final boolean commit)
	{}

	@Override
	public ItemInstance destroyItem(final int objectId, final long count, final boolean toLog)
	{
		return null;
	}

	@Override
	public ItemInstance destroyItem(final ItemInstance item, final long count, final boolean toLog)
	{
		return null;
	}

	@Override
	public ItemInstance dropItem(final int objectId, final long count)
	{
		return null;
	}

	@Override
	public ItemInstance dropItem(final ItemInstance item, final long count, final boolean whflag)
	{
		return null;
	}

	@Override
	public void restore()
	{}

	@Override
	public ItemInstance destroyItemByItemId(final int itemId, final long count, final boolean toLog)
	{
		return null;
	}

	@Override
	public boolean validateCapacity(final int slots)
	{
		return false;
	}

	@Override
	public int slotsLeft()
	{
		return 0;
	}

	@Override
	public boolean validateWeight(final long weight)
	{
		return false;
	}

	@Override
	public ItemInstance addItem(final int id, final long count)
	{
		return null;
	}

	@Override
	public ItemInstance getPaperdollItem(final int slot)
	{
		return null;
	}

	@Override
	public int getPaperdollItemId(final int slot)
	{
		return 0;
	}

	@Override
	public int getPaperdollObjectId(final int slot)
	{
		return 0;
	}

	@Override
	public synchronized void addPaperdollListener(final PaperdollListener listener)
	{}

	@Override
	public synchronized void removePaperdollListener(final PaperdollListener listener)
	{}

	@Override
	public ItemInstance setPaperdollItem(final int slot, final ItemInstance item)
	{
		return null;
	}

	@Override
	public void unEquipItemInBodySlotAndNotify(final int slot, final ItemInstance item)
	{}

	@Override
	public ItemInstance unEquipItemInSlot(final int pdollSlot)
	{
		return null;
	}

	@Override
	public void unEquipItemInBodySlot(final int slot, final ItemInstance item)
	{}

	@Override
	public synchronized void equipItem(final ItemInstance item, final boolean checks)
	{}

	@Override
	public ItemInstance findEquippedLure()
	{
		return null;
	}

	@Override
	public void validateItems()
	{}

	static
	{
		instance = new PcInventoryDummy();
		noitems = new ItemInstance[0];
		noitems_list = new ConcurrentLinkedQueue<ItemInstance>();
	}
}
