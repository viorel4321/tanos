package l2s.gameserver.network.l2.s2c;

public class TutorialEnableClientEvent extends L2GameServerPacket
{
	private int _event;

	public TutorialEnableClientEvent(final int event)
	{
		_event = 0;
		_event = event;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(162);
		writeD(_event);
	}
}
