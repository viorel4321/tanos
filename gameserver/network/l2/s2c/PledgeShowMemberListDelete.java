package l2s.gameserver.network.l2.s2c;

public class PledgeShowMemberListDelete extends L2GameServerPacket
{
	private String _player;

	public PledgeShowMemberListDelete(final String playerName)
	{
		_player = playerName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(86);
		writeS((CharSequence) _player);
	}
}
