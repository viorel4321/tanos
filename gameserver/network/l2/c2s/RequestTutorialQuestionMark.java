package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.QuestManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.Quest;

public class RequestTutorialQuestionMark extends L2GameClientPacket
{
	int _number;

	public RequestTutorialQuestionMark()
	{
		_number = 0;
	}

	@Override
	public void readImpl()
	{
		_number = readD();
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getId(), "QM" + _number, null);
	}
}
