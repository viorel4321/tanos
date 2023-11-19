package l2s.gameserver.network.l2.s2c;

public class AskJoinParty extends L2GameServerPacket
{
	private String _requestorName;
	private int _itemDistribution;

	public AskJoinParty(final String requestorName, final int itemDistribution)
	{
		_requestorName = requestorName;
		_itemDistribution = itemDistribution;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(57);
		writeS((CharSequence) _requestorName);
		writeD(_itemDistribution);
	}
}
