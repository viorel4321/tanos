package l2s.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.util.Rnd;
import l2s.gameserver.Config;

public class OlympiadManager implements Runnable
{
	private static final Logger _log;
	private Map<Integer, OlympiadGame> _olympiadInstances;
	private boolean DISABLED;

	public OlympiadManager()
	{
		_olympiadInstances = new ConcurrentHashMap<Integer, OlympiadGame>();
		DISABLED = false;
	}

	public void sleep(final long time)
	{
		try
		{
			Thread.sleep(time);
		}
		catch(InterruptedException ex)
		{}
	}

	@Override
	public void run()
	{
		if(Olympiad.isOlympiadEnd())
			return;
		while(Olympiad.inCompPeriod())
		{
			if(DISABLED)
				return;
			if(Olympiad._nobles.isEmpty())
				sleep(60000L);
			else
			{
				while(Olympiad.inCompPeriod())
				{
					if(DISABLED)
						return;
					if(Olympiad._nonClassBasedRegisters.size() >= Config.NONCLASS_GAME_MIN)
						prepareBattles(CompType.NON_CLASSED, Olympiad._nonClassBasedRegisters);
					for(TIntObjectIterator<TIntList> iterator = Olympiad._classBasedRegisters.iterator(); iterator.hasNext();) {
						iterator.advance();
						if (iterator.value().size() >= Config.CLASS_GAME_MIN)
							prepareBattles(CompType.CLASSED, iterator.value());
					}
					sleep(30000L);
				}
				sleep(30000L);
			}
		}
		if(DISABLED)
			return;
		Olympiad._classBasedRegisters.clear();
		Olympiad._nonClassBasedRegisters.clear();
		boolean allGamesTerminated = false;
		while(!allGamesTerminated)
		{
			sleep(30000L);
			if(DISABLED)
				return;
			if(_olympiadInstances.isEmpty())
				break;
			allGamesTerminated = true;
			for(final OlympiadGame game : _olympiadInstances.values())
				if(game.getTask() != null && !game.getTask().isTerminated())
					allGamesTerminated = false;
		}
		_olympiadInstances.clear();
	}

	private void prepareBattles(CompType type, TIntList list)
	{
		for(int i = 0; i < Olympiad.STADIUMS.length; ++i)
			try
			{
				if(Olympiad.STADIUMS[i].isFreeToUse())
				{
					if(list.size() < 2 || DISABLED)
						break;
					final OlympiadGame game = new OlympiadGame(i, type, nextOpponents(list));
					game.sheduleTask(new OlympiadGameTask(game, BattleStatus.Begining, 0, 1L));
					_olympiadInstances.put(i, game);
					Olympiad.STADIUMS[i].setStadiaBusy();
				}
			}
			catch(Exception e)
			{
				OlympiadManager._log.error("", e);
			}
	}

	public void freeOlympiadInstance(final int index)
	{
		_olympiadInstances.remove(index);
		Olympiad.STADIUMS[index].setStadiaFree();
	}

	public OlympiadGame getOlympiadInstance(final int index)
	{
		return _olympiadInstances.get(index);
	}

	public Map<Integer, OlympiadGame> getOlympiadGames()
	{
		return _olympiadInstances;
	}

	private TIntList nextOpponents(TIntList list)
	{
		TIntList opponents = new TIntArrayList();
		for(int i = 0; i < 2; ++i)
		{
			int nobleObjectId = Rnd.get(list.toArray());
			list.remove(nobleObjectId);
			opponents.add(nobleObjectId);
			removeOpponent(nobleObjectId);
		}
		return opponents;
	}

	private void removeOpponent(final Integer noble)
	{
		Olympiad._classBasedRegisters.removeValue(noble);
		Olympiad._nonClassBasedRegisters.remove(noble);
	}

	public void disable()
	{
		DISABLED = true;
		Olympiad._classBasedRegisters.clear();
		Olympiad._nonClassBasedRegisters.clear();
		if(!_olympiadInstances.isEmpty())
		{
			for(final OlympiadGame game : _olympiadInstances.values())
				if(game.getTask() != null && !game.getTask().isTerminated())
					game.endGame(10L, 3);
			_olympiadInstances.clear();
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(OlympiadManager.class);
	}
}
