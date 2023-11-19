package l2s.gameserver.model.items.listeners;

import l2s.gameserver.model.items.ItemInstance;

public interface PaperdollListener
{
	void notifyEquipped(final int p0, final ItemInstance p1);

	void notifyUnequipped(final int p0, final ItemInstance p1);
}
