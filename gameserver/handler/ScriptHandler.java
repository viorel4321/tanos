package l2s.gameserver.handler;

public class ScriptHandler
{
	private static ScriptHandler _instance;
	private IScriptHandler _handler;

	public static ScriptHandler getInstance()
	{
		if(ScriptHandler._instance == null)
			ScriptHandler._instance = new ScriptHandler();
		return ScriptHandler._instance;
	}

	public void registerScriptHandler(final IScriptHandler handler)
	{
		_handler = handler;
	}

	public IScriptHandler getScriptHandler()
	{
		return _handler;
	}
}
