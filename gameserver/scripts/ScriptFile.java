package l2s.gameserver.scripts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface ScriptFile
{
	public static final Logger _log = LoggerFactory.getLogger(ScriptFile.class);

	void onLoad();

	void onReload();

	void onShutdown();
}
