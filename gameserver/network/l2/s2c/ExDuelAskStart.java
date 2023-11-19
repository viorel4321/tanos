package l2s.gameserver.network.l2.s2c;

public class ExDuelAskStart extends L2GameServerPacket
{
	String _requestor;
	int _isPartyDuel;

	public ExDuelAskStart(final String requestor, final int isPartyDuel)
	{
		_requestor = requestor;
		_isPartyDuel = isPartyDuel;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(75);
		writeS((CharSequence) _requestor);
		writeD(_isPartyDuel);
	}
}
