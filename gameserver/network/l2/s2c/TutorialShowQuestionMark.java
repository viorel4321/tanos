package l2s.gameserver.network.l2.s2c;

public class TutorialShowQuestionMark extends L2GameServerPacket
{
	private int _number;

	public TutorialShowQuestionMark(final int number)
	{
		_number = number;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(161);
		writeD(_number);
	}
}
