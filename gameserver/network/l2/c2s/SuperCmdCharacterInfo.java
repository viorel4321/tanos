package l2s.gameserver.network.l2.c2s;

class SuperCmdCharacterInfo extends L2GameClientPacket
{
	private String _characterName;

	@Override
	public void readImpl()
	{
		_characterName = readS();
	}

	@Override
	public void runImpl()
	{}
}
