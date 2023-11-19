package l2s.gameserver.network.l2.s2c;

public class AskJoinPledge extends L2GameServerPacket
{
	private int _requestorId;
	private String _pledgeName;

	public AskJoinPledge(final int requestorId, final String pledgeName)
	{
		_requestorId = requestorId;
		_pledgeName = pledgeName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(50);
		writeD(_requestorId);
		writeS((CharSequence) _pledgeName);
	}
}
