package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class CharSit extends L2GameServerPacket
{
	private Player _activeChar;
	private int _staticObjectId;

	public CharSit(final Player player, final int staticObjectId)
	{
		_activeChar = player;
		_staticObjectId = staticObjectId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(225);
		writeD(_activeChar.getObjectId());
		writeD(_staticObjectId);
	}
}
