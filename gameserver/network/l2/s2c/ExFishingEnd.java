package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExFishingEnd extends L2GameServerPacket
{
	private int _charId;
	private boolean _win;

	public ExFishingEnd(final Player character, final boolean win)
	{
		_charId = character.getObjectId();
		_win = win;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(20);
		writeD(_charId);
		writeC(_win ? 1 : 0);
	}
}
