package l2s.gameserver.listener;

import l2s.gameserver.listener.events.PropertyEvent;

public interface PropertyChangeListener
{
	void propertyChanged(final PropertyEvent p0);

	String getPropery();

	boolean accept(final String p0);
}
