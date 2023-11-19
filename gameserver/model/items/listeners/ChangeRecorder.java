package l2s.gameserver.model.items.listeners;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;

public final class ChangeRecorder implements PaperdollListener
{
	private final List<ItemInstance> _changed;

	public ChangeRecorder(final Inventory inventory)
	{
		_changed = new ArrayList<ItemInstance>();
		inventory.addPaperdollListener(this);
	}

	@Override
	public void notifyEquipped(final int slot, final ItemInstance item)
	{
		if(!_changed.contains(item))
			_changed.add(item);
	}

	@Override
	public void notifyUnequipped(final int slot, final ItemInstance item)
	{
		if(!_changed.contains(item))
			_changed.add(item);
	}

	public ItemInstance[] getChangedItems()
	{
		return _changed.toArray(new ItemInstance[_changed.size()]);
	}
}
