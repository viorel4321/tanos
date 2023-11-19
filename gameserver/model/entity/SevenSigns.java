package l2s.gameserver.model.entity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.CatacombSpawnManager;
import l2s.gameserver.model.AutoChatHandler;
import l2s.gameserver.model.AutoSpawnHandler;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import l2s.gameserver.network.l2.s2c.SSQInfo;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.templates.StatsSet;

public class SevenSigns
{
	public class SevenSignsAnnounce implements Runnable
	{
		@Override
		public void run()
		{
			for(Player player : GameObjectsStorage.getPlayers())
			{
				sendCurrentPeriodMsg(player);
			}
			ThreadPoolManager.getInstance().schedule(new SevenSignsAnnounce(), Config.SS_ANNOUNCE_PERIOD * 1000 * 60);
		}
	}

	public class SevenSignsPeriodChange implements Runnable
	{
		@Override
		public void run()
		{
			_log.info("SevenSignsPeriodChange: old=" + _activePeriod);
			int periodEnded = _activePeriod;
			++_activePeriod;
			int compWinner = 0;
			_sky = 256;
			switch(periodEnded)
			{
				case 0:
				{
					SevenSignsFestival.getInstance().startFestivalManager();
					sendMessageToAll(1210);
					break;
				}
				case 1:
				{
					sendMessageToAll(1211);
					compWinner = getCabalHighestScore();
					SevenSignsFestival.getInstance().stopFestivalManager();
					calcNewSealOwners();
					if(compWinner == 1)
					{
						_sky = 257;
						sendMessageToAll(1240);
					}
					else
					{
						_sky = 258;
						sendMessageToAll(1241);
					}
					_previousWinner = compWinner;
					break;
				}
				case 2:
				{
					initializeSeals();
					sendMessageToAll(1218);
					_log.info("SevenSigns: The " + getCabalName(_previousWinner) + " have won the competition with " + getCurrentScore(_previousWinner) + " points!");
					break;
				}
				case 3:
				{
					SevenSignsFestival.getInstance().rewardHighestRanked();
					_activePeriod = 0;
					sendMessageToAll(1219);
					resetPlayerData();
					resetSeals();
					_dawnStoneScore = 0L;
					_duskStoneScore = 0L;
					_dawnFestivalScore = 0L;
					_duskFestivalScore = 0L;
					++_currentCycle;
					SevenSignsFestival.getInstance().resetFestivalData(false);
					break;
				}
			}
			saveSevenSignsData(null, true);
			_log.info("SevenSignsPeriodChange: new=" + _activePeriod);
			_log.info("SevenSigns: Teleporting losing players from dungeons...");
			try
			{
				teleLosingCabalFromDungeons(getCabalHighestScore());
				SSQInfo ss = new SSQInfo();
				for(Player player : GameObjectsStorage.getPlayers())
					player.sendPacket(ss);
				_log.info("SevenSigns: Change Catacomb spawn...");
				CatacombSpawnManager.getInstance().notifyChangeMode();
				_log.info("SevenSigns: Spawning NPCs...");
				spawnSevenSignsNPC();
				_log.info("SevenSigns: The " + getCurrentPeriodName() + " period has begun!");
				_log.info("SevenSigns: Calculating next period change time...");
				setCalendarForNextPeriodChange();
				_log.info("SevenSignsPeriodChange: SecondsToNextChange=" + getMilliToPeriodChange() / 1000L);
				_periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), getMilliToPeriodChange());
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	protected static Logger _log = LoggerFactory.getLogger(SevenSigns.class);
	private static SevenSigns _instance;

	private ScheduledFuture<?> _periodChange;

	public static final String SEVEN_SIGNS_HTML_PATH = "seven_signs/";
	public static final int CABAL_NULL = 0;
	public static final int CABAL_DUSK = 1;
	public static final int CABAL_DAWN = 2;
	public static final int SEAL_NULL = 0;
	public static final int SEAL_AVARICE = 1;
	public static final int SEAL_GNOSIS = 2;
	public static final int SEAL_STRIFE = 3;
	public static final int PERIOD_COMP_RECRUITING = 0;
	public static final int PERIOD_COMPETITION = 1;
	public static final int PERIOD_COMP_RESULTS = 2;
	public static final int PERIOD_SEAL_VALIDATION = 3;
	public static final int PERIOD_START_HOUR = 18;
	public static final int PERIOD_START_MINS = 0;
	public static final int PERIOD_START_DAY = 2;
	public static final int PERIOD_MINOR_LENGTH = 900000;
	public static final int PERIOD_MAJOR_LENGTH = 603900000;
	public static final short ANCIENT_ADENA_ID = 5575;
	public static final short RECORD_SEVEN_SIGNS_ID = 5707;
	public static final short CERTIFICATE_OF_APPROVAL_ID = 6388;
	public static final int RECORD_SEVEN_SIGNS_COST = 500;
	public static final int ADENA_JOIN_DAWN_COST = 50000;
	public static final int ORATOR_NPC_ID = 31094;
	public static final int PREACHER_NPC_ID = 31093;
	public static final int MAMMON_MERCHANT_ID = 31113;
	public static final int MAMMON_BLACKSMITH_ID = 31126;
	public static final int MAMMON_MARKETEER_ID = 31092;
	public static final int SPIRIT_IN_ID = 31111;
	public static final int SPIRIT_OUT_ID = 31112;
	public static final short LILITH_NPC_ID = 25283;
	public static final short ANAKIM_NPC_ID = 25286;
	public static final int CREST_OF_DAWN_ID = 31170;
	public static final int CREST_OF_DUSK_ID = 31171;
	public static final int SEAL_STONE_BLUE_ID = 6360;
	public static final int SEAL_STONE_GREEN_ID = 6361;
	public static final int SEAL_STONE_RED_ID = 6362;
	public static final int SEAL_STONE_BLUE_VALUE = 3;
	public static final int SEAL_STONE_GREEN_VALUE = 5;
	public static final int SEAL_STONE_RED_VALUE = 10;
	public static final int BLUE_CONTRIB_POINTS = 3;
	public static final int GREEN_CONTRIB_POINTS = 5;
	public static final int RED_CONTRIB_POINTS = 10;
	public static final long MAXIMUM_PLAYER_CONTRIB = Config.MAX_PLAYER_CONTR;

