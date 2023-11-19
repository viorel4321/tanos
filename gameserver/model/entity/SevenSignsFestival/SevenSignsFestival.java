package l2s.gameserver.model.entity.SevenSignsFestival;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.World;
import l2s.gameserver.model.SpawnListener;
import l2s.gameserver.model.entity.SevenSigns;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.ChatType;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2s.gameserver.network.l2.s2c.Say2;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.templates.StatsSet;

public class SevenSignsFestival implements SpawnListener
{
	public enum FestivalStatus
	{
		Begining,
		Signup,
		Started,
		FirstSpawn,
		FirstSwarm,
		SecondSpawn,
		SecondSwarm,
		ChestSpawn,
		Ending;
	}

	private static Logger _log = LoggerFactory.getLogger(SevenSignsFestival.class);

	private static SevenSignsFestival _instance;
	private static final SevenSigns _signsInstance = SevenSigns.getInstance();

	public static final String GET_CLAN_NAME = "SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)";
	public static final int FESTIVAL_MANAGER_START = 120000;
	public static final int FESTIVAL_LENGTH = 1080000;
	public static final int FESTIVAL_CYCLE_LENGTH = 2280000;
	public static final int FESTIVAL_SIGNUP_TIME = 1200000;
	public static final int FESTIVAL_FIRST_SPAWN = 120000;
	public static final int FESTIVAL_FIRST_SWARM = 300000;
	public static final int FESTIVAL_SECOND_SPAWN = 540000;
	public static final int FESTIVAL_SECOND_SWARM = 720000;
	public static final int FESTIVAL_CHEST_SPAWN = 900000;
	public static final int FESTIVAL_MAX_OFFSET = 230;
	public static final int FESTIVAL_DEFAULT_RESPAWN = 60;
	public static final int FESTIVAL_COUNT = 5;
	public static final int FESTIVAL_LEVEL_MAX_31 = 0;
	public static final int FESTIVAL_LEVEL_MAX_42 = 1;
	public static final int FESTIVAL_LEVEL_MAX_53 = 2;
	public static final int FESTIVAL_LEVEL_MAX_64 = 3;
	public static final int FESTIVAL_LEVEL_MAX_NONE = 4;
	public static final int[] FESTIVAL_LEVEL_SCORES = new int[] { 60, 70, 100, 120, 150 };
	public static final short FESTIVAL_OFFERING_ID = 5901;
	public static final short FESTIVAL_OFFERING_VALUE = 5;
	private static FestivalManager _managerInstance;
	private static ScheduledFuture<?> _managerScheduledTask;
	private static long _nextFestivalCycleStart;
	private static long _nextFestivalStart;
	private static boolean _festivalInitialized;
	private static boolean _festivalInProgress;

	private static NpcInstance _dawnChatGuide;
	private static NpcInstance _duskChatGuide;

	private static final int[] _accumulatedBonuses = new int[5];
	private static final Map<Integer, List<Player>> _dawnFestivalParticipants = new HashMap<Integer, List<Player>>();
	private static final Map<Integer, List<Player>> _duskFestivalParticipants = new HashMap<Integer, List<Player>>();
	private static final Map<Integer, List<Player>> _dawnPreviousParticipants = new HashMap<Integer, List<Player>>();
	private static final Map<Integer, List<Player>> _duskPreviousParticipants = new HashMap<Integer, List<Player>>();
	private static final Map<Integer, Integer> _dawnFestivalScores = new HashMap<Integer, Integer>();
	private static final Map<Integer, Integer> _duskFestivalScores = new HashMap<Integer, Integer>();
	private static final Map<Integer, Map<Integer, StatsSet>> _festivalData = new HashMap<Integer, Map<Integer, StatsSet>>();

	private static Map<Integer, L2DarknessFestival> _festivalInstances;

