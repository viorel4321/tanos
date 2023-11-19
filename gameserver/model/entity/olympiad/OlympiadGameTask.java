package l2s.gameserver.model.entity.olympiad;

import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.Log;

public class OlympiadGameTask implements Runnable
{
	private static final Logger _log;
	private OlympiadGame _game;
	private BattleStatus _status;
	private int _count;
	private long _time;
	private static final int BATTLE_PERIOD;
	private boolean _terminated;

	public boolean isTerminated()
	{
		return _terminated;
	}

	public BattleStatus getStatus()
	{
		return _status;
	}

	public int getCount()
	{
		return _count;
	}

	public OlympiadGame getGame()
	{
		return _game;
	}

	public long getTime()
	{
		return _count;
	}

	public ScheduledFuture<?> shedule()
	{
		return ThreadPoolManager.getInstance().schedule(this, _time);
	}

	public OlympiadGameTask(final OlympiadGame game, final BattleStatus status, final int count, final long time)
	{
		_terminated = false;
		_game = game;
		_status = status;
		_count = count;
		_time = time;
	}

	@Override
	public void run()
	{
		if(_game == null || _terminated)
			return;
		OlympiadGameTask task = null;
		final int gameId = _game.getId();
		try
		{
			if(_game._aborted)
			{
				_game.endGame(1000L, 3);
				return;
			}
			switch(_status)
			{
				case Begining:
				{
					_game.sendMessageToPlayers(false, 45);
					task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 30, 15000L);
					break;
				}
				case Begin_Countdown:
				{
					_game.sendMessageToPlayers(false, _count);
					if(_count == 30)
					{
						if(!_game.checkContinue(false))
							return;
						task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 15, 15000L);
						break;
					}
					else if(_count == 15)
					{
						if(!_game.checkContinue(false))
							return;
						task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, 5, 10000L);
						break;
					}
					else
					{
						if(_count < 6 && _count > 1)
						{
							task = new OlympiadGameTask(_game, BattleStatus.Begin_Countdown, _count - 1, 1000L);
							break;
						}
						if(_count == 1)
						{
							task = new OlympiadGameTask(_game, BattleStatus.PortPlayers, 0, 1000L);
							break;
						}
						break;
					}
				}
				case PortPlayers:
				{
					if(!_game.portPlayersToArena())
						return;
					task = new OlympiadGameTask(_game, BattleStatus.Started, 60, 3000L);
					break;
				}
				case Started:
				{
					if(_count == 60)
					{
						_game.setStarted(1);
						_game.preparation();
					}
					_game.sendMessageToPlayers(true, _count);
					_count -= 10;
					if(_count > 0)
					{
						task = new OlympiadGameTask(_game, BattleStatus.Started, _count, 10000L);
						break;
					}
					task = new OlympiadGameTask(_game, BattleStatus.CountDown, 5, 5000L);
					break;
				}
				case CountDown:
				{
					_game.sendMessageToPlayers(true, _count);
					--_count;
					if(_count <= 0)
					{
						task = new OlympiadGameTask(_game, BattleStatus.StartComp, OlympiadGameTask.BATTLE_PERIOD, 1000L);
						break;
					}
					task = new OlympiadGameTask(_game, BattleStatus.CountDown, _count, 1000L);
					break;
				}
				case StartComp:
				{
					if(_count == OlympiadGameTask.BATTLE_PERIOD)
					{
						if(!_game.checkContinue(true))
							return;
						_game.healPlusBuffs();
						_game.setStarted(2);
						_game.makeCompetitionStart();
					}
					--_count;
					if(_count == 0)
					{
						task = new OlympiadGameTask(_game, BattleStatus.ValidateWinner, 0, 10000L);
						break;
					}
					if((_count == 34 || _count == 30 || _count == 24 || _count == 18 || _count == 12 || _count == 6) && _count != OlympiadGameTask.BATTLE_PERIOD && !_game.checkContinue(true))
						return;
					if(_count == 5)
						_game.broadcastMessage(new SystemMessage(1915).addNumber(Integer.valueOf(60)), true, true);
					task = new OlympiadGameTask(_game, BattleStatus.StartComp, _count, 10000L);
					break;
				}
				case ValidateWinner:
				{
					try
					{
						_game.validateWinner();
					}
					catch(Exception e)
					{
						OlympiadGameTask._log.error("", e);
					}
					task = new OlympiadGameTask(_game, BattleStatus.Ending, 0, Config.OLY_RETURN_TIME * 1000);
					break;
				}
				case Ending:
				{
					_game.portPlayersBack();
					_game.clearSpectators();
					_game.clearPlayers();
					_terminated = true;
					if(Olympiad._manager != null)
						Olympiad._manager.freeOlympiadInstance(gameId);
					return;
				}
			}
			if(task == null)
			{
				Log.addLog("task == null for game " + _game.toString(), "olympiad");
				_game.endGame(1000L, 3);
				return;
			}
			_game.sheduleTask(task);
		}
		catch(Exception e)
		{
			OlympiadGameTask._log.error("", e);
			_game.endGame(1000L, 3);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(OlympiadGameTask.class);
		BATTLE_PERIOD = Config.ALT_OLY_BATTLE * 6;
	}
}
