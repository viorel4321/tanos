package l2s.gameserver.network.l2.s2c;

public class AskJoinAlliance extends L2GameServerPacket
{
	private String _requestorName;
	private String _requestorAllyName;
	private int _requestorId;

	public AskJoinAlliance(final int requestorId, final String requestorName, final String requestorAllyName)
	{
		_requestorName = requestorName;
		_requestorAllyName = requestorAllyName;
		_requestorId = requestorId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(168);
		writeD(_requestorId);
		writeS((CharSequence) _requestorName);
		writeS((CharSequence) "");
		writeS((CharSequence) _requestorAllyName);
	}
}
