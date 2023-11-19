package l2s.gameserver.network.l2.s2c;

public class ExAskJoinMPCC extends L2GameServerPacket
{
	private String _requestorName;

	public ExAskJoinMPCC(final String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(39);
		writeS((CharSequence) _requestorName);
	}
}
