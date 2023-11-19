package l2s.gameserver.network.authcomm;

import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;

public class KickPlayerInGameTask implements Runnable
{
	private final GameClient client;

	public KickPlayerInGameTask(final GameClient client)
	{
		this.client = client;
	}

	@Override
	public void run()
	{
		final Player activeChar = client.getActiveChar();
		if(activeChar != null)
			activeChar.kick(true);
		else
			client.close(Msg.ServerClose);
	}
}
