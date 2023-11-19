package l2s.gameserver.skills.conditions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.skills.Env;

public abstract class Condition implements ConditionListener
{
	public static final Condition[] EMPTY_ARRAY = new Condition[0];

	static final Logger _log = LoggerFactory.getLogger(Condition.class);

	private ConditionListener _listener;
	private String _msg;
	private boolean _result;
	private SystemMessage _message;

	public final void setSystemMsg(final int msgId)
	{
		_message = new SystemMessage(msgId);
	}

	public final SystemMessage getSystemMsg()
	{
		return _message;
	}

	public final void setMessage(final String msg)
	{
		_msg = msg;
	}

	public final String getMessage()
	{
		return _msg;
	}

	public void setListener(final ConditionListener listener)
	{
		_listener = listener;
		notifyChanged();
	}

	public final ConditionListener getListener()
	{
		return _listener;
	}

	public final boolean test(final Env env)
	{
		final boolean res = testImpl(env);
		if(_listener != null && res != _result)
		{
			_result = res;
			notifyChanged();
		}
		return res;
	}

	protected abstract boolean testImpl(final Env p0);

	@Override
	public void notifyChanged()
	{
		if(_listener != null)
			_listener.notifyChanged();
	}
}
