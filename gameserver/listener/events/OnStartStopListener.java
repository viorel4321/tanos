package l2s.gameserver.listener.events;

import l2s.gameserver.listener.EventListener;
import l2s.gameserver.model.entity.events.GlobalEvent;

public interface OnStartStopListener extends EventListener
{
	void onStart(final GlobalEvent p0);

	void onStop(final GlobalEvent p0);
}
