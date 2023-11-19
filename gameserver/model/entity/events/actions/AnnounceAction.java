package l2s.gameserver.model.entity.events.actions;

import l2s.gameserver.model.entity.events.EventAction;
import l2s.gameserver.model.entity.events.GlobalEvent;

public class AnnounceAction implements EventAction
{
	private int _id;

	public AnnounceAction(final int id)
	{
		_id = id;
	}

	@Override
	public void call(final GlobalEvent event)
	{
		event.announce(_id);
	}
}
