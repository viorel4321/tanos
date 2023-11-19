package l2s.gameserver.network.l2.c2s;

import l2s.commons.ban.BanBindType;
import l2s.gameserver.Config;
import l2s.gameserver.Shutdown;
import l2s.gameserver.instancemanager.AuthBanManager;
import l2s.gameserver.instancemanager.GameBanManager;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerAuthRequest;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.LoginFail;
import l2s.gameserver.utils.Language;

import java.nio.BufferUnderflowException;

public class AuthLogin extends L2GameClientPacket
{
	private String _loginName;
	private int _playKey1;
	private int _playKey2;
	private int _loginKey1;
	private int _loginKey2;
	private int _lang;
	private boolean _cond;

	@Override
	protected void readImpl()
	{
		try
		{
			_loginName = readS(32).toLowerCase();
		}
		catch(BufferUnderflowException e)
		{
			_cond = true;
			return;
		}
		_playKey2 = readD();
		_playKey1 = readD();
		_loginKey1 = readD();
		_loginKey2 = readD();
		_lang = readD();
	}

	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		if(_cond || !client.isProtocolOk())
		{
			client.closeNow(true);
			return;
		}
		if(Shutdown.getInstance().getMode() != -1 && Shutdown.getInstance().getSeconds() <= 30)
		{
			client.closeNow(false);
			return;
		}
		if(AuthServerCommunication.getInstance().isShutdown())
		{
			client.close(LoginFail.SYSTEM_ERROR_LOGIN_LATER);
			return;
		}
		if(GameBanManager.getInstance().isBanned(BanBindType.LOGIN, client.getLogin()))
		{
			client.close(LoginFail.ACCESS_FAILED_TRY_LATER);
			return;
		}
		if(GameBanManager.getInstance().isBanned(BanBindType.IP, client.getIpAddr()))
		{
			client.close(LoginFail.ACCESS_FAILED_TRY_LATER);
			return;
		}
		if(AuthBanManager.getInstance().isBanned(BanBindType.HWID, client.getHWID()) || GameBanManager.getInstance().isBanned(BanBindType.HWID, client.getHWID()))
		{
			client.close(LoginFail.ACCESS_FAILED_TRY_LATER);
			return;
		}
		final SessionKey key = new SessionKey(_loginKey1, _loginKey2, _playKey1, _playKey2);
		client.setSessionId(key);
		client.setLoginName(_loginName);
		client.setLanguage(Language.getLanguage(_lang));
		final GameClient oldClient = AuthServerCommunication.getInstance().addWaitingClient(client);
		if(oldClient != null)
			oldClient.close(Msg.ServerClose);
		Config.addClient(_loginName, client);
		AuthServerCommunication.getInstance().sendPacket(new PlayerAuthRequest(client));
	}
}
