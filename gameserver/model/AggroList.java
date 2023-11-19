package l2s.gameserver.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.collections.LazyArrayList;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.model.instances.NpcInstance;

public class AggroList
{
	private final NpcInstance npc;
	private final TIntObjectHashMap<AggroInfo> hateList;
	private final ReadWriteLock lock;
	private final Lock readLock;
	private final Lock writeLock;

	public AggroList(final NpcInstance npc)
	{
		hateList = new TIntObjectHashMap<AggroInfo>();
		lock = new ReentrantReadWriteLock();
		readLock = lock.readLock();
		writeLock = lock.writeLock();
		this.npc = npc;
	}

	public void addDamageHate(final Creature attacker, int damage, final int aggro)
	{
		damage = Math.max(damage, 0);
		if(damage == 0 && aggro == 0)
			return;
		writeLock.lock();
		try
		{
			AggroInfo ai;
			if((ai = hateList.get(attacker.getObjectId())) == null)
				hateList.put(attacker.getObjectId(), (ai = new AggroInfo(attacker)));
			else if(ai.time + Config.HATE_TIME < System.currentTimeMillis())
				ai.hate = 0;
			final AggroInfo aggroInfo = ai;
			aggroInfo.damage += damage;
			final AggroInfo aggroInfo2 = ai;
			aggroInfo2.hate += aggro;
			ai.damage = Math.max(ai.damage, 0);
			ai.hate = Math.max(ai.hate, 0);
			ai.time = System.currentTimeMillis();
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public AggroInfo get(final Creature attacker)
	{
		readLock.lock();
		try
		{
			return hateList.get(attacker.getObjectId());
		}
		finally
		{
			readLock.unlock();
		}
	}

	public void remove(final Creature attacker, final boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(!onlyHate)
			{
				hateList.remove(attacker.getObjectId());
				return;
			}
			final AggroInfo ai = hateList.get(attacker.getObjectId());
			if(ai != null)
				ai.hate = 0;
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public void clear()
	{
		clear(false);
	}

	public void clear(final boolean onlyHate)
	{
		writeLock.lock();
		try
		{
			if(hateList.isEmpty())
				return;
			if(!onlyHate)
			{
				hateList.clear();
				return;
			}
			final TIntObjectIterator<AggroInfo> itr = hateList.iterator();
			while(itr.hasNext())
			{
				itr.advance();
				final AggroInfo ai = itr.value();
				ai.hate = 0;
				if(ai.damage == 0)
					itr.remove();
			}
		}
		finally
		{
			writeLock.unlock();
		}
	}

	public boolean isEmpty()
	{
		readLock.lock();
		try
		{
			return hateList.isEmpty();
		}
		finally
		{
			readLock.unlock();
		}
	}

	public List<Creature> getHateList(final int radius)
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return Collections.emptyList();
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return Collections.emptyList();
		final List<Creature> hateList = new LazyArrayList<Creature>();
		final List<Creature> chars = World.getAroundCharacters(npc, radius, radius);
		for(int i = 0; i < hated.length; ++i)
		{
			final AggroInfo ai = hated[i];
			if(ai.hate != 0)
				if(ai.time + Config.HATE_TIME >= System.currentTimeMillis())
					for(final Creature cha : chars)
						if(cha.getObjectId() == ai.attackerId)
						{
							hateList.add(cha);
							break;
						}
		}
		return hateList;
	}

	public Creature getMostHated()
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return null;
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return null;
		final List<Creature> chars = World.getAroundCharacters(npc);
		for(int i = 0; i < hated.length; ++i)
		{
			final AggroInfo ai = hated[i];
			if(ai.hate != 0)
				for(final Creature cha : chars)
					if(cha.getObjectId() == ai.attackerId)
					{
						if(cha.isDead())
							break;
						return cha;
					}
		}
		return null;
	}

	public Creature getRandomHated()
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return null;
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Arrays.sort(hated, HateComparator.getInstance());
		if(hated[0].hate == 0)
			return null;
		final List<Creature> chars = World.getAroundCharacters(npc);
		final LazyArrayList<Creature> randomHated = LazyArrayList.newInstance();
		for(int i = 0; i < hated.length; ++i)
		{
			final AggroInfo ai = hated[i];
			if(ai.hate != 0)
				for(final Creature cha : chars)
					if(cha.getObjectId() == ai.attackerId)
					{
						if(cha.isDead())
							break;
						randomHated.add(cha);
						break;
					}
		}
		Creature mostHated;
		if(randomHated.isEmpty())
			mostHated = null;
		else
			mostHated = randomHated.get(Rnd.get(randomHated.size()));
		LazyArrayList.recycle(randomHated);
		return mostHated;
	}

