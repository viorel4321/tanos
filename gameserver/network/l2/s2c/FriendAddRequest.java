package l2s.gameserver.network.l2.s2c;

public class FriendAddRequest extends L2GameServerPacket
{
	private String _requestorName;

	public FriendAddRequest(final String requestorName)
	{
		_requestorName = requestorName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(125);
		writeS((CharSequence) _requestorName);
		writeD(0);
	}
}