	protected int _activePeriod;
	protected int _currentCycle;
	protected long _dawnStoneScore;
	protected long _duskStoneScore;
	protected long _dawnFestivalScore;
	protected long _duskFestivalScore;
	protected int _compWinner;
	protected int _previousWinner;

	private final Calendar _calendar = Calendar.getInstance();
	private final Map<Integer, StatsSet> _signsPlayerData = new ConcurrentHashMap<Integer, StatsSet>();
	private final Map<Integer, Integer> _signsSealOwners = new ConcurrentHashMap<Integer, Integer>();
	private final Map<Integer, Integer> _signsDuskSealTotals = new ConcurrentHashMap<Integer, Integer>();
	private final Map<Integer, Integer> _signsDawnSealTotals = new ConcurrentHashMap<Integer, Integer>();

	private int _sky;

	public SevenSigns()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_sky = 256;

		try
		{
			restoreSevenSignsData();
		}
		catch(Exception e)
		{
			_log.error("SevenSigns: Failed to load configuration: " + e);
			e.printStackTrace();
		}

		_log.info("SevenSigns: Currently in the " + getCurrentPeriodName() + " period!");

		initializeSeals();

		if(isSealValidationPeriod())
		{
			if(getCabalHighestScore() == 0)
				_log.info("SevenSigns: The Competition last week ended with a tie.");
			else
				_log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " were victorious last week.");
		}
		else if(getCabalHighestScore() == 0)
			_log.info("SevenSigns: The Competition this week, if the trend continue, will end with a tie.");
		else
			_log.info("SevenSigns: The " + getCabalName(getCabalHighestScore()) + " are in the lead this week.");

