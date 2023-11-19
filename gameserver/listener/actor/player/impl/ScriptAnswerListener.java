package l2s.gameserver.listener.actor.player.impl;

import l2s.gameserver.listener.actor.player.OnAnswerListener;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.scripts.Scripts;

public class ScriptAnswerListener implements OnAnswerListener
{
	private int _playerRef;
	private String _scriptName;
	private Object[] _arg;

	public ScriptAnswerListener(final Player player, final String scriptName, final Object[] arg)
	{
		_scriptName = scriptName;
		_arg = arg;
		_playerRef = player.getObjectId();
	}

	@Override
	public void sayYes()
	{
		final Player player = GameObjectsStorage.getPlayer(_playerRef);
		if(player == null)
			return;

		Scripts.getInstance().callScripts(player, _scriptName.split(":")[0], _scriptName.split(":")[1], _arg);
	}

	@Override
	public void sayNo()
	{}
}
