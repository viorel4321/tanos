package l2s.gameserver.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;

public class AdminCommandHandler
{
	private static Logger _log;
	private static AdminCommandHandler _instance;
	private Map<String, IAdminCommandHandler> _datatable;

	public static AdminCommandHandler getInstance()
	{
		if(AdminCommandHandler._instance == null)
			AdminCommandHandler._instance = new AdminCommandHandler();
		return AdminCommandHandler._instance;
	}

	private AdminCommandHandler()
	{
		_datatable = new HashMap<String, IAdminCommandHandler>();
	}

	public void registerAdminCommandHandler(final IAdminCommandHandler handler)
	{
		final String[] adminCommandList;
		final String[] ids = adminCommandList = handler.getAdminCommandList();
		for(final String element : adminCommandList)
		{
			if(Config.DEBUG)
				AdminCommandHandler._log.info("Adding handler for command " + element);
			_datatable.put(element.toLowerCase(), handler);
		}
	}

	public IAdminCommandHandler getAdminCommandHandler(final String adminCommand)
	{
		String command = adminCommand;
		if(adminCommand.indexOf(" ") != -1)
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		if(Config.DEBUG)
			AdminCommandHandler._log.info("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
		return _datatable.get(command);
	}

	public int size()
	{
		return _datatable.size();
	}

	public void clear()
	{
		_datatable.clear();
	}

	public Set<String> getAllCommands()
	{
		return _datatable.keySet();
	}

	static
	{
		AdminCommandHandler._log = LoggerFactory.getLogger(AdminCommandHandler.class);
	}
}
