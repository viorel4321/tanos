package l2s.gameserver.network.l2.s2c;

public class OpenURL extends L2GameServerPacket
{
	private final String _url;

	public OpenURL(final String url)
	{
		_url = url;
	}

	@Override
	protected void writeImpl()
	{
		writeC(255);
		writeC(3);
		writeS(_url);
	}
}
