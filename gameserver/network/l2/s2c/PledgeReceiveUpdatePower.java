package l2s.gameserver.network.l2.s2c;

public class PledgeReceiveUpdatePower extends L2GameServerPacket
{
	private int _privs;

	public PledgeReceiveUpdatePower(final int privs)
	{
		_privs = privs;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(66);
		writeD(_privs);
	}
}
