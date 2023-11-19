package l2s.gameserver.listener.engine;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import l2s.gameserver.listener.MethodInvokeListener;
import l2s.gameserver.listener.PropertyChangeListener;
import l2s.gameserver.listener.events.DefaultMethodInvokeEvent;
import l2s.gameserver.listener.events.DefaultPropertyChangeEvent;
import l2s.gameserver.listener.events.MethodEvent;
import l2s.gameserver.listener.events.PropertyEvent;

public class DefaultListenerEngine<T> implements ListenerEngine<T>
{
	protected LinkedBlockingQueue<PropertyChangeListener> propertyChangeListeners;
	protected ConcurrentHashMap<String, LinkedBlockingQueue<PropertyChangeListener>> mappedPropertyChangeListeners;
	protected HashMap<String, Object> properties;
	protected LinkedBlockingQueue<MethodInvokeListener> methodInvokedListeners;
	protected ConcurrentHashMap<String, LinkedBlockingQueue<MethodInvokeListener>> mappedMethodInvokedListeners;
	private final T owner;

	public DefaultListenerEngine(final T owner)
	{
		this.owner = owner;
	}

	@Override
	public void addPropertyChangeListener(final PropertyChangeListener listener)
	{
		if(this.propertyChangeListeners == null)
			this.propertyChangeListeners = new LinkedBlockingQueue<PropertyChangeListener>();
		this.propertyChangeListeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(final PropertyChangeListener listener)
	{
		if(this.propertyChangeListeners == null)
			return;
		this.propertyChangeListeners.remove(listener);
	}

	@Override
	public void addPropertyChangeListener(final String value, final PropertyChangeListener listener)
	{
		if(this.mappedPropertyChangeListeners == null)
			this.mappedPropertyChangeListeners = new ConcurrentHashMap<String, LinkedBlockingQueue<PropertyChangeListener>>();
		LinkedBlockingQueue<PropertyChangeListener> listeners = this.mappedPropertyChangeListeners.get(value);
		if(listeners == null)
		{
			listeners = new LinkedBlockingQueue<PropertyChangeListener>();
			this.mappedPropertyChangeListeners.put(value, listeners);
		}
		listeners.add(listener);
	}

	@Override
	public void removePropertyChangeListener(final String value, final PropertyChangeListener listener)
	{
		if(this.mappedPropertyChangeListeners == null)
			return;
		final LinkedBlockingQueue<PropertyChangeListener> listeners = this.mappedPropertyChangeListeners.get(value);
		if(listeners == null)
			return;
		listeners.remove(listener);
	}

	@Override
	public void firePropertyChanged(final String value, final T source, final Object oldValue, final Object newValue)
	{
		this.firePropertyChanged(new DefaultPropertyChangeEvent(value, source, oldValue, newValue));
	}

	@Override
	public void firePropertyChanged(final PropertyEvent event)
	{
		if(this.propertyChangeListeners != null)
			for(final PropertyChangeListener l : this.propertyChangeListeners)
				if(l.accept(event.getProperty()))
					l.propertyChanged(event);
		if(this.mappedPropertyChangeListeners == null)
			return;
		final LinkedBlockingQueue<PropertyChangeListener> listeners = this.mappedPropertyChangeListeners.get(event.getProperty());
		if(listeners == null)
			return;
		for(final PropertyChangeListener i : listeners)
			i.propertyChanged(event);
	}

	@Override
	public void addProperty(final String property, final Object value)
	{
		if(this.properties == null)
			this.properties = new HashMap<String, Object>();
		final Object old = this.properties.get(property);
		this.properties.put(property, value);
		this.firePropertyChanged(property, this.getOwner(), old, value);
	}

	@Override
	public Object getProperty(final String property)
	{
		if(this.properties == null)
			return null;
		return this.properties.get(property);
	}

	@Override
	public T getOwner()
	{
		return this.owner;
	}

	@Override
	public void addMethodInvokedListener(final MethodInvokeListener listener)
	{
		if(this.methodInvokedListeners == null)
			this.methodInvokedListeners = new LinkedBlockingQueue<MethodInvokeListener>();
		this.methodInvokedListeners.add(listener);
	}

	@Override
	public void removeMethodInvokedListener(final MethodInvokeListener listener)
	{
		if(this.methodInvokedListeners == null)
			return;
		this.methodInvokedListeners.remove(listener);
	}

	@Override
	public void addMethodInvokedListener(final String methodName, final MethodInvokeListener listener)
	{
		if(this.mappedMethodInvokedListeners == null)
			this.mappedMethodInvokedListeners = new ConcurrentHashMap<String, LinkedBlockingQueue<MethodInvokeListener>>();
		LinkedBlockingQueue<MethodInvokeListener> listeners = this.mappedMethodInvokedListeners.get(methodName);
		if(listeners == null)
		{
			listeners = new LinkedBlockingQueue<MethodInvokeListener>();
			this.mappedMethodInvokedListeners.put(methodName, listeners);
		}
		listeners.add(listener);
	}

	@Override
	public void removeMethodInvokedListener(final String methodName, final MethodInvokeListener listener)
	{
		if(this.mappedMethodInvokedListeners == null)
			return;
		final LinkedBlockingQueue<MethodInvokeListener> a = this.mappedMethodInvokedListeners.get(methodName);
		if(a == null)
			return;
		a.remove(listener);
	}

	@Override
	public void fireMethodInvoked(final MethodEvent event)
	{
		if(this.methodInvokedListeners != null)
			for(final MethodInvokeListener listener : this.methodInvokedListeners)
				if(listener.accept(event))
					listener.methodInvoked(event);
		if(this.mappedMethodInvokedListeners == null)
			return;
		final LinkedBlockingQueue<MethodInvokeListener> list = this.mappedMethodInvokedListeners.get(event.getMethodName());
		if(list == null)
			return;
		for(final MethodInvokeListener lsr : list)
			if(lsr.accept(event))
				lsr.methodInvoked(event);
	}

	@Override
	public void fireMethodInvoked(final String methodName, final T source, final Object[] args)
	{
		this.fireMethodInvoked(new DefaultMethodInvokeEvent(methodName, source, args));
	}
}
