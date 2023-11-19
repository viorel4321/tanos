package l2s.gameserver.model.entity.SevenSignsFestival;

import java.util.HashMap;

import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.model.entity.SevenSigns;

public class FestivalManager implements Runnable
{
	private static final SevenSigns _signsInstance = SevenSigns.getInstance();

	private SevenSignsFestival.FestivalStatus _status;
	private long _elapsed;

	public FestivalManager(final SevenSignsFestival.FestivalStatus status)
	{
		_status = status;
	}

	public FestivalManager(final SevenSignsFestival.FestivalStatus status, final long elapsed)
	{
		_status = status;
		_elapsed = elapsed;
	}

	@Override
	public synchronized void run()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		if(FestivalManager._signsInstance.isSealValidationPeriod())
			return;

		switch(_status)
		{
			case Begining:
			{
				if(FestivalManager._signsInstance.getMilliToPeriodChange() < 2280000L)
					return;
				SevenSignsFestival.setFestivalInstances(new HashMap<Integer, L2DarknessFestival>());
				SevenSignsFestival.setNextCycleStart();
				SevenSignsFestival.setNextFestivalStart(1200000L);
				SevenSignsFestival.sendMessageToAll("Festival Guide", "The main event will start in " + (SevenSignsFestival.getMinsToNextFestival() - 1) + " minutes. Please register now.");
				ThreadPoolManager.getInstance().schedule(new FestivalManager(SevenSignsFestival.FestivalStatus.Started), 1200000L);
				break;
			}
			case Started:
			{
				for(int i = 0; i < 5; ++i)
				{
					if(SevenSignsFestival.getDuskFestivalParticipants().get(i) != null)
						SevenSignsFestival.getFestivalInstances().put(10 + i, new L2DarknessFestival(1, i));
					if(SevenSignsFestival.getDawnFestivalParticipants().get(i) != null)
						SevenSignsFestival.getFestivalInstances().put(20 + i, new L2DarknessFestival(2, i));
				}
				SevenSignsFestival.setFestivalInitialized(true);
				SevenSignsFestival.setNextFestivalStart(2280000L);
				SevenSignsFestival.sendMessageToAll("Festival Guide", "The main event is now starting.");
				SevenSignsFestival.getDawnPreviousParticipants().clear();
				SevenSignsFestival.getDuskPreviousParticipants().clear();
				ThreadPoolManager.getInstance().schedule(new FestivalManager(SevenSignsFestival.FestivalStatus.FirstSpawn), 120000L);
				break;
			}
			case FirstSpawn:
			{
				_elapsed = 120000L;
				SevenSignsFestival.setFestivalInProgress(true);
				for(final L2DarknessFestival festivalInst : SevenSignsFestival.getFestivalInstances().values())
				{
					festivalInst.festivalStart();
					festivalInst.sendMessageToParticipants("The festival is about to begin!");
				}
				ThreadPoolManager.getInstance().schedule(new FestivalManager(SevenSignsFestival.FestivalStatus.FirstSwarm, _elapsed), 180000L);
				break;
			}
			case FirstSwarm:
			{
				_elapsed += 180000L;
				for(final L2DarknessFestival festivalInst : SevenSignsFestival.getFestivalInstances().values())
					festivalInst.moveMonstersToCenter();
				ThreadPoolManager.getInstance().schedule(new FestivalManager(SevenSignsFestival.FestivalStatus.SecondSpawn, _elapsed), 240000L);
				break;
			}
			case SecondSpawn:
			{
				for(final L2DarknessFestival festivalInst : SevenSignsFestival.getFestivalInstances().values())
				{
					festivalInst.spawnFestivalMonsters(30, 2);
					festivalInst.sendMessageToParticipants("The festival will end in 9 minute(s).");
				}
				_elapsed += 240000L;
				ThreadPoolManager.getInstance().schedule(new FestivalManager(SevenSignsFestival.FestivalStatus.SecondSwarm, _elapsed), 180000L);
				break;
			}
			case SecondSwarm:
			{
				for(final L2DarknessFestival festivalInst : SevenSignsFestival.getFestivalInstances().values())
					festivalInst.moveMonstersToCenter();
				_elapsed += 180000L;
				ThreadPoolManager.getInstance().schedule(new FestivalManager(SevenSignsFestival.FestivalStatus.ChestSpawn, _elapsed), 180000L);
				break;
			}
			case ChestSpawn:
			{
				for(final L2DarknessFestival festivalInst : SevenSignsFestival.getFestivalInstances().values())
				{
					festivalInst.spawnFestivalMonsters(60, 3);
					festivalInst.sendMessageToParticipants("The chests have spawned! Be quick, the festival will end soon.");
				}
				_elapsed += 180000L;
				ThreadPoolManager.getInstance().schedule(new FestivalManager(SevenSignsFestival.FestivalStatus.Ending, _elapsed), 1080000L - _elapsed);
				break;
			}
			case Ending:
			{
				SevenSignsFestival.setFestivalInProgress(false);
				for(final L2DarknessFestival festivalInst : SevenSignsFestival.getFestivalInstances().values())
					festivalInst.festivalEnd();
				SevenSignsFestival.getDawnFestivalParticipants().clear();
				SevenSignsFestival.getDuskFestivalParticipants().clear();
				SevenSignsFestival.setFestivalInitialized(false);
				SevenSignsFestival.getFestivalInstances().clear();
				SevenSignsFestival.sendMessageToAll("Festival Witch", "That will do! I'll move you to the outside soon.");
				SevenSignsFestival.setManagerInstance(null);
				break;
			}
		}
	}
}
