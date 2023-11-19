package l2s.gameserver.network.l2.s2c;

public class StopPledgeWar extends L2GameServerPacket
{
	private String _pledgeName;
	private String _char;

	public StopPledgeWar(final String pledge, final String charName)
	{
		_pledgeName = pledge;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(103);
		writeS((CharSequence) _pledgeName);
		writeS((CharSequence) _char);
	}
}
