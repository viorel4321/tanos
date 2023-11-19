package l2s.gameserver.handler.items;

import org.apache.commons.lang3.ArrayUtils;

import l2s.gameserver.model.Playable;
import l2s.gameserver.model.items.ItemInstance;

public interface IItemHandler
{
	public static final IItemHandler NULL = new IItemHandler()
	{
		@Override
		public boolean useItem(final Playable p0, final ItemInstance p1, final Boolean p2)
		{
			return false;
		}

		public int[] getItemIds()
		{
			return ArrayUtils.EMPTY_INT_ARRAY;
		}
	};

	public boolean useItem(final Playable p0, final ItemInstance p1, final Boolean p2);

	public int[] getItemIds();
}
