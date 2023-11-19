package l2s.gameserver.network.l2.s2c;

public class SurrenderPledgeWar extends L2GameServerPacket
{
	private String _pledgeName;
	private String _char;

	public SurrenderPledgeWar(final String pledge, final String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(105);
		writeS((CharSequence) _pledgeName);
		writeS((CharSequence) _char);
	}
}
