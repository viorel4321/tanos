package l2s.gameserver.network.l2.s2c;

public class ExAskJoinPartyRoom extends L2GameServerPacket
{
	private String _charName;

	public ExAskJoinPartyRoom(final String charName)
	{
		_charName = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(52);
		writeS((CharSequence) _charName);
	}
}
