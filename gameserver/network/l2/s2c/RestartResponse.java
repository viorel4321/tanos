package l2s.gameserver.network.l2.s2c;

public class RestartResponse extends L2GameServerPacket
{
	private static final RestartResponse STATIC_PACKET_TRUE;
	private static final RestartResponse STATIC_PACKET_FALSE;
	private final boolean _result;

	public static final RestartResponse valueOf(final boolean result)
	{
		return result ? RestartResponse.STATIC_PACKET_TRUE : RestartResponse.STATIC_PACKET_FALSE;
	}

	public RestartResponse(final boolean result)
	{
		_result = result;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(95);
		writeD(_result ? 1 : 0);
	}

	static
	{
		STATIC_PACKET_TRUE = new RestartResponse(true);
		STATIC_PACKET_FALSE = new RestartResponse(false);
	}
}