	public Creature getTopDamager()
	{
		readLock.lock();
		AggroInfo[] hated;
		try
		{
			if(hateList.isEmpty())
				return null;
			hated = hateList.values(new AggroInfo[hateList.size()]);
		}
		finally
		{
			readLock.unlock();
		}
		Creature topDamager = null;
		Arrays.sort(hated, DamageComparator.getInstance());
		if(hated[0].damage == 0)
			return null;
		final List<Creature> chars = World.getAroundCharacters(npc);
		for(int i = 0; i < hated.length; ++i)
		{
			final AggroInfo ai = hated[i];
			if(ai.damage != 0)
				for(final Creature cha : chars)
					if(cha.getObjectId() == ai.attackerId)
					{
						topDamager = cha;
						return topDamager;
					}
		}
		return null;
	}

	public Map<Creature, HateInfo> getCharMap()
	{
		if(isEmpty())
			return Collections.emptyMap();
		final Map<Creature, HateInfo> aggroMap = new HashMap<Creature, HateInfo>();
		final List<Creature> chars = World.getAroundCharacters(npc);
		readLock.lock();
		try
		{
			final TIntObjectIterator<AggroInfo> itr = hateList.iterator();
			while(itr.hasNext())
			{
				itr.advance();
				final AggroInfo ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(final Creature attacker : chars)
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return aggroMap;
	}

	public Map<Playable, HateInfo> getPlayableMap()
	{
		if(isEmpty())
			return Collections.emptyMap();
		final Map<Playable, HateInfo> aggroMap = new HashMap<Playable, HateInfo>();
		final List<Playable> chars = World.getAroundPlayables(npc);
		readLock.lock();
		try
		{
			final TIntObjectIterator<AggroInfo> itr = hateList.iterator();
			while(itr.hasNext())
			{
				itr.advance();
				final AggroInfo ai = itr.value();
				if(ai.damage == 0 && ai.hate == 0)
					continue;
				for(final Playable attacker : chars)
					if(attacker.getObjectId() == ai.attackerId)
					{
						aggroMap.put(attacker, new HateInfo(attacker, ai));
						break;
					}
			}
		}
		finally
		{
			readLock.unlock();
		}
		return aggroMap;
	}

	private abstract class DamageHate
	{
		public int hate;
		public int damage;
		public long time;
	}

	public class HateInfo extends DamageHate
	{
		public final Creature attacker;

		HateInfo(final Creature attacker, final AggroInfo ai)
		{
			this.attacker = attacker;
			hate = ai.hate;
			damage = ai.damage;
		}
	}

	public class AggroInfo extends DamageHate
	{
		public final int attackerId;

		AggroInfo(final Creature attacker)
		{
			attackerId = attacker.getObjectId();
		}
	}

	public static class DamageComparator implements Comparator<DamageHate>
	{
		private static Comparator<DamageHate> instance;

		public static Comparator<DamageHate> getInstance()
		{
			return DamageComparator.instance;
		}

		@Override
		public int compare(final DamageHate o1, final DamageHate o2)
		{
			return o2.damage - o1.damage;
		}

		static
		{
			DamageComparator.instance = new DamageComparator();
		}
	}

	public static class HateComparator implements Comparator<DamageHate>
	{
		private static Comparator<DamageHate> instance;

		public static Comparator<DamageHate> getInstance()
		{
			return HateComparator.instance;
		}

		@Override
		public int compare(final DamageHate o1, final DamageHate o2)
		{
			final int diff = o2.hate - o1.hate;
			return diff == 0 ? o2.damage - o1.damage : diff;
		}

		static
		{
			HateComparator.instance = new HateComparator();
		}
	}
}
