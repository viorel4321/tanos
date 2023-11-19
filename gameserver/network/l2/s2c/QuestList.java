package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.QuestState;

public class QuestList extends L2GameServerPacket
{
	private List<int[]> questlist;

	public QuestList(final Player player)
	{
		questlist = new ArrayList<int[]>();
		if(player == null)
			return;
		for(final QuestState quest : player.getAllQuestsStates())
			if(quest != null && quest.getQuest().isVisible(player) && quest.isStarted())
				questlist.add(new int[] { quest.getQuest().getId(), quest.getRawInt("cond") });
	}

	@Override
	protected final void writeImpl()
	{
		if(questlist == null || questlist.size() == 0)
		{
			writeC(128);
			writeH(0);
			return;
		}
		writeC(128);
		writeH(questlist.size());
		for(final int[] q : questlist)
		{
			writeD(q[0]);
			writeD(q[1]);
		}
	}
}
