package l2s.gameserver.network.l2.s2c;

public class ExDuelReady extends L2GameServerPacket
{
	private int _unk1;

	public ExDuelReady(final int unk1)
	{
		_unk1 = unk1;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(76);
		writeD(_unk1);
	}
}
