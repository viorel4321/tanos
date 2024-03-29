package l2s.gameserver.model.entity;

import java.lang.reflect.Constructor;

import l2s.commons.util.Rnd;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.tables.NpcTable;
import l2s.gameserver.templates.npc.NpcTemplate;

public class MonsterRace
{
	private NpcInstance[] monsters;
	private static MonsterRace _instance;
	private Constructor<?> _constructor;
	private int[][] speeds;
	private int[] first;
	private int[] second;

	private MonsterRace()
	{
		monsters = new NpcInstance[8];
		speeds = new int[8][20];
		first = new int[2];
		second = new int[2];
	}

	public static MonsterRace getInstance()
	{
		if(MonsterRace._instance == null)
			MonsterRace._instance = new MonsterRace();
		return MonsterRace._instance;
	}

	public void newRace()
	{
		int random = 0;
		for(int i = 0; i < 8; ++i)
		{
			final int id = 31003;
			random = Rnd.get(24);
			for(int j = i - 1; j >= 0; --j)
				if(monsters[j].getTemplate().npcId == id + random)
					random = Rnd.get(24);
			try
			{
				final NpcTemplate template = NpcTable.getTemplate((short) (id + random));
				_constructor = template.getInstanceConstructor();
				final int objectId = IdFactory.getInstance().getNextId();
				monsters[i] = (NpcInstance) _constructor.newInstance(objectId, template);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		newSpeeds();
	}

	public void newSpeeds()
	{
		speeds = new int[8][20];
		int total = 0;
		first[1] = 0;
		second[1] = 0;
		for(int i = 0; i < 8; ++i)
		{
			total = 0;
			for(int j = 0; j < 20; ++j)
			{
				if(j == 19)
					speeds[i][j] = 100;
				else
					speeds[i][j] = Rnd.get(65, 124);
				total += speeds[i][j];
			}
			if(total >= first[1])
			{
				second[0] = first[0];
				second[1] = first[1];
				first[0] = 8 - i;
				first[1] = total;
			}
			else if(total >= second[1])
			{
				second[0] = 8 - i;
				second[1] = total;
			}
		}
	}

	public NpcInstance[] getMonsters()
	{
		return monsters;
	}

	public int[][] getSpeeds()
	{
		return speeds;
	}

	public int getFirstPlace()
	{
		return first[0];
	}

	public int getSecondPlace()
	{
		return second[0];
	}
}
