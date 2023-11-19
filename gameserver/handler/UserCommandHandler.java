package l2s.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;

public class UserCommandHandler
{
	private static Logger _log;
	private static UserCommandHandler _instance;
	private Map<Integer, IUserCommandHandler> _datatable;

	public static UserCommandHandler getInstance()
	{
		if(UserCommandHandler._instance == null)
			UserCommandHandler._instance = new UserCommandHandler();
		return UserCommandHandler._instance;
	}

	private UserCommandHandler()
	{
		_datatable = new HashMap<Integer, IUserCommandHandler>();
	}

	public void registerUserCommandHandler(final IUserCommandHandler handler)
	{
		final int[] userCommandList;
		final int[] ids = userCommandList = handler.getUserCommandList();
		for(final int element : userCommandList)
		{
			if(Config.DEBUG)
				UserCommandHandler._log.info("Adding handler for user command " + element);
			_datatable.put(element, handler);
		}
	}

	public IUserCommandHandler getUserCommandHandler(final int userCommand)
	{
		if(Config.DEBUG)
			UserCommandHandler._log.info("getting handler for user command: " + userCommand);
		return _datatable.get(userCommand);
	}

	public int size()
	{
		return _datatable.size();
	}

	public void clear()
	{
		_datatable.clear();
	}

	static
	{
		UserCommandHandler._log = LoggerFactory.getLogger(UserCommandHandler.class);
	}
}
