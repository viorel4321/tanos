package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.GameClient;
import l2s.gameserver.network.l2.s2c.CharacterSelectionInfo;

public class CharacterRestore extends L2GameClientPacket
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
		final int charId = client.getObjectIdByIndex(_charSlot);
		if(charId <= 0)
			return;
		client.markDeleteCharByObjId(charId, false);
		final CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
		this.sendPacket(cl);
		client.setCharSelection(cl.getCharInfo());
	}
}