	public SevenSignsFestival()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		restoreFestivalData();
		Spawn.addSpawnListener(this);
		if(_signsInstance.isSealValidationPeriod())
		{
			_log.info("SevenSignsFestival: Initialization bypassed due to Seal Validation in effect.");
			return;
		}
		startFestivalManager();
	}

	public static SevenSignsFestival getInstance()
	{
		if(_instance == null)
			_instance = new SevenSignsFestival();
		return _instance;
	}

	public static String getFestivalName(int festivalID)
	{
		String festivalName = null;
		switch(festivalID)
		{
			case 0:
			{
				festivalName = "Level 31 or lower";
				break;
			}
			case 1:
			{
				festivalName = "Level 42 or lower";
				break;
			}
			case 2:
			{
				festivalName = "Level 53 or lower";
				break;
			}
			case 3:
			{
				festivalName = "Level 64 or lower";
				break;
			}
			default:
			{
				festivalName = "No Level Limit";
				break;
			}
		}
		return festivalName;
	}

	public static int getMaxLevelForFestival(int festivalId)
	{
		int maxLevel = 80;
		switch(festivalId)
		{
			case 0:
			{
				maxLevel = 31;
				break;
			}
			case 1:
			{
				maxLevel = 42;
				break;
			}
			case 2:
			{
				maxLevel = 53;
				break;
			}
			case 3:
			{
				maxLevel = 64;
				break;
			}
		}
		return maxLevel;
	}

	public static String implodeString(List<String> strArray, String delimiter)
	{
		StringBuilder sb = new StringBuilder();
		for(String strValue : strArray)
			sb.append(strValue + delimiter);
		sb.delete(sb.length() - delimiter.length(), sb.length() - 1);
		return sb.toString();
	}

	public void startFestivalManager()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		_managerInstance = new FestivalManager(FestivalStatus.Begining);
		setNextFestivalStart(1320000L);
		_managerScheduledTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(_managerInstance, 120000L, 2281000L);
		_log.info("SevenSignsFestival: The first Festival of Darkness cycle begins in 2 minute(s).");
	}

	public void stopFestivalManager()
	{
		if(_managerScheduledTask != null)
			_managerScheduledTask.cancel(false);
	}

	private void restoreFestivalData()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT festivalId, cabal, cycle, date, score, members FROM seven_signs_festival");
			rset = statement.executeQuery();
			while(rset.next())
			{
				int cycle = _signsInstance.getCurrentCycle();
				int festivalId = rset.getInt("festivalId");
				int cabal = SevenSigns.getCabalNumber(rset.getString("cabal"));
				StatsSet festivalDat = new StatsSet();
				festivalDat.set("festivalId", festivalId);
				festivalDat.set("cabal", cabal);
				festivalDat.set("cycle", cycle);
				festivalDat.set("date", rset.getString("date"));
				festivalDat.set("score", rset.getInt("score"));
				festivalDat.set("members", rset.getString("members"));
				if(cabal == 2)
					festivalId += 5;
				Map<Integer, StatsSet> tempData = _festivalData.get(cycle);
				if(tempData == null)
					tempData = new HashMap<Integer, StatsSet>();
				tempData.put(festivalId, festivalDat);
				_festivalData.put(cycle, tempData);
			}
			DbUtils.closeQuietly(statement, rset);
			StringBuffer query = new StringBuffer("SELECT festival_cycle, ");
			for(int i = 0; i < 4; ++i)
				query.append("accumulated_bonus" + String.valueOf(i) + ", ");
			query.append("accumulated_bonus" + String.valueOf(4) + " ");
			query.append("FROM seven_signs_status");
			statement = con.prepareStatement(query.toString());
			rset = statement.executeQuery();
			while(rset.next())
				for(int i = 0; i < 5; ++i)
					_accumulatedBonuses[i] = rset.getInt("accumulated_bonus" + String.valueOf(i));
		}
		catch(SQLException e)
		{
			_log.error("SevenSignsFestival: Failed to load configuration: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public synchronized void saveFestivalData(boolean updateSettings)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			for(Map<Integer, StatsSet> currCycleData : _festivalData.values())
			{
				for(StatsSet festivalDat : currCycleData.values())
				{
					int festivalCycle = festivalDat.getInteger("cycle");
					int festivalId = festivalDat.getInteger("festivalId");
					String cabal = SevenSigns.getCabalShortName(festivalDat.getInteger("cabal"));
					statement = con.prepareStatement("UPDATE seven_signs_festival SET date=?, score=?, members=? WHERE cycle=? AND cabal=? AND festivalId=?");
					statement.setLong(1, Long.valueOf(festivalDat.getString("date")));
					statement.setInt(2, festivalDat.getInteger("score"));
					statement.setString(3, festivalDat.getString("members"));
					statement.setInt(4, festivalCycle);
					statement.setString(5, cabal);
					statement.setInt(6, festivalId);
					if(statement.executeUpdate() > 0)
						statement.close();
					else
					{
						DbUtils.close(statement);
						PreparedStatement statement2 = con.prepareStatement("INSERT INTO seven_signs_festival (festivalId, cabal, cycle, date, score, members) VALUES (?,?,?,?,?,?)");
						statement2.setInt(1, festivalId);
						statement2.setString(2, cabal);
						statement2.setInt(3, festivalCycle);
						statement2.setLong(4, Long.valueOf(festivalDat.getString("date")));
						statement2.setInt(5, festivalDat.getInteger("score"));
						statement2.setString(6, festivalDat.getString("members"));
						statement2.execute();
						DbUtils.close(statement2);
					}
				}
			}
		}
		catch(SQLException e)
		{
			_log.error("SevenSignsFestival: Failed to save configuration: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}

		if(updateSettings)
			_signsInstance.saveSevenSignsData(null, true);
	}

	public void rewardHighestRanked()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		StatsSet overallData = getOverallHighestScoreData(0);
		if(overallData != null)
		{
			String[] split;
			String[] partyMembers = split = overallData.getString("members").split(",");
			for(String partyMemberName : split)
				addReputationPointsForPartyMemberClan(partyMemberName);
		}
		overallData = getOverallHighestScoreData(1);
		if(overallData != null)
		{
			String[] split2;
			String[] partyMembers = split2 = overallData.getString("members").split(",");
			for(String partyMemberName : split2)
				addReputationPointsForPartyMemberClan(partyMemberName);
		}
		overallData = getOverallHighestScoreData(2);
		if(overallData != null)
		{
			String[] split3;
			String[] partyMembers = split3 = overallData.getString("members").split(",");
			for(String partyMemberName : split3)
				addReputationPointsForPartyMemberClan(partyMemberName);
		}
		overallData = getOverallHighestScoreData(3);
		if(overallData != null)
		{
			String[] split4;
			String[] partyMembers = split4 = overallData.getString("members").split(",");
			for(String partyMemberName : split4)
				addReputationPointsForPartyMemberClan(partyMemberName);
		}
		overallData = getOverallHighestScoreData(4);
		if(overallData != null)
		{
			String[] split5;
			String[] partyMembers = split5 = overallData.getString("members").split(",");
			for(String partyMemberName : split5)
				addReputationPointsForPartyMemberClan(partyMemberName);
		}
	}

	private void addReputationPointsForPartyMemberClan(String partyMemberName)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		Player player = World.getPlayer(partyMemberName);
		if(player != null)
		{
			if(player.getClan() != null)
			{
				player.getClan().incReputation(100, true, "SevenSignsFestival");
				player.getClan().broadcastToOnlineMembers(new PledgeShowInfoUpdate(player.getClan()));
				SystemMessage sm = new SystemMessage(1775);
				sm.addString(partyMemberName);
				sm.addNumber(Integer.valueOf(100));
				player.getClan().broadcastToOnlineMembers(sm);
			}
		}
		else
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("SELECT clan_name FROM clan_data WHERE clan_id = (SELECT clanid FROM characters WHERE char_name = ?)");
				statement.setString(1, partyMemberName);
				ResultSet rset = statement.executeQuery();
				if(rset.next())
				{
					String clanName = rset.getString("clan_name");
					if(clanName != null)
					{
						Clan clan = ClanTable.getInstance().getClanByName(clanName);
						if(clan != null)
						{
							clan.incReputation(100, true, "SevenSignsFestival");
							clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
							SystemMessage sm2 = new SystemMessage(1775);
							sm2.addString(partyMemberName);
							sm2.addNumber(Integer.valueOf(100));
							clan.broadcastToOnlineMembers(sm2);
						}
					}
				}
				DbUtils.close(rset);
				DbUtils.close(statement);
			}
			catch(Exception e)
			{
				_log.warn("could not get clan name of " + partyMemberName + ": " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con);
			}
		}
	}

	public void resetFestivalData(boolean updateSettings)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		for(int i = 0; i < 5; ++i)
			_accumulatedBonuses[i] = 0;

		_dawnPreviousParticipants.clear();
		_duskPreviousParticipants.clear();
		_dawnFestivalParticipants.clear();
		_duskFestivalParticipants.clear();
		_dawnFestivalScores.clear();
		_duskFestivalScores.clear();

		Map<Integer, StatsSet> newData = new HashMap<Integer, StatsSet>();
		for(int j = 0; j < 10; ++j)
		{
			int festivalId;
			if((festivalId = j) >= 5)
				festivalId -= 5;
			StatsSet tempStats = new StatsSet();
			tempStats.set("festivalId", festivalId);
			tempStats.set("cycle", _signsInstance.getCurrentCycle());
			tempStats.set("date", "0");
			tempStats.set("score", 0);
			tempStats.set("members", "");
			if(j >= 5)
				tempStats.set("cabal", 2);
			else
				tempStats.set("cabal", 1);
			newData.put(j, tempStats);
		}
		_festivalData.put(_signsInstance.getCurrentCycle(), newData);
		saveFestivalData(updateSettings);
		for(Player onlinePlayer : GameObjectsStorage.getPlayers())
		{
			ItemInstance bloodOfferings = onlinePlayer.getInventory().findItemByItemId(5901);
			if(bloodOfferings != null)
				onlinePlayer.getInventory().destroyItem(bloodOfferings, bloodOfferings.getIntegerLimitedCount(), true);
		}
		_log.info("SevenSignsFestival: Reinitialized engine for next competition period.");
	}

	public boolean isFestivalInitialized()
	{
		return _festivalInitialized;
	}

	public static void setFestivalInitialized(boolean isInitialized)
	{
		_festivalInitialized = isInitialized;
	}

	public boolean isFestivalInProgress()
	{
		return _festivalInProgress;
	}

	public static void setFestivalInProgress(boolean inProgress)
	{
		_festivalInProgress = inProgress;
	}

	public static void setNextCycleStart()
	{
		_nextFestivalCycleStart = System.currentTimeMillis() + 2280000L;
	}

	public static void setNextFestivalStart(long milliFromNow)
	{
		_nextFestivalStart = System.currentTimeMillis() + milliFromNow;
	}

	public int getMinsToNextCycle()
	{
		if(_signsInstance.isSealValidationPeriod())
			return -1;
		return Math.round((_nextFestivalCycleStart - System.currentTimeMillis()) / 1000L / 60L);
	}

	public static int getMinsToNextFestival()
	{
		if(_signsInstance.isSealValidationPeriod())
			return -1;
		return Math.round((_nextFestivalStart - System.currentTimeMillis()) / 1000L / 60L) + 1;
	}

	public String getTimeToNextFestivalStr()
	{
		if(_signsInstance.isSealValidationPeriod())
			return "<font color=\"FF0000\">This is the Seal Validation period. Festivals will resume next week.</font>";
		return "<font color=\"FF0000\">The next festival will begin in " + getMinsToNextFestival() + " minute(s).</font>";
	}

	public int[] getFestivalForPlayer(Player player)
	{
		int[] playerFestivalInfo = { -1, -1 };
		for(int festivalId = 0; festivalId < 5; ++festivalId)
		{
			List<Player> participants = getDawnFestivalParticipants().get(festivalId);
			if(participants != null && participants.contains(player))
			{
				playerFestivalInfo[0] = 2;
				playerFestivalInfo[1] = festivalId;
				return playerFestivalInfo;
			}
			++festivalId;
			participants = getDuskFestivalParticipants().get(festivalId);
			if(participants != null && participants.contains(player))
			{
				playerFestivalInfo[playerFestivalInfo[0] = 1] = festivalId;
				return playerFestivalInfo;
			}
		}
		return playerFestivalInfo;
	}

	public boolean isParticipant(Player player)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return false;
		if(_signsInstance.isSealValidationPeriod())
			return false;
		if(_managerInstance == null)
			return false;
		for(List<Player> participants : getDawnFestivalParticipants().values())
		{
			if(participants.contains(player))
				return true;
		}
		for(List<Player> participants : getDuskFestivalParticipants().values())
		{
			if(participants.contains(player))
				return true;
		}
		return false;
	}

	public List<Player> getParticipants(int oracle, int festivalId)
	{
		if(oracle == 2)
			return getDawnFestivalParticipants().get(festivalId);
		return getDuskFestivalParticipants().get(festivalId);
	}

	public List<Player> getPreviousParticipants(int oracle, int festivalId)
	{
		if(oracle == 2)
			return getDawnPreviousParticipants().get(festivalId);
		return getDuskPreviousParticipants().get(festivalId);
	}

	public void setParticipants(int oracle, int festivalId, Party festivalParty)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		List<Player> participants = new CopyOnWriteArrayList<Player>();
		if(festivalParty != null)
		{
			for(Player p : festivalParty.getPartyMembers())
				participants.add(p);
		}
		if(oracle == 2)
			getDawnFestivalParticipants().put(festivalId, participants);
		else
			getDuskFestivalParticipants().put(festivalId, participants);
	}

	public void updateParticipants(Player player, Party festivalParty)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;
		if(!isParticipant(player))
			return;
		int[] playerFestInfo = getFestivalForPlayer(player);
		int oracle = playerFestInfo[0];
		int festivalId = playerFestInfo[1];
		if(festivalId > -1)
		{
			if(_festivalInitialized)
			{
				L2DarknessFestival festivalInst = getFestivalInstance(oracle, festivalId);
				if(festivalParty == null)
					for(Player partyMember : getParticipants(oracle, festivalId))
						festivalInst.relocatePlayer(partyMember, true);
				else
					festivalInst.relocatePlayer(player, true);
			}
			setParticipants(oracle, festivalId, festivalParty);
		}
	}

	public int getHighestScore(int oracle, int festivalId)
	{
		return getHighestScoreData(oracle, festivalId).getInteger("score");
	}

	public StatsSet getHighestScoreData(int oracle, int festivalId)
	{
		int offsetId = festivalId;
		if(oracle == 2)
			offsetId += 5;
		StatsSet currData = null;
		try
		{
			currData = _festivalData.get(_signsInstance.getCurrentCycle()).get(offsetId);
		}
		catch(Exception e)
		{
			_log.info("SSF: Error while getting scores");
			_log.info("oracle=" + oracle + " festivalId=" + festivalId + " offsetId" + offsetId + " _signsCycle" + _signsInstance.getCurrentCycle());
			_log.info("_festivalData=" + _festivalData.toString());
			e.printStackTrace();
		}
		if(currData == null)
		{
			currData = new StatsSet();
			currData.set("score", 0);
			currData.set("members", "");
			_log.warn("SevenSignsFestival: Data missing for " + SevenSigns.getCabalName(oracle) + ", FestivalID = " + festivalId + " (Current Cycle " + _signsInstance.getCurrentCycle() + ")");
		}
		return currData;
	}

	public StatsSet getOverallHighestScoreData(int festivalId)
	{
		StatsSet result = null;
		int highestScore = 0;
		for(Map<Integer, StatsSet> currCycleData : _festivalData.values())
		{
			for(StatsSet currFestData : currCycleData.values())
			{
				int currFestID = currFestData.getInteger("festivalId");
				int festivalScore = currFestData.getInteger("score");
				if(currFestID != festivalId)
					continue;
				if(festivalScore <= highestScore)
					continue;
				highestScore = festivalScore;
				result = currFestData;
			}
		}
		return result;
	}

	public boolean setFinalScore(Player player, int oracle, int festivalId, int offeringScore)
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return false;

		int currDawnHighScore = getHighestScore(2, festivalId);
		int currDuskHighScore = getHighestScore(1, festivalId);
		int thisCabalHighScore = 0;
		int otherCabalHighScore = 0;
		if(oracle == 2)
		{
			thisCabalHighScore = currDawnHighScore;
			otherCabalHighScore = currDuskHighScore;
			_dawnFestivalScores.put(festivalId, offeringScore);
		}
		else
		{
			thisCabalHighScore = currDuskHighScore;
			otherCabalHighScore = currDawnHighScore;
			_duskFestivalScores.put(festivalId, offeringScore);
		}
		StatsSet currFestData = getHighestScoreData(oracle, festivalId);
		if(offeringScore <= thisCabalHighScore)
			return false;
		if(thisCabalHighScore > otherCabalHighScore)
			return false;
		List<String> partyMembers = new ArrayList<String>();
		List<Player> prevParticipants = getPreviousParticipants(oracle, festivalId);
		for(Player partyMember : prevParticipants)
			partyMembers.add(partyMember.getName());
		currFestData.set("date", String.valueOf(System.currentTimeMillis()));
		currFestData.set("score", offeringScore);
		currFestData.set("members", implodeString(partyMembers, ","));
		if(offeringScore > otherCabalHighScore)
		{
			int contribPoints = FESTIVAL_LEVEL_SCORES[festivalId];
			_signsInstance.addFestivalScore(oracle, contribPoints);
		}
		saveFestivalData(true);
		return true;
	}

	public int getAccumulatedBonus(int festivalId)
	{
		return _accumulatedBonuses[festivalId];
	}

	public void addAccumulatedBonus(int festivalId, int stoneType, int stoneAmount)
	{
		int eachStoneBonus = 0;
		switch(stoneType)
		{
			case 6360:
			{
				eachStoneBonus = 3;
				break;
			}
			case 6361:
			{
				eachStoneBonus = 5;
				break;
			}
			case 6362:
			{
				eachStoneBonus = 10;
				break;
			}
		}
		int[] accumulatedBonuses = _accumulatedBonuses;
		accumulatedBonuses[festivalId] += stoneAmount * eachStoneBonus;
	}

	public int distribAccumulatedBonus(Player player)
	{
		int playerCabal = _signsInstance.getPlayerCabal(player);
		if(playerCabal != _signsInstance.getCabalHighestScore())
			return 0;

		int playerBonus = 0;
		String playerName = player.getName();
		for(StatsSet festivalData : _festivalData.get(_signsInstance.getCurrentCycle()).values())
		{
			if(festivalData.getString("members").indexOf(playerName) > -1)
			{
				int festivalId = festivalData.getInteger("festivalId");
				int numPartyMembers = festivalData.getString("members").split(",").length;
				int totalAccumBonus = _accumulatedBonuses[festivalId];
				playerBonus = totalAccumBonus / numPartyMembers;
				_accumulatedBonuses[festivalId] = totalAccumBonus - playerBonus;
				break;
			}
		}
		return playerBonus;
	}

	public static void sendMessageToAll(String senderName, String message)
	{
		if(!Config.ALLOW_SEVEN_SIGNS || _dawnChatGuide == null || _duskChatGuide == null)
			return;

		_dawnChatGuide.broadcastPacket(new Say2(_dawnChatGuide.getObjectId(), ChatType.SHOUT, senderName, message));
		_duskChatGuide.broadcastPacket(new Say2(_duskChatGuide.getObjectId(), ChatType.SHOUT, senderName, message));
	}

	public boolean increaseChallenge(int oracle, int festivalId)
	{
		return getFestivalInstance(oracle, festivalId) != null && getFestivalInstance(oracle, festivalId).increaseChallenge();
	}

	@Override
	public void npcSpawned(NpcInstance npc)
	{
		if(npc == null)
			return;
		if(npc.getNpcId() == 31127)
			_dawnChatGuide = npc;
		if(npc.getNpcId() == 31137)
			_duskChatGuide = npc;
	}

	@Override
	public void npcDeSpawned(NpcInstance npc)
	{}

	public final L2DarknessFestival getFestivalInstance(int oracle, int festivalId)
	{
		if(!_festivalInitialized)
			return null;
		return _festivalInstances.get(festivalId + (oracle == 1 ? 10 : 20));
	}

	public void setDawnChat(NpcInstance npc)
	{
		_dawnChatGuide = npc;
	}

	public void setDuskChat(NpcInstance npc)
	{
		_duskChatGuide = npc;
	}

	public static Map<Integer, List<Player>> getDawnFestivalParticipants()
	{
		return _dawnFestivalParticipants;
	}

	public static Map<Integer, List<Player>> getDuskFestivalParticipants()
	{
		return _duskFestivalParticipants;
	}

	public static Map<Integer, List<Player>> getDawnPreviousParticipants()
	{
		return _dawnPreviousParticipants;
	}

	public static Map<Integer, List<Player>> getDuskPreviousParticipants()
	{
		return _duskPreviousParticipants;
	}

	public static Map<Integer, L2DarknessFestival> getFestivalInstances()
	{
		return _festivalInstances;
	}

	public static void setFestivalInstances(Map<Integer, L2DarknessFestival> instances)
	{
		_festivalInstances = instances;
	}

	public static void setManagerInstance(FestivalManager instance)
	{
		_managerInstance = instance;
	}
}
