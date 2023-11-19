package l2s.gameserver.network.authcomm.as2gs;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.dao.AccountBonusDAO;
import l2s.gameserver.network.authcomm.AuthServerCommunication;
import l2s.gameserver.network.authcomm.ReceivablePacket;
import l2s.gameserver.network.authcomm.SessionKey;
import l2s.gameserver.network.authcomm.gs2as.PlayerInGame;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfo;
import l2s.gameserver.network.l2.s2c.LoginFail;

public class PlayerAuthResponse extends ReceivablePacket
{
	private String account;
	private boolean authed;
	private int playOkId1;
	private int playOkId2;
	private int loginOkId1;
	private int loginOkId2;
	private double bonus;
	private int bonusExpire;
	private int points;
	private String hwid;
	private long phoneNumber;

	@Override
	public void readImpl()
	{
		account = readS();
		authed = readC() == 1;
		if(authed)
		{
			playOkId1 = readD();
			playOkId2 = readD();
			loginOkId1 = readD();
			loginOkId2 = readD();
			bonus = readF();
			bonusExpire = readD();
			points = readD();
			hwid = readS();
			if(getByteBuffer().hasRemaining())
				phoneNumber = readQ();
		}
	}

	@Override
	protected void runImpl()
	{
		final SessionKey skey = new SessionKey(loginOkId1, loginOkId2, playOkId1, playOkId2);
		final GameClient client = AuthServerCommunication.getInstance().removeWaitingClient(account);
		if(client == null)
			return;

		if(authed && client.getSessionKey().equals(skey))
		{
			if(Config.SERVICES_LOCK_ACC_HWID && client.accLockHWID())
			{
				client.close(Msg.LeaveWorld);
				return;
			}
			client.setAuthed(true);
			client.setState(GameClient.GameClientState.AUTHED);

			switch(Config.SERVICES_RATE_TYPE)
			{
				case 0:
				{
					bonus = 1.0;
					bonusExpire = 0;
					break;
				}
				case 2:
				{
					final double[] bonuses = AccountBonusDAO.getInstance().select(account);
					bonus = bonuses[0];
					bonusExpire = (int) bonuses[1];
					break;
				}
			}
			client.setBonus((float) bonus);
			client.setBonusExpire(bonusExpire);
	
			final GameClient oldClient = AuthServerCommunication.getInstance().addAuthedClient(client);
			if(oldClient != null)
				oldClient.kick();
			this.sendPacket(new PlayerInGame(client.getLogin()));
			final CharacterSelectionInfo csi = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
			client.sendPacket(csi);
			client.setCharSelection(csi.getCharInfo());
		}
		else
			client.close(LoginFail.ACCESS_FAILED_TRY_LATER);
	}
}
