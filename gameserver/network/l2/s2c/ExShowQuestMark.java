package l2s.gameserver.network.l2.s2c;

public class ExShowQuestMark extends L2GameServerPacket
{
	private int _questId;

	public ExShowQuestMark(final int questId)
	{
		_questId = questId;
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(26);
		writeD(_questId);
	}
}
