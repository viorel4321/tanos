package l2s.gameserver.network.l2.c2s;

class SuperCmdSummonCmd extends L2GameClientPacket
{
	private String _summonName;

	@Override
	public void readImpl()
	{
		_summonName = readS();
	}

	@Override
	public void runImpl()
	{}
}
