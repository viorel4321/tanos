package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExOlympiadUserInfo extends L2GameServerPacket
{
	private int _side;
	private Player _player;

	public ExOlympiadUserInfo(final Player player, final int side)
	{
		_player = player;
		_side = side;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(41);
		writeC(_side);
		writeD(_player.getObjectId());
		writeS((CharSequence) _player.getName());
		writeD(_player.getClassId().getId());
		writeD((int) _player.getCurrentHp());
		writeD(_player.getMaxHp());
		writeD((int) _player.getCurrentCp());
		writeD(_player.getMaxCp());
	}
}
