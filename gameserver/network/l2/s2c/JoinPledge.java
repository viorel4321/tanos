package l2s.gameserver.network.l2.s2c;

public class JoinPledge extends L2GameServerPacket
{
	private int _pledgeId;

	public JoinPledge(final int pledgeId)
	{
		_pledgeId = pledgeId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(51);
		writeD(_pledgeId);
	}
}
