package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class GiveItemAction implements EventAction
{
	private int _itemId;
	private long _count;

	public GiveItemAction(final int itemId, final long count)
	{
		_itemId = itemId;
		_count = count;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		for(final Player player : event.itemObtainPlayers())
			event.giveItem(player, _itemId, _count);
	}
}
