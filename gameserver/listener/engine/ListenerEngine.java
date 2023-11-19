package l2s.gameserver.listener.engine;

import l2s.gameserver.listener.MethodInvokeListener;
import l2s.gameserver.listener.PropertyChangeListener;
import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.listener.events.PropertyEvent;

public interface ListenerEngine<T>
{
	void addPropertyChangeListener(final PropertyChangeListener p0);

	void removePropertyChangeListener(final PropertyChangeListener p0);

	void addPropertyChangeListener(final String p0, final PropertyChangeListener p1);

	void removePropertyChangeListener(final String p0, final PropertyChangeListener p1);

	void firePropertyChanged(final String p0, final T p1, final Object p2, final Object p3);

	void firePropertyChanged(final PropertyEvent p0);

	void addProperty(final String p0, final Object p1);

	Object getProperty(final String p0);

	T getOwner();

	void addMethodInvokedListener(final MethodInvokeListener p0);

	void removeMethodInvokedListener(final MethodInvokeListener p0);

	void addMethodInvokedListener(final String p0, final MethodInvokeListener p1);

	void removeMethodInvokedListener(final String p0, final MethodInvokeListener p1);

	void fireMethodInvoked(final MethodEvent p0);

	void fireMethodInvoked(final String p0, final T p1, final Object[] p2);
}
