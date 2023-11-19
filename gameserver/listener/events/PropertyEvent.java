package l2s.gameserver.listener.events;

public interface PropertyEvent
{
	Object getObject();

	Object getOldValue();

	Object getNewValue();

	String getProperty();
}
