package l2s.gameserver.network.l2.s2c;

import gnu.trove.iterator.TIntIntIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.quest.Quest;
import l2s.gameserver.model.quest.QuestState;

public class GMViewQuestInfo extends L2GameServerPacket
{
	private final String _characterName;
	private final TIntIntMap _quests = new TIntIntHashMap();

	public GMViewQuestInfo(final Player targetCharacter)
	{
		_characterName = targetCharacter.getName();
		for(Quest quest : targetCharacter.getAllActiveQuests())
		{
			if(quest.isVisible(targetCharacter))
			{
				QuestState qs = targetCharacter.getQuestState(quest.getId());
				_quests.put(quest.getId(), qs == null ? 0 : qs.getInt("cond"));
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(147);
		writeS(_characterName);
		writeH(_quests.size());
		for(TIntIntIterator iterator = _quests.iterator(); iterator.hasNext();)
		{
			iterator.advance();

			writeD(iterator.key());
			writeD(iterator.value());
		}
	}
}
