package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.QuestManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.Quest;

public class RequestTutorialLinkHtml extends L2GameClientPacket
{
	String _bypass;

	@Override
	public void readImpl()
	{
		_bypass = readS();
	}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Quest q = QuestManager.getQuest(255);
		if(q != null)
			player.processQuestEvent(q.getId(), _bypass, null);
	}
}
