package l2s.gameserver.network.authcomm.as2gs;

import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.KickPlayerInGameTask;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.l2.GameClient;

public class KickPlayer extends ReceivablePacket
{
	String account;

	@Override
	public void readImpl()
	{
		account = readS();
	}

	@Override
	protected void runImpl()
	{
		GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
			client = AuthServerCommunication.getInstance().removeAuthedClient(account);
		if(client == null)
			return;
		final Player activeChar = client.getActiveChar();
		if(activeChar != null)
		{
			activeChar.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
			ThreadPoolManager.getInstance().schedule(new KickPlayerInGameTask(client), 500L);
		}
		else
			client.close(Msg.ServerClose);
	}
}
