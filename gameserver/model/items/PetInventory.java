package l2s.gameserver.model.items;

import l2s.gameserver.model.instances.PetInstance;

public class PetInventory extends Inventory
{
	private final PetInstance _owner;

	public PetInventory(final PetInstance owner)
	{
		_owner = owner;
	}

	@Override
	public PetInstance getOwner()
	{
		return _owner;
	}

	@Override
	protected ItemInstance.ItemLocation getBaseLocation()
	{
		return ItemInstance.ItemLocation.INVENTORY;
	}

	@Override
	protected ItemInstance.ItemLocation getEquipLocation()
	{
		return ItemInstance.ItemLocation.PAPERDOLL;
	}

	public boolean validateWeight(final long weight)
	{
		return weight == 0L || weight >= -2147483648L && weight <= Integer.MAX_VALUE && getTotalWeight() + (int) weight >= 0 && getTotalWeight() + weight <= getOwner().getMaxLoad();
	}

	public boolean validateCapacity(final long slots)
	{
		return slots == 0L || slots >= -2147483648L && slots <= Integer.MAX_VALUE && getSize() + (int) slots >= 0 && getSize() + slots <= 12L;
	}
}
