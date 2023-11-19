package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.instancemanager.PlayerManager;
import l2s.gameserver.model.CharSelectInfo;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharDeleteFail;
import l2s.gameserver.network.l2.s2c.CharacterDeleteSuccess;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfo;
import l2s.gameserver.tables.ClanTable;

public class CharacterDelete extends L2GameClientPacket
{
	private int _charSlot;

	@Override
	protected void readImpl()
	{
		_charSlot = readD();
	}

	@Override
	protected void runImpl()
	{
		final GameClient client = getClient();
		if(client.getActiveChar() != null)
			return;
		final CharSelectInfo[] cs = client.getCharacters();
		if(_charSlot < 0 || _charSlot >= cs.length)
			return;
		final CharSelectInfo csi = cs[_charSlot];
		if(csi == null)
			return;
		final int charId = csi.getObjectId();
		if(charId <= 0)
			return;
		final Player player = GameObjectsStorage.getPlayer(charId);
		if(player != null)
		{
			sendPacket(new CharDeleteFail(CharDeleteFail.REASON_DELETION_FAILED));
			final CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
			sendPacket(cl);
			client.setCharSelection(cl.getCharInfo());
			return;
		}
		if(csi.getClanId() > 0)
		{
			final Clan clan = ClanTable.getInstance().getClan(csi.getClanId());
			if(clan != null)
			{
				if(clan.getLeaderId() == csi.getObjectId())
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
				else
					sendPacket(new CharDeleteFail(CharDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
				final CharacterSelectionInfo cl2 = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
				sendPacket(cl2);
				client.setCharSelection(cl2.getCharInfo());
				return;
			}
		}
		if(Config.SERVICES_LOCK_CHAR_HWID && client.charLockHWID(charId) && Config.SERVICES_LOCK_ACC_HWID && client.accLockHWID())
		{
			client.close(Msg.LeaveWorld);
			return;
		}
		if(Config.DELETE_DAYS == 0)
			PlayerManager.deleteCharByObjId(charId);
		else
			client.markDeleteCharByObjId(charId, true);
		sendPacket(new CharacterDeleteSuccess());
		final CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
