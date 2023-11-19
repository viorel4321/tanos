package l2s.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VoicedCommandHandler
{
	private static final Logger _log = LoggerFactory.getLogger(VoicedCommandHandler.class);

	private static VoicedCommandHandler _instance;

	private Map<String, IVoicedCommandHandler> _datatable;

	public static VoicedCommandHandler getInstance()
	{
		if(_instance == null)
			_instance = new VoicedCommandHandler();
		return _instance;
	}

	private VoicedCommandHandler()
	{
		_datatable = new HashMap<String, IVoicedCommandHandler>();
		final Status s = new Status();
		for(final String e : s.getVoicedCommandList())
		{
			if(_log.isDebugEnabled())
				_log.debug("Adding handler for command " + e);
			_datatable.put(e, s);
		}
	}

	public void registerVoicedCommandHandler(final IVoicedCommandHandler handler)
	{
		final String[] voicedCommandList;
		final String[] ids = voicedCommandList = handler.getVoicedCommandList();
		for(final String element : voicedCommandList)
		{
			if(_log.isDebugEnabled())
				_log.debug("Adding handler for command " + element);
			_datatable.put(element, handler);
		}
	}

	public IVoicedCommandHandler getVoicedCommandHandler(final String voicedCommand)
	{
		String command = voicedCommand;
		if(voicedCommand.indexOf(" ") != -1)
			command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
		if(_log.isDebugEnabled())
			_log.debug("getting handler for command: " + command + " -> " + (_datatable.get(command) != null));
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
}
