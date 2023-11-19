package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.network.l2.s2c.QuestList;

public class RequestQuestList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		this.sendPacket(new QuestList(getClient().getActiveChar()));
	}
}