		int numMins = 0;
		int numHours = 0;
		int numDays = 0;
		synchronized (this)
		{
			setCalendarForNextPeriodChange();
			long milliToChange = getMilliToPeriodChange();
			if(milliToChange < 10L)
				milliToChange = 10L;
			_periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), milliToChange);
			double numSecs = milliToChange / 1000L % 60L;
			double countDown = (milliToChange / 1000L - numSecs) / 60.0;
			numMins = (int) Math.floor(countDown % 60.0);
			countDown = (countDown - numMins) / 60.0;
			numHours = (int) Math.floor(countDown % 24.0);
			numDays = (int) Math.floor((countDown - numHours) / 24.0);
		}

		_log.info("SevenSigns: Next period begins in " + numDays + " days, " + numHours + " hours and " + numMins + " mins.");

		if(Config.SS_ANNOUNCE_PERIOD > 0)
			ThreadPoolManager.getInstance().schedule(new SevenSignsAnnounce(), Config.SS_ANNOUNCE_PERIOD * 1000 * 60);
	}

	public void spawnSevenSignsNPC()
	{
		int[] manageNpcIDs = { 31078,31079,31080,31081,31082,31083,31084,31085,31086,31087,31088,31089,31090,31091,31168,31169,31692,31693,31694,31695,31997,31998 };
		List<AutoSpawnHandler.AutoSpawnInstance> _managersSpawn = new ArrayList<AutoSpawnHandler.AutoSpawnInstance>();
		for(int npcId : manageNpcIDs)
			_managersSpawn.add(AutoSpawnHandler.getInstance().getAutoSpawnInstance(npcId, false));

		AutoSpawnHandler.AutoSpawnInstance _merchantSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31113, false);
		AutoSpawnHandler.AutoSpawnInstance _blacksmithSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31126, false);
		Map<Integer, AutoSpawnHandler.AutoSpawnInstance> _marketeerSpawns = AutoSpawnHandler.getInstance().getAllAutoSpawnInstance(31092);
		AutoSpawnHandler.AutoSpawnInstance _spiritInSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31111, false);
		AutoSpawnHandler.AutoSpawnInstance _spiritOutSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31112, false);
		AutoSpawnHandler.AutoSpawnInstance _lilithSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(25283, false);
		AutoSpawnHandler.AutoSpawnInstance _anakimSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(25286, false);
		AutoSpawnHandler.AutoSpawnInstance _crestOfDawnSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31170, false);
		AutoSpawnHandler.AutoSpawnInstance _crestOfDuskSpawn = AutoSpawnHandler.getInstance().getAutoSpawnInstance(31171, false);

		Map<Integer, AutoSpawnHandler.AutoSpawnInstance> _oratorSpawns = AutoSpawnHandler.getInstance().getAllAutoSpawnInstance(31094);
		Map<Integer, AutoSpawnHandler.AutoSpawnInstance> _preacherSpawns = AutoSpawnHandler.getInstance().getAllAutoSpawnInstance(31093);
		if(Config.ALLOW_SEVEN_SIGNS)
		{
			for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _managersSpawn)
			{
				if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
					AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
			}

			if(isSealValidationPeriod() || isCompResultsPeriod())
			{
				for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _marketeerSpawns.values())
					AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
				if(getSealOwner(2) == getCabalHighestScore() && getSealOwner(2) != 0)
				{
					if(!Config.ANNOUNCE_MAMMON_SPAWN)
						_blacksmithSpawn.setBroadcast(false);
					if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_blacksmithSpawn.getObjectId(), true).isSpawnActive())
						AutoSpawnHandler.getInstance().setSpawnActive(_blacksmithSpawn, true);
					for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _oratorSpawns.values())
						if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
							AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
					for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _preacherSpawns.values())
						if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive())
							AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, true);
					if(AutoChatHandler.getInstance().getAutoChatInstance(31093, false) != null && AutoChatHandler.getInstance().getAutoChatInstance(31094, false) != null && !AutoChatHandler.getInstance().getAutoChatInstance(31093, false).isActive() && !AutoChatHandler.getInstance().getAutoChatInstance(31094, false).isActive())
						AutoChatHandler.getInstance().setAutoChatActive(true);
				}
				else
				{
					AutoSpawnHandler.getInstance().setSpawnActive(_blacksmithSpawn, false);
					for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _oratorSpawns.values())
						AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
					for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _preacherSpawns.values())
						AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
					AutoChatHandler.getInstance().setAutoChatActive(false);
				}
				if(getSealOwner(1) == getCabalHighestScore() && getSealOwner(1) != 0)
				{
					if(!Config.ANNOUNCE_MAMMON_SPAWN)
						_merchantSpawn.setBroadcast(false);
					if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_merchantSpawn.getObjectId(), true).isSpawnActive())
						AutoSpawnHandler.getInstance().setSpawnActive(_merchantSpawn, true);
					if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_spiritInSpawn.getObjectId(), true).isSpawnActive())
						AutoSpawnHandler.getInstance().setSpawnActive(_spiritInSpawn, true);
					if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_spiritOutSpawn.getObjectId(), true).isSpawnActive())
						AutoSpawnHandler.getInstance().setSpawnActive(_spiritOutSpawn, true);
					switch(getCabalHighestScore())
					{
						case 2:
						{
							if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_lilithSpawn.getObjectId(), true).isSpawnActive())
								AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, true);
							AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, false);
							if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_crestOfDawnSpawn.getObjectId(), true).isSpawnActive())
								AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDawnSpawn, true);
							AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDuskSpawn, false);
							break;
						}
						case 1:
						{
							if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_anakimSpawn.getObjectId(), true).isSpawnActive())
								AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, true);
							AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, false);
							if(!AutoSpawnHandler.getInstance().getAutoSpawnInstance(_crestOfDuskSpawn.getObjectId(), true).isSpawnActive())
								AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDuskSpawn, true);
							AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDawnSpawn, false);
							break;
						}
					}
				}
				else
				{
					AutoSpawnHandler.getInstance().setSpawnActive(_merchantSpawn, false);
					AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, false);
					AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, false);
					AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDawnSpawn, false);
					AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDuskSpawn, false);
					AutoSpawnHandler.getInstance().setSpawnActive(_spiritInSpawn, false);
					AutoSpawnHandler.getInstance().setSpawnActive(_spiritOutSpawn, false);
				}
			}
			else
			{
				AutoSpawnHandler.getInstance().setSpawnActive(_merchantSpawn, false);
				AutoSpawnHandler.getInstance().setSpawnActive(_blacksmithSpawn, false);
				AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, false);
				AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, false);
				AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDawnSpawn, false);
				AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDuskSpawn, false);
				AutoSpawnHandler.getInstance().setSpawnActive(_spiritInSpawn, false);
				AutoSpawnHandler.getInstance().setSpawnActive(_spiritOutSpawn, false);
				for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _oratorSpawns.values())
					AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
				for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _preacherSpawns.values())
					AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
				for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _marketeerSpawns.values())
					AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
				AutoChatHandler.getInstance().setAutoChatActive(false);
			}
		}
		else
		{
			for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _managersSpawn)
				AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);

			AutoSpawnHandler.getInstance().setSpawnActive(_merchantSpawn, false);
			AutoSpawnHandler.getInstance().setSpawnActive(_blacksmithSpawn, false);
			AutoSpawnHandler.getInstance().setSpawnActive(_lilithSpawn, false);
			AutoSpawnHandler.getInstance().setSpawnActive(_anakimSpawn, false);
			AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDawnSpawn, false);
			AutoSpawnHandler.getInstance().setSpawnActive(_crestOfDuskSpawn, false);
			AutoSpawnHandler.getInstance().setSpawnActive(_spiritInSpawn, false);
			AutoSpawnHandler.getInstance().setSpawnActive(_spiritOutSpawn, false);
			for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _oratorSpawns.values())
				AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
			for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _preacherSpawns.values())
				AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
			for(AutoSpawnHandler.AutoSpawnInstance spawnInst : _marketeerSpawns.values())
				AutoSpawnHandler.getInstance().setSpawnActive(spawnInst, false);
			AutoChatHandler.getInstance().setAutoChatActive(false);
		}
	}

	public static SevenSigns getInstance()
	{
		if(_instance == null)
			_instance = new SevenSigns();
		return _instance;
	}

	public static long calcContributionScore(long blueCount, long greenCount, long redCount)
	{
		long contrib = blueCount * 3L;
		contrib += greenCount * 5L;
		contrib += redCount * 10L;
		return contrib;
	}

	public static long calcAncientAdenaReward(long blueCount, long greenCount, long redCount)
	{
		long reward = blueCount * 3L;
		reward += greenCount * 5L;
		reward += redCount * 10L;
		return reward;
	}

	public static int getCabalNumber(String cabal)
	{
		if(cabal.equalsIgnoreCase("dawn"))
			return 2;
		if(cabal.equalsIgnoreCase("dusk"))
			return 1;
		return 0;
	}

	public static String getCabalShortName(int cabal)
	{
		switch(cabal)
		{
			case 2:
				return "dawn";
			case 1:
				return "dusk";
			default:
				return "No Cabal";
		}
	}

	public static String getCabalName(int cabal)
	{
		switch(cabal)
		{
			case 2:
				return "Lords of Dawn";
			case 1:
				return "Revolutionaries of Dusk";
			default:
				return "No Cabal";
		}
	}

	public static String getSealName(int seal, boolean shortName)
	{
		String sealName = shortName ? "" : "Seal of ";
		switch(seal)
		{
			case 1:
			{
				sealName += "Avarice";
				break;
			}
			case 2:
			{
				sealName += "Gnosis";
				break;
			}
			case 3:
			{
				sealName += "Strife";
				break;
			}
		}
		return sealName;
	}

	public static String capitalizeWords(String str)
	{
		char[] charArray = str.toCharArray();
		StringBuffer buf = new StringBuffer();
		charArray[0] = Character.toUpperCase(charArray[0]);
		for(int i = 0; i < charArray.length; ++i)
		{
			if(Character.isWhitespace(charArray[i]) && i != charArray.length - 1)
				charArray[i + 1] = Character.toUpperCase(charArray[i + 1]);
			buf.append(Character.toString(charArray[i]));
		}
		return buf.toString();
	}

	public final int getCurrentCycle()
	{
		return _currentCycle;
	}

	public final int getCurrentPeriod()
	{
		return _activePeriod;
	}

	private int getDaysToPeriodChange()
	{
		int numDays = _calendar.get(7) - 2;
		if(numDays < 0)
			return 0 - numDays;
		return 7 - numDays;
	}

	public final long getMilliToPeriodChange()
	{
		return _calendar.getTimeInMillis() - System.currentTimeMillis();
	}

	protected void setCalendarForNextPeriodChange()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		switch(getCurrentPeriod())
		{
			case 1:
			case 3:
			{
				int daysToChange = getDaysToPeriodChange();
				if(daysToChange == 7)
				{
					if(_calendar.get(11) < 18)
						daysToChange = 0;
					else if(_calendar.get(11) == 18 && _calendar.get(12) < 0)
						daysToChange = 0;
				}
				if(daysToChange > 0)
					_calendar.add(5, daysToChange);
				_calendar.set(11, 18);
				_calendar.set(12, 0);
				break;
			}
			case 0:
			case 2:
			{
				_calendar.add(14, 900000);
				break;
			}
		}
	}

	public final String getCurrentPeriodName()
	{
		String periodName = null;
		switch(_activePeriod)
		{
			case 0:
			{
				periodName = "Quest Event Initialization";
				break;
			}
			case 1:
			{
				periodName = "Competition (Quest Event)";
				break;
			}
			case 2:
			{
				periodName = "Quest Event Results";
				break;
			}
			case 3:
			{
				periodName = "Seal Validation";
				break;
			}
		}
		return periodName;
	}

	public final boolean isSealValidationPeriod()
	{
		return _activePeriod == 3;
	}

	public final boolean isCompResultsPeriod()
	{
		return _activePeriod == 2;
	}

	public final long getCurrentScore(int cabal)
	{
		double totalStoneScore = _dawnStoneScore + _duskStoneScore;
		switch(cabal)
		{
			case 0:
				return 0L;
			case 2:
				return Math.round((float) (_dawnStoneScore / ((float) totalStoneScore == 0.0f ? 1.0 : totalStoneScore)) * 500.0f) + _dawnFestivalScore;
			case 1:
				return Math.round((float) (_duskStoneScore / ((float) totalStoneScore == 0.0f ? 1.0 : totalStoneScore)) * 500.0f) + _duskFestivalScore;
			default:
				return 0L;
		}
	}

	public final long getCurrentStoneScore(int cabal)
	{
		switch(cabal)
		{
			case 0:
				return 0L;
			case 2:
				return _dawnStoneScore;
			case 1:
				return _duskStoneScore;
			default:
				return 0L;
		}
	}

	public final long getCurrentFestivalScore(int cabal)
	{
		switch(cabal)
		{
			case 0:
				return 0L;
			case 2:
				return _dawnFestivalScore;
			case 1:
				return _duskFestivalScore;
			default:
				return 0L;
		}
	}

	public final int getCabalHighestScore()
	{
		if(getCurrentScore(1) == getCurrentScore(2))
			return 0;
		if(getCurrentScore(1) > getCurrentScore(2))
			return 1;
		return 2;
	}

	public final int getSealOwner(int seal)
	{
		if(_signsSealOwners == null || !_signsSealOwners.containsKey(seal))
			return 0;
		return _signsSealOwners.get(seal);
	}

	public Map<Integer, Integer> getSealOwners()
	{
		return _signsSealOwners;
	}

	public final int getSealProportion(int seal, int cabal)
	{
		if(cabal == 0)
			return 0;
		if(cabal == 1)
			return _signsDuskSealTotals.get(seal);
		return _signsDawnSealTotals.get(seal);
	}

	public final int getTotalMembers(int cabal)
	{
		int cabalMembers = 0;
		for(StatsSet sevenDat : _signsPlayerData.values())
		{
			if(sevenDat.getInteger("cabal") == cabal)
				++cabalMembers;
		}
		return cabalMembers;
	}

	public final StatsSet getPlayerStatsSet(Player player)
	{
		if(!hasRegisteredBefore(player))
			return null;
		return _signsPlayerData.get(player.getObjectId());
	}

	public int getPlayerStoneContrib(Player player)
	{
		if(!hasRegisteredBefore(player))
			return 0;
		int stoneCount = 0;
		StatsSet currPlayer = _signsPlayerData.get(player.getObjectId());
		if(getPlayerCabal(player) == 2)
		{
			stoneCount += currPlayer.getInteger("dawn_red_stones");
			stoneCount += currPlayer.getInteger("dawn_green_stones");
			stoneCount += currPlayer.getInteger("dawn_blue_stones");
		}
		else
		{
			stoneCount += currPlayer.getInteger("dusk_red_stones");
			stoneCount += currPlayer.getInteger("dusk_green_stones");
			stoneCount += currPlayer.getInteger("dusk_blue_stones");
		}
		return stoneCount;
	}

	public long getPlayerContribScore(Player player)
	{
		if(!hasRegisteredBefore(player))
			return 0L;
		StatsSet currPlayer = _signsPlayerData.get(player.getObjectId());
		if(getPlayerCabal(player) == 2)
			return currPlayer.getInteger("dawn_contribution_score");
		return currPlayer.getInteger("dusk_contribution_score");
	}

	public int getPlayerAdenaCollect(Player player)
	{
		if(!hasRegisteredBefore(player))
			return 0;
		if(getPlayerCabal(player) == 2)
			return _signsPlayerData.get(player.getObjectId()).getInteger("dawn_ancient_adena_amount");
		return _signsPlayerData.get(player.getObjectId()).getInteger("dusk_ancient_adena_amount");
	}

	public int getPlayerSeal(Player player)
	{
		if(!hasRegisteredBefore(player))
			return 0;
		return _signsPlayerData.get(player.getObjectId()).getInteger("seal");
	}

	public int getPlayerCabal(Player player)
	{
		if(!hasRegisteredBefore(player))
			return 0;
		return _signsPlayerData.get(player.getObjectId()).getInteger("cabal");
	}

	protected void restoreSevenSignsData()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT char_obj_id, cabal, seal, dawn_red_stones, dawn_green_stones, dawn_blue_stones, dawn_ancient_adena_amount, dawn_contribution_score, dusk_red_stones, dusk_green_stones, dusk_blue_stones, dusk_ancient_adena_amount, dusk_contribution_score FROM seven_signs");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int charObjId = rset.getInt("char_obj_id");
				StatsSet sevenDat = new StatsSet();
				sevenDat.set("char_obj_id", charObjId);
				sevenDat.set("cabal", getCabalNumber(rset.getString("cabal")));
				sevenDat.set("seal", rset.getInt("seal"));
				sevenDat.set("dawn_red_stones", rset.getInt("dawn_red_stones"));
				sevenDat.set("dawn_green_stones", rset.getInt("dawn_green_stones"));
				sevenDat.set("dawn_blue_stones", rset.getInt("dawn_blue_stones"));
				sevenDat.set("dawn_ancient_adena_amount", rset.getInt("dawn_ancient_adena_amount"));
				sevenDat.set("dawn_contribution_score", rset.getInt("dawn_contribution_score"));
				sevenDat.set("dusk_red_stones", rset.getInt("dusk_red_stones"));
				sevenDat.set("dusk_green_stones", rset.getInt("dusk_green_stones"));
				sevenDat.set("dusk_blue_stones", rset.getInt("dusk_blue_stones"));
				sevenDat.set("dusk_ancient_adena_amount", rset.getInt("dusk_ancient_adena_amount"));
				sevenDat.set("dusk_contribution_score", rset.getInt("dusk_contribution_score"));
				if(Config.DEBUG)
					_log.info("SevenSigns: Loaded data from DB for char ID " + charObjId + " (" + getCabalShortName(sevenDat.getInteger("cabal")) + ")");
				_signsPlayerData.put(charObjId, sevenDat);
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("SELECT * FROM seven_signs_status");
			rset = statement.executeQuery();
			while(rset.next())
			{
				_currentCycle = rset.getInt("current_cycle");
				_activePeriod = rset.getInt("active_period");
				_previousWinner = rset.getInt("previous_winner");
				_dawnStoneScore = rset.getLong("dawn_stone_score");
				_dawnFestivalScore = rset.getLong("dawn_festival_score");
				_duskStoneScore = rset.getLong("dusk_stone_score");
				_duskFestivalScore = rset.getLong("dusk_festival_score");
				_signsSealOwners.put(1, rset.getInt("avarice_owner"));
				_signsSealOwners.put(2, rset.getInt("gnosis_owner"));
				_signsSealOwners.put(3, rset.getInt("strife_owner"));
				_signsDawnSealTotals.put(1, rset.getInt("avarice_dawn_score"));
				_signsDawnSealTotals.put(2, rset.getInt("gnosis_dawn_score"));
				_signsDawnSealTotals.put(3, rset.getInt("strife_dawn_score"));
				_signsDuskSealTotals.put(1, rset.getInt("avarice_dusk_score"));
				_signsDuskSealTotals.put(2, rset.getInt("gnosis_dusk_score"));
				_signsDuskSealTotals.put(3, rset.getInt("strife_dusk_score"));
			}
			DbUtils.close(statement, rset);
			statement = con.prepareStatement("UPDATE seven_signs_status SET date=?");
			statement.setInt(1, Calendar.getInstance().get(7));
			statement.execute();
		}
		catch(SQLException e)
		{
			_log.error("Unable to load Seven Signs Data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public synchronized void saveSevenSignsData(Player player, boolean updateSettings)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		Connection con = null;
		PreparedStatement statement = null;
		if(Config.DEBUG)
			_log.info("SevenSigns: Saving data to disk.");
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(StatsSet sevenDat : _signsPlayerData.values())
			{
				if(player != null && sevenDat.getInteger("char_obj_id") != player.getObjectId())
					continue;
				statement = con.prepareStatement("UPDATE seven_signs SET cabal=?, seal=?, dawn_red_stones=?, dawn_green_stones=?, dawn_blue_stones=?, dawn_ancient_adena_amount=?, dawn_contribution_score=?, dusk_red_stones=?, dusk_green_stones=?, dusk_blue_stones=?, dusk_ancient_adena_amount=?, dusk_contribution_score=? WHERE char_obj_id=?");
				statement.setString(1, getCabalShortName(sevenDat.getInteger("cabal")));
				statement.setInt(2, sevenDat.getInteger("seal"));
				statement.setInt(3, sevenDat.getInteger("dawn_red_stones"));
				statement.setInt(4, sevenDat.getInteger("dawn_green_stones"));
				statement.setInt(5, sevenDat.getInteger("dawn_blue_stones"));
				statement.setInt(6, sevenDat.getInteger("dawn_ancient_adena_amount"));
				statement.setInt(7, sevenDat.getInteger("dawn_contribution_score"));
				statement.setInt(8, sevenDat.getInteger("dusk_red_stones"));
				statement.setInt(9, sevenDat.getInteger("dusk_green_stones"));
				statement.setInt(10, sevenDat.getInteger("dusk_blue_stones"));
				statement.setInt(11, sevenDat.getInteger("dusk_ancient_adena_amount"));
				statement.setInt(12, sevenDat.getInteger("dusk_contribution_score"));
				statement.setInt(13, sevenDat.getInteger("char_obj_id"));
				statement.execute();
				DbUtils.close(statement);
				statement = null;
				if(!Config.DEBUG)
					continue;
				_log.info("SevenSigns: Updated data in DB for char ID " + sevenDat.getInteger("char_obj_id") + " (" + getCabalShortName(sevenDat.getInteger("cabal")) + ")");
			}
			if(updateSettings)
			{
				StringBuffer buf = new StringBuffer();
				buf.append("UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, ");
				for(int i = 0; i < 5; ++i)
					buf.append("accumulated_bonus" + String.valueOf(i) + "=?, ");
				buf.append("date=?");
				statement = con.prepareStatement(buf.toString());
				statement.setInt(1, _currentCycle);
				statement.setInt(2, _activePeriod);
				statement.setInt(3, _previousWinner);
				statement.setLong(4, _dawnStoneScore);
				statement.setLong(5, _dawnFestivalScore);
				statement.setLong(6, _duskStoneScore);
				statement.setLong(7, _duskFestivalScore);
				statement.setInt(8, _signsSealOwners.get(1));
				statement.setInt(9, _signsSealOwners.get(2));
				statement.setInt(10, _signsSealOwners.get(3));
				statement.setInt(11, _signsDawnSealTotals.get(1));
				statement.setInt(12, _signsDawnSealTotals.get(2));
				statement.setInt(13, _signsDawnSealTotals.get(3));
				statement.setInt(14, _signsDuskSealTotals.get(1));
				statement.setInt(15, _signsDuskSealTotals.get(2));
				statement.setInt(16, _signsDuskSealTotals.get(3));
				statement.setInt(17, getCurrentCycle());
				for(int i = 0; i < 5; ++i)
					statement.setInt(18 + i, SevenSignsFestival.getInstance().getAccumulatedBonus(i));
				statement.setInt(23, Calendar.getInstance().get(7));
				statement.execute();
				if(Config.DEBUG)
					_log.info("SevenSigns: Updated data in SQL database.");
			}
		}
		catch(SQLException e)
		{
			_log.error("Unable to save Seven Signs data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	protected void resetPlayerData()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		if(Config.DEBUG)
			_log.info("SevenSigns: Resetting player data for new event period.");
		for(StatsSet sevenDat : _signsPlayerData.values())
		{
			int charObjId = sevenDat.getInteger("char_obj_id");
			if(sevenDat.getInteger("cabal") == getCabalHighestScore())
				switch(getCabalHighestScore())
				{
					case 2:
					{
						sevenDat.set("dawn_red_stones", 0);
						sevenDat.set("dawn_green_stones", 0);
						sevenDat.set("dawn_blue_stones", 0);
						sevenDat.set("dawn_contribution_score", 0);
						break;
					}
					case 1:
					{
						sevenDat.set("dusk_red_stones", 0);
						sevenDat.set("dusk_green_stones", 0);
						sevenDat.set("dusk_blue_stones", 0);
						sevenDat.set("dusk_contribution_score", 0);
						break;
					}
				}
			else if(sevenDat.getInteger("cabal") == 2 || sevenDat.getInteger("cabal") == 0)
			{
				sevenDat.set("dusk_red_stones", 0);
				sevenDat.set("dusk_green_stones", 0);
				sevenDat.set("dusk_blue_stones", 0);
				sevenDat.set("dusk_contribution_score", 0);
			}
			else if(sevenDat.getInteger("cabal") == 1 || sevenDat.getInteger("cabal") == 0)
			{
				sevenDat.set("dawn_red_stones", 0);
				sevenDat.set("dawn_green_stones", 0);
				sevenDat.set("dawn_blue_stones", 0);
				sevenDat.set("dawn_contribution_score", 0);
			}
			sevenDat.set("cabal", 0);
			sevenDat.set("seal", 0);
			_signsPlayerData.put(charObjId, sevenDat);
		}
	}

	private boolean hasRegisteredBefore(Player player)
	{
		return _signsPlayerData.containsKey(player.getObjectId());
	}

	public int setPlayerInfo(Player player, int chosenCabal, int chosenSeal)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return 0;

		int charObjId = player.getObjectId();
		Connection con = null;
		PreparedStatement statement = null;
		StatsSet currPlayer = null;
		if(hasRegisteredBefore(player))
		{
			currPlayer = _signsPlayerData.get(charObjId);
			currPlayer.set("cabal", chosenCabal);
			currPlayer.set("seal", chosenSeal);
			_signsPlayerData.put(charObjId, currPlayer);
		}
		else
		{
			currPlayer = new StatsSet();
			currPlayer.set("char_obj_id", charObjId);
			currPlayer.set("cabal", chosenCabal);
			currPlayer.set("seal", chosenSeal);
			currPlayer.set("dawn_red_stones", 0);
			currPlayer.set("dawn_green_stones", 0);
			currPlayer.set("dawn_blue_stones", 0);
			currPlayer.set("dawn_ancient_adena_amount", 0);
			currPlayer.set("dawn_contribution_score", 0);
			currPlayer.set("dusk_red_stones", 0);
			currPlayer.set("dusk_green_stones", 0);
			currPlayer.set("dusk_blue_stones", 0);
			currPlayer.set("dusk_ancient_adena_amount", 0);
			currPlayer.set("dusk_contribution_score", 0);
			_signsPlayerData.put(charObjId, currPlayer);
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)");
				statement.setInt(1, charObjId);
				statement.setString(2, getCabalShortName(chosenCabal));
				statement.setInt(3, chosenSeal);
				statement.execute();
				if(Config.DEBUG)
					_log.info("SevenSigns: Inserted data in DB for char ID " + currPlayer.getInteger("char_obj_id") + " (" + getCabalShortName(currPlayer.getInteger("cabal")) + ")");
			}
			catch(SQLException e)
			{
				_log.error("SevenSigns: Failed to save data: " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
		long contribScore = 0L;
		switch(chosenCabal)
		{
			case 2:
			{
				contribScore = calcContributionScore(currPlayer.getInteger("dawn_blue_stones"), currPlayer.getInteger("dawn_green_stones"), currPlayer.getInteger("dawn_red_stones"));
				_dawnStoneScore += contribScore;
				break;
			}
			case 1:
			{
				contribScore = calcContributionScore(currPlayer.getInteger("dusk_blue_stones"), currPlayer.getInteger("dusk_green_stones"), currPlayer.getInteger("dusk_red_stones"));
				_duskStoneScore += contribScore;
				break;
			}
		}

		if(currPlayer.getInteger("cabal") == 2)
			_signsDawnSealTotals.put(chosenSeal, _signsDawnSealTotals.get(chosenSeal) + 1);
		else
			_signsDuskSealTotals.put(chosenSeal, _signsDuskSealTotals.get(chosenSeal) + 1);

		saveSevenSignsData(player, true);

		if(Config.DEBUG)
			_log.info("SevenSigns: " + player.getName() + " has joined the " + getCabalName(chosenCabal) + " for the " + getSealName(chosenSeal, false) + "!");

		return chosenCabal;
	}

	public int getAncientAdenaReward(Player player, boolean removeReward)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return 0;

		int charObjId = player.getObjectId();
		StatsSet currPlayer = _signsPlayerData.get(charObjId);
		int rewardAmount = 0;
		if(currPlayer.getInteger("cabal") == 2)
		{
			rewardAmount = currPlayer.getInteger("dawn_ancient_adena_amount");
			currPlayer.set("dawn_ancient_adena_amount", 0);
		}
		else
		{
			rewardAmount = currPlayer.getInteger("dusk_ancient_adena_amount");
			currPlayer.set("dusk_ancient_adena_amount", 0);
		}
		if(removeReward)
		{
			_signsPlayerData.put(charObjId, currPlayer);
			saveSevenSignsData(player, true);
		}
		return rewardAmount;
	}

	public long addPlayerStoneContrib(Player player, long blueCount, long greenCount, long redCount)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return 0L;

		int charObjId = player.getObjectId();
		StatsSet currPlayer = _signsPlayerData.get(charObjId);
		long contribScore = calcContributionScore(blueCount, greenCount, redCount);
		long totalAncientAdena = 0L;
		long totalContribScore = 0L;
		if(currPlayer.getInteger("cabal") == 2)
		{
			totalAncientAdena = currPlayer.getInteger("dawn_ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
			totalContribScore = currPlayer.getInteger("dawn_contribution_score") + contribScore;
			if(totalContribScore > MAXIMUM_PLAYER_CONTRIB)
				return -1L;
			currPlayer.set("dawn_red_stones", currPlayer.getInteger("dawn_red_stones") + redCount);
			currPlayer.set("dawn_green_stones", currPlayer.getInteger("dawn_green_stones") + greenCount);
			currPlayer.set("dawn_blue_stones", currPlayer.getInteger("dawn_blue_stones") + blueCount);
			currPlayer.set("dawn_ancient_adena_amount", totalAncientAdena);
			currPlayer.set("dawn_contribution_score", totalContribScore);
			_signsPlayerData.put(charObjId, currPlayer);
			_dawnStoneScore += contribScore;
		}
		else
		{
			totalAncientAdena = currPlayer.getInteger("dusk_ancient_adena_amount") + calcAncientAdenaReward(blueCount, greenCount, redCount);
			totalContribScore = currPlayer.getInteger("dusk_contribution_score") + contribScore;
			if(totalContribScore > MAXIMUM_PLAYER_CONTRIB)
				return -1L;
			currPlayer.set("dusk_red_stones", currPlayer.getInteger("dusk_red_stones") + redCount);
			currPlayer.set("dusk_green_stones", currPlayer.getInteger("dusk_green_stones") + greenCount);
			currPlayer.set("dusk_blue_stones", currPlayer.getInteger("dusk_blue_stones") + blueCount);
			currPlayer.set("dusk_ancient_adena_amount", totalAncientAdena);
			currPlayer.set("dusk_contribution_score", totalContribScore);
			_signsPlayerData.put(charObjId, currPlayer);
			_duskStoneScore += contribScore;
		}
		saveSevenSignsData(player, true);
		if(Config.DEBUG)
			_log.info("SevenSigns: " + player.getName() + " contributed " + contribScore + " seal stone points to their cabal.");
		return contribScore;
	}

	public void addFestivalScore(int cabal, int amount)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		if(cabal == 1)
		{
			_duskFestivalScore += amount;
			if(_dawnFestivalScore >= amount)
				_dawnFestivalScore -= amount;
			else
				_dawnFestivalScore = 0L;
		}
		else
		{
			_dawnFestivalScore += amount;
			if(_duskFestivalScore >= amount)
				_duskFestivalScore -= amount;
			else
				_duskFestivalScore = 0L;
		}
	}

	public void sendCurrentPeriodMsg(Player player)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		SystemMessage sm = null;
		switch(_activePeriod)
		{
			case 0:
			{
				sm = new SystemMessage(1260);
				break;
			}
			case 1:
			{
				sm = new SystemMessage(1261);
				break;
			}
			case 2:
			{
				sm = new SystemMessage(1262);
				break;
			}
			case 3:
			{
				sm = new SystemMessage(1263);
				break;
			}
		}
		if(sm != null)
			player.sendPacket(sm);
	}

	public void sendMessageToAll(int sysMsgId)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		SystemMessage sm = new SystemMessage(sysMsgId);
		for(Player player : GameObjectsStorage.getPlayers())
			player.sendPacket(sm);
	}

	protected void initializeSeals()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		for(Integer currSeal : _signsSealOwners.keySet())
		{
			int sealOwner = _signsSealOwners.get(currSeal);
			if(sealOwner != 0)
			{
				if(isSealValidationPeriod())
					_log.info("SevenSigns: The " + getCabalName(sealOwner) + " have won the " + getSealName(currSeal, false) + ".");
				else
					_log.info("SevenSigns: The " + getSealName(currSeal, false) + " is currently owned by " + getCabalName(sealOwner) + ".");
			}
			else
				_log.info("SevenSigns: The " + getSealName(currSeal, false) + " remains unclaimed.");
		}
	}

	protected void resetSeals()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_signsDawnSealTotals.put(1, 0);
		_signsDawnSealTotals.put(2, 0);
		_signsDawnSealTotals.put(3, 0);
		_signsDuskSealTotals.put(1, 0);
		_signsDuskSealTotals.put(2, 0);
		_signsDuskSealTotals.put(3, 0);
	}

	protected void calcNewSealOwners()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		if(Config.DEBUG)
		{
			_log.info("SevenSigns: (Avarice) Dawn = " + _signsDawnSealTotals.get(1) + ", Dusk = " + _signsDuskSealTotals.get(1));
			_log.info("SevenSigns: (Gnosis) Dawn = " + _signsDawnSealTotals.get(2) + ", Dusk = " + _signsDuskSealTotals.get(2));
			_log.info("SevenSigns: (Strife) Dawn = " + _signsDawnSealTotals.get(3) + ", Dusk = " + _signsDuskSealTotals.get(3));
		}
		for(Integer currSeal : _signsDawnSealTotals.keySet())
		{
			int prevSealOwner = _signsSealOwners.get(currSeal);
			int newSealOwner = 0;
			int dawnProportion = getSealProportion(currSeal, 2);
			int totalDawnMembers = getTotalMembers(2) == 0 ? 1 : getTotalMembers(2);
			int duskProportion = getSealProportion(currSeal, 1);
			int totalDuskMembers = getTotalMembers(1) == 0 ? 1 : getTotalMembers(1);

			switch(prevSealOwner)
			{
				case 0:
				{
					switch(getCabalHighestScore())
					{
						case 0:
						{
							if(dawnProportion >= Math.round(0.35 * totalDawnMembers) && dawnProportion > duskProportion)
							{
								newSealOwner = 2;
								break;
							}
							if(duskProportion >= Math.round(0.35 * totalDuskMembers) && duskProportion > dawnProportion)
							{
								newSealOwner = 1;
								break;
							}
							newSealOwner = prevSealOwner;
							break;
						}
						case 2:
						{
							if(dawnProportion >= Math.round(0.35 * totalDawnMembers))
							{
								newSealOwner = 2;
								break;
							}
							if(duskProportion >= Math.round(0.35 * totalDuskMembers))
							{
								newSealOwner = 1;
								break;
							}
							newSealOwner = prevSealOwner;
							break;
						}
						case 1:
						{
							if(duskProportion >= Math.round(0.35 * totalDuskMembers))
							{
								newSealOwner = 1;
								break;
							}
							if(dawnProportion >= Math.round(0.35 * totalDawnMembers))
							{
								newSealOwner = 2;
								break;
							}
							newSealOwner = prevSealOwner;
							break;
						}
					}
					break;
				}
				case 2:
				{
					switch(getCabalHighestScore())
					{
						case 0:
						{
							if(dawnProportion >= Math.round(0.1 * totalDawnMembers))
							{
								newSealOwner = prevSealOwner;
								break;
							}
							if(duskProportion >= Math.round(0.35 * totalDuskMembers))
							{
								newSealOwner = 1;
								break;
							}
							newSealOwner = 0;
							break;
						}
						case 2:
						{
							if(dawnProportion >= Math.round(0.1 * totalDawnMembers))
							{
								newSealOwner = prevSealOwner;
								break;
							}
							if(duskProportion >= Math.round(0.35 * totalDuskMembers))
							{
								newSealOwner = 1;
								break;
							}
							newSealOwner = 0;
							break;
						}
						case 1:
						{
							if(duskProportion >= Math.round(0.1 * totalDuskMembers))
							{
								newSealOwner = 1;
								break;
							}
							if(dawnProportion >= Math.round(0.35 * totalDawnMembers))
							{
								newSealOwner = prevSealOwner;
								break;
							}
							newSealOwner = 0;
							break;
						}
					}
					break;
				}
				case 1:
				{
					switch(getCabalHighestScore())
					{
						case 0:
						{
							if(duskProportion >= Math.round(0.1 * totalDuskMembers))
							{
								newSealOwner = prevSealOwner;
								break;
							}
							if(dawnProportion >= Math.round(0.35 * totalDawnMembers))
							{
								newSealOwner = 2;
								break;
							}
							newSealOwner = 0;
							break;
						}
						case 2:
						{
							if(dawnProportion >= Math.round(0.35 * totalDawnMembers))
							{
								newSealOwner = 2;
								break;
							}
							if(duskProportion >= Math.round(0.1 * totalDuskMembers))
							{
								newSealOwner = prevSealOwner;
								break;
							}
							newSealOwner = 0;
							break;
						}
						case 1:
						{
							if(duskProportion >= Math.round(0.1 * totalDuskMembers))
							{
								newSealOwner = prevSealOwner;
								break;
							}
							if(dawnProportion >= Math.round(0.35 * totalDawnMembers))
							{
								newSealOwner = 2;
								break;
							}
							newSealOwner = 0;
							break;
						}
					}
					break;
				}
			}
			_signsSealOwners.put(currSeal, newSealOwner);
			switch(currSeal)
			{
				case 1:
				{
					if(newSealOwner == 2)
					{
						sendMessageToAll(1212);
						continue;
					}
					if(newSealOwner == 1)
					{
						sendMessageToAll(1215);
						continue;
					}
					continue;
				}
				case 2:
				{
					if(newSealOwner == 2)
					{
						sendMessageToAll(1213);
						continue;
					}
					if(newSealOwner == 1)
					{
						sendMessageToAll(1216);
						continue;
					}
					continue;
				}
				case 3:
				{
					if(newSealOwner == 2)
					{
						sendMessageToAll(1214);
						continue;
					}
					if(newSealOwner == 1)
					{
						sendMessageToAll(1217);
						continue;
					}
					continue;
				}
			}
		}
	}

	private void teleLosingCabalFromDungeons(Integer compWinner)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		for(Player onlinePlayer : GameObjectsStorage.getPlayers())
		{
			if(onlinePlayer == null)
				continue;
			int charObjId = onlinePlayer.getObjectId();
			StatsSet currPlayer = _signsPlayerData.get(charObjId);
			if(currPlayer != null && (isSealValidationPeriod() || isCompResultsPeriod()))
			{
				if(onlinePlayer.isGM() || !onlinePlayer.isIn7sDungeon() || currPlayer.getInteger("cabal") == compWinner)
					continue;
				onlinePlayer.teleToClosestTown();
				onlinePlayer.sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
			}
			else
			{
				if(onlinePlayer.isGM() || !onlinePlayer.isIn7sDungeon() || currPlayer != null && currPlayer.getInteger("cabal") != 0)
					continue;
				onlinePlayer.teleToClosestTown();
				onlinePlayer.sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
			}
		}
	}

	public int getPriestCabal(int id)
	{
		switch(id)
		{
			case 31078:
			case 31079:
			case 31080:
			case 31081:
			case 31082:
			case 31083:
			case 31084:
			case 31168:
			case 31692:
			case 31694:
			case 31997:
				return 2;
			case 31085:
			case 31086:
			case 31087:
			case 31088:
			case 31089:
			case 31090:
			case 31091:
			case 31169:
			case 31693:
			case 31695:
			case 31998:
				return 1;
			default:
				return 0;
		}
	}

	public void changePeriod()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), 10L);
	}

	public void changePeriod(int period)
	{
		changePeriod(period, 1);
	}

	public void changePeriod(int period, int seconds)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_activePeriod = period - 1;
		if(_activePeriod < 0)
			_activePeriod += 4;
		_periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), seconds * 1000);
	}

	public void setTimeToNextPeriodChange(int time)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_calendar.setTimeInMillis(System.currentTimeMillis() + time * 60 * 1000);
		if(_periodChange != null)
			_periodChange.cancel(false);
		_periodChange = ThreadPoolManager.getInstance().schedule(new SevenSignsPeriodChange(), getMilliToPeriodChange());
	}

	public int getSky()
	{
		return _sky;
	}
}
