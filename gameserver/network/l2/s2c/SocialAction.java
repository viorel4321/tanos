package l2s.gameserver.network.l2.s2c;

public class SocialAction extends L2GameServerPacket
{
	private int _playerId;
	private int _actionId;

	public SocialAction(final int playerId, final int actionId)
	{
		_playerId = playerId;
		_actionId = actionId;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(45);
		writeD(_playerId);
		writeD(_actionId);
	}
}
