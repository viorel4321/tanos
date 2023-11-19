package l2s.gameserver.network.l2.s2c;

public class TutorialShowHtml extends L2GameServerPacket
{
	private String _html;

	public TutorialShowHtml(final String html)
	{
		_html = html;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(160);
		writeS(_html);
	}
}
