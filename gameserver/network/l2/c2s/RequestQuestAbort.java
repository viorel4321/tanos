package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestQuestAbort extends L2GameClientPacket
{
	private int _QuestID;

	@Override
	public void readImpl()
	{
		_QuestID = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final QuestState qs = activeChar.getQuestState(_QuestID);
		if(qs != null)
		{
			qs.abortQuest();
			activeChar.sendPacket(new SystemMessage(335).addString(qs.getQuest().getDescr()));
		}
	}
}
