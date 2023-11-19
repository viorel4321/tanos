package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class FriendStatus extends L2GameServerPacket
{
	private String _charName;
	boolean _login;

	public FriendStatus(final Player player, final boolean login)
	{
		_login = false;
		if(player == null)
			return;
		_login = login;
		_charName = player.getName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(252);
		writeD(_login ? 1 : 0);
		writeS(_charName);
		writeD(0);
	}
}
