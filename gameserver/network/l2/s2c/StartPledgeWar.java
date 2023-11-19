package l2s.gameserver.network.l2.s2c;

public class StartPledgeWar extends L2GameServerPacket
{
	private String _pledgeName;
	private String _char;

	public StartPledgeWar(final String pledge, final String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(101);
		writeS((CharSequence) _char);
		writeS((CharSequence) _pledgeName);
	}
}
