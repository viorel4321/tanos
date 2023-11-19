package l2s.gameserver.instancemanager;

import java.util.Collection;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.HashIntObjectMap;

import l2s.gameserver.model.quest.Quest;

public class QuestManager
{
	private static IntObjectMap<Quest> _quests = new HashIntObjectMap<Quest>();

	public static Quest getQuest(final int id)
	{
		return _quests.get(id);
	}

	public static void addQuest(final Quest newQuest)
	{
		_quests.put(newQuest.getId(), newQuest);
	}

	public static Collection<Quest> getQuests()
	{
		return _quests.valueCollection();
	}
}
