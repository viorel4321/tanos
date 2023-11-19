package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;

public class RequestShortCutDel extends L2GameClientPacket
{
	private int _slot;
	private int _page;

	@Override
	public void readImpl()
	{
		final int id = readD();
		_slot = id % 12;
		_page = id / 12;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		activeChar.deleteShortCut(_slot, _page);
	}
}
