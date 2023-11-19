package l2s.gameserver.listener.events;

public interface MethodEvent
{
	Object getOwner();

	Object[] getArgs();

	String getMethodName();
}
