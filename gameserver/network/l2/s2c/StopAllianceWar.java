package l2s.gameserver.network.l2.s2c;

public class StopAllianceWar extends L2GameServerPacket
{
	private String _allianceName;
	private String _char;

	public StopAllianceWar(final String alliance, final String charName)
	{
		_allianceName = alliance;
		_char = charName;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(191);
		writeS((CharSequence) _allianceName);
		writeS((CharSequence) _char);
	}
}
