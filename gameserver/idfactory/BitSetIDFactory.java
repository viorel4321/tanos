package l2s.gameserver.idfactory;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.math.PrimeFinder;
import l2s.gameserver.ThreadPoolManager;

public class BitSetIDFactory extends IdFactory
{
	private static final Logger _log;
	private BitSet freeIds;
	private AtomicInteger freeIdCount;
	private AtomicInteger nextFreeId;

	protected BitSetIDFactory()
	{
		initialize();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new BitSetCapacityCheck(), 30000L, 30000L);
	}

	private void initialize()
	{
		try
		{
			(freeIds = new BitSet(PrimeFinder.nextPrime(100000))).clear();
			freeIdCount = new AtomicInteger(1879048191);
			for(final int usedObjectId : extractUsedObjectIDTable())
			{
				final int objectID = usedObjectId - 268435456;
				if(objectID < 0)
					BitSetIDFactory._log.warn("Object ID " + usedObjectId + " in DB is less than minimum ID of " + 268435456);
				else
				{
					freeIds.set(usedObjectId - 268435456);
					freeIdCount.decrementAndGet();
				}
			}
			nextFreeId = new AtomicInteger(freeIds.nextClearBit(0));
			initialized = true;
			BitSetIDFactory._log.info("IdFactory: " + freeIds.size() + " id's available.");
		}
		catch(Exception e)
		{
			initialized = false;
			BitSetIDFactory._log.warn("BitSet ID Factory could not be initialized correctly! " + e);
		}
	}

	@Override
	public synchronized void releaseId(final int objectID)
	{
		if(objectID - 268435456 > -1)
		{
			freeIds.clear(objectID - 268435456);
			freeIdCount.incrementAndGet();
			super.releaseId(objectID);
		}
		else
			BitSetIDFactory._log.warn("BitSet ID Factory: release objectID " + objectID + " failed (< " + 268435456 + ")");
	}

	@Override
	public synchronized int getNextId()
	{
		final int newID = nextFreeId.get();
		freeIds.set(newID);
		freeIdCount.decrementAndGet();
		int nextFree = freeIds.nextClearBit(newID);
		if(nextFree < 0)
			nextFree = freeIds.nextClearBit(0);
		if(nextFree < 0)
		{
			if(freeIds.size() >= 1879048191)
				throw new NullPointerException("Ran out of valid Id's.");
			increaseBitSetCapacity();
		}
		nextFreeId.set(nextFree);
		return newID + 268435456;
	}

	@Override
	public synchronized int size()
	{
		return freeIdCount.get();
	}

	protected synchronized int usedIdCount()
	{
		return size() - 268435456;
	}

	protected synchronized boolean reachingBitSetCapacity()
	{
		return PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > freeIds.size();
	}

	protected synchronized void increaseBitSetCapacity()
	{
		final BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
		newBitSet.or(freeIds);
		freeIds = newBitSet;
	}

	static
	{
		_log = LoggerFactory.getLogger(BitSetIDFactory.class);
	}

	public class BitSetCapacityCheck implements Runnable
	{
		@Override
		public void run()
		{
			if(reachingBitSetCapacity())
				increaseBitSetCapacity();
		}
	}
}
