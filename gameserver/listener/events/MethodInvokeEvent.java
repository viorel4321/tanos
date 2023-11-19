package l2s.gameserver.listener.events;

public class MethodInvokeEvent implements MethodEvent
{
	private final Object owner;
	private final Object[] args;
	private final String methodName;

	public MethodInvokeEvent(final String methodName, final Object owner, final Object[] args)
	{
		this.methodName = methodName;
		this.owner = owner;
		this.args = args;
	}

	@Override
	public Object getOwner()
	{
		return owner;
	}

	@Override
	public Object[] getArgs()
	{
		return args;
	}

	@Override
	public String getMethodName()
	{
		return methodName;
	}
}
