package l2s.gameserver.network.l2.s2c;

public class EventTrigger extends L2GameServerPacket
{
	private int _trapId;
	private boolean _active;

	public EventTrigger(final int trapId, final boolean active)
	{
		_trapId = trapId;
		_active = active;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(207);
		writeD(_trapId);
		writeC(_active ? 1 : 0);
	}
}
