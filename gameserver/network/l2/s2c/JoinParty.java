package l2s.gameserver.network.l2.s2c;

public class JoinParty extends L2GameServerPacket
{
	private int _response;

	public JoinParty(final int response)
	{
		_response = response;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(58);
		writeD(_response);
	}
}
