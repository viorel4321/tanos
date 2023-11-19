package l2s.gameserver.instancemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Announcements;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.Spawn;
import l2s.gameserver.model.instances.RaidBossInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.GmListTable;
import l2s.gameserver.templates.StatsSet;
import l2s.gameserver.utils.SqlBatch;
import l2s.gameserver.utils.TimeUtils;

public class RaidBossSpawnManager
{
	private static Logger _log;
	private static RaidBossSpawnManager _instance;
	protected static Map<Integer, Spawn> _spawntable;
	protected static Map<Integer, RaidBossInstance> _bosses;
	protected static Map<Integer, StatsSet> _storedInfo;
	protected static Map<Integer, Map<Integer, Integer>> _points;
	protected static Map<Integer, Integer> _pointsReward;
	public static final Integer KEY_RANK;
	public static final Integer KEY_TOTAL_POINTS;
	private ReentrantLock pointsLock;

	private RaidBossSpawnManager()
	{
		pointsLock = new ReentrantLock();
		RaidBossSpawnManager._instance = this;
		if(!Config.DONTLOADSPAWN)
			reloadBosses();
	}

	public void reloadBosses()
	{
		loadRaidPoinsValuesPath();
		loadStatus();
		restorePointsTable();
		calculateRanking();
	}

	public void cleanUp()
	{
		updateAllStatusDb(false);
		updatePointsDb();
		RaidBossSpawnManager._bosses.clear();
		RaidBossSpawnManager._storedInfo.clear();
		RaidBossSpawnManager._spawntable.clear();
		RaidBossSpawnManager._points.clear();
		RaidBossSpawnManager._log.info("RaidBossSpawnManager: All raidboss info saved!");
	}

	public static RaidBossSpawnManager getInstance()
	{
		if(RaidBossSpawnManager._instance == null)
			new RaidBossSpawnManager();
		return RaidBossSpawnManager._instance;
	}

	private void loadStatus()
	{
		RaidBossSpawnManager._storedInfo = new HashMap<Integer, StatsSet>();
		Connection con = null;
		final Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			rset = con.createStatement().executeQuery("SELECT * FROM `raidboss_status`");
			while(rset.next())
			{
				final int id = rset.getInt("id");
				final StatsSet info = new StatsSet();
				info.set("current_hp", rset.getDouble("current_hp"));
				info.set("current_mp", rset.getDouble("current_mp"));
				info.set("respawn_delay", rset.getInt("respawn_delay"));
				RaidBossSpawnManager._storedInfo.put(id, info);
			}
		}
		catch(Exception e)
		{
			RaidBossSpawnManager._log.warn("RaidBossSpawnManager: Couldnt load raidboss statuses");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		RaidBossSpawnManager._log.info("RaidBossSpawnManager: Loaded " + RaidBossSpawnManager._storedInfo.size() + " Statuses");
	}

	private void loadRaidPoinsValuesPath()
	{
		LineNumberReader lnr = null;
		try
		{
			final File data = new File(Config.DATAPACK_ROOT, "data/raidpoints.csv");
			lnr = new LineNumberReader(new BufferedReader(new FileReader(data)));
			String line = null;
			while((line = lnr.readLine()) != null)
				if(line.trim().length() != 0)
				{
					if(line.startsWith("#"))
						continue;
					final StringTokenizer st = new StringTokenizer(line, ";");
					RaidBossSpawnManager._pointsReward.put(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken()));
				}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if(lnr != null)
					lnr.close();
			}
			catch(Exception ex)
			{}
		}
	}

	public void updateAllStatusDb(final boolean save)
	{
		for(final int id : RaidBossSpawnManager._storedInfo.keySet())
			updateStatusDb(id, save);
	}

	private void updateStatusDb(final int id, final boolean save)
	{
		if(!RaidBossSpawnManager._spawntable.containsKey(id))
			return;
		StatsSet info = RaidBossSpawnManager._storedInfo.get(id);
		if(info == null)
			RaidBossSpawnManager._storedInfo.put(id, (info = new StatsSet()));
		final RaidBossInstance raidboss = RaidBossSpawnManager._bosses.get(id);
		if(raidboss != null && raidboss.getRaidStatus() == Status.ALIVE)
		{
			if(save && raidboss.getCurrentHpPercents() > Config.SAVE_BOSS_HP)
				return;
			info.set("current_hp", raidboss.getCurrentHp());
			info.set("current_mp", raidboss.getCurrentMp());
			info.set("respawn_delay", 0);
		}
		else
		{
			if(save)
				return;
			info.set("current_hp", 0);
			info.set("current_mp", 0);
			if(raidboss != null && raidboss.getSpawn() != null)
				info.set("respawn_delay", raidboss.getSpawn().getRespawnTime());
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO `raidboss_status` (id, current_hp, current_mp, respawn_delay) VALUES (?,?,?,?)");
			statement.setInt(1, id);
			statement.setDouble(2, info.getDouble("current_hp"));
			statement.setDouble(3, info.getDouble("current_mp"));
			statement.setInt(4, info.getInteger("respawn_delay", 0));
			statement.execute();
			if(!save)
				if(raidboss != null)
				{
					final String msg = "Saved status: " + raidboss.getName() + " [" + id + "] " + TimeUtils.toSimpleFormat(info.getInteger("respawn_delay", 0) * 1000L);
					GmListTable.broadcastMessageToGMs(msg);
				}
				else
					RaidBossSpawnManager._log.info("RaidBossSpawnManager: Saved respawn time for raidboss " + id);
		}
		catch(SQLException e)
		{
			RaidBossSpawnManager._log.warn("RaidBossSpawnManager: Couldn't update raidboss_status table");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void addNewSpawn(final Spawn spawnDat)
	{
		if(spawnDat == null)
			return;
		final int bossId = spawnDat.getNpcId();
		if(RaidBossSpawnManager._spawntable.containsKey(bossId))
			return;
		RaidBossSpawnManager._spawntable.put(bossId, spawnDat);
		StatsSet info = RaidBossSpawnManager._storedInfo.get(bossId);
		if(info != null)
			spawnDat.setRespawnTime(info.getInteger("respawn_delay", 0));
		else
		{
			RaidBossSpawnManager._storedInfo.put(bossId, (info = new StatsSet()));
			info.set("current_hp", 0);
			info.set("current_mp", 0);
			info.set("respawn_delay", 0);
		}
	}

	public void onBossSpawned(final RaidBossInstance raidboss)
	{
		raidboss.setRaidStatus(Status.ALIVE);
		final int bossId = raidboss.getNpcId();
		if(!RaidBossSpawnManager._spawntable.containsKey(bossId))
			return;
		final StatsSet info = RaidBossSpawnManager._storedInfo.get(bossId);
		if(info != null && info.getDouble("current_hp") > 1.0)
		{
			raidboss.setCurrentHp(info.getDouble("current_hp"), true);
			raidboss.setCurrentMp(info.getDouble("current_mp"));
		}
		RaidBossSpawnManager._bosses.put(raidboss.getNpcId(), raidboss);
		if(!SpawnManager.retardation)
		{
			if(raidboss.getNpcId() == 25328)
				GmListTable.broadcastMessageToGMs("Spawning night RaidBoss " + raidboss.getName());
			else
				GmListTable.broadcastMessageToGMs("Spawning RaidBoss " + raidboss.getName());
			if(Config.ANNOUNCE_RB)
				Announcements.getInstance().announceByCustomMessage("l2s.BossAlive", new String[] { raidboss.getName() });
		}
	}

	public void onBossDespawned(final RaidBossInstance raidboss)
	{
		updateStatusDb(raidboss.getNpcId(), false);
	}

	public Status getRaidBossStatusId(final int bossId)
	{
		if(RaidBossSpawnManager._bosses.containsKey(bossId))
			return RaidBossSpawnManager._bosses.get(bossId).getRaidStatus();
		if(RaidBossSpawnManager._spawntable.containsKey(bossId))
			return Status.DEAD;
		return Status.UNDEFINED;
	}

	public boolean isDefined(final int bossId)
	{
		return RaidBossSpawnManager._spawntable.containsKey(bossId);
	}

	public Map<Integer, Spawn> getSpawnTable()
	{
		return RaidBossSpawnManager._spawntable;
	}

	private void restorePointsTable()
	{
		pointsLock.lock();
		RaidBossSpawnManager._points = new HashMap<Integer, Map<Integer, Integer>>();
		Connection con = null;
		Statement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			rset = statement.executeQuery("SELECT owner_id, boss_id, points FROM `raidboss_points` ORDER BY owner_id ASC");
			int currentOwner = 0;
			Map<Integer, Integer> score = null;
			while(rset.next())
			{
				if(currentOwner != rset.getInt("owner_id"))
				{
					currentOwner = rset.getInt("owner_id");
					score = new HashMap<Integer, Integer>();
					RaidBossSpawnManager._points.put(currentOwner, score);
				}
				assert score != null;
				final int bossId = rset.getInt("boss_id");
				if(bossId == RaidBossSpawnManager.KEY_RANK || bossId == RaidBossSpawnManager.KEY_TOTAL_POINTS || !RaidBossSpawnManager._pointsReward.containsKey(bossId))
					continue;
				score.put(bossId, rset.getInt("points"));
			}
		}
		catch(Exception e)
		{
			RaidBossSpawnManager._log.warn("RaidBossSpawnManager: Couldnt load raidboss points");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		pointsLock.unlock();
	}

	public void updatePointsDb()
	{
		pointsLock.lock();
		if(!mysql.set("TRUNCATE `raidboss_points`"))
			RaidBossSpawnManager._log.warn("RaidBossSpawnManager: Couldnt empty raidboss_points table");
		if(RaidBossSpawnManager._points.isEmpty())
		{
			pointsLock.unlock();
			return;
		}
		Connection con = null;
		Statement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.createStatement();
			final SqlBatch b = new SqlBatch("INSERT INTO `raidboss_points` (owner_id, boss_id, points) VALUES");
			for(final Map.Entry<Integer, Map<Integer, Integer>> pointEntry : RaidBossSpawnManager._points.entrySet())
			{
				final Map<Integer, Integer> tmpPoint = pointEntry.getValue();
				if(tmpPoint != null)
				{
					if(tmpPoint.isEmpty())
						continue;
					for(final Map.Entry<Integer, Integer> pointListEntry : tmpPoint.entrySet())
						if(!RaidBossSpawnManager.KEY_RANK.equals(pointListEntry.getKey()) && !RaidBossSpawnManager.KEY_TOTAL_POINTS.equals(pointListEntry.getKey()) && pointListEntry.getValue() != null)
						{
							if(pointListEntry.getValue() == 0)
								continue;
							final StringBuilder sb = new StringBuilder("(");
							sb.append(pointEntry.getKey()).append(",");
							sb.append(pointListEntry.getKey()).append(",");
							sb.append(pointListEntry.getValue()).append(")");
							b.write(sb.toString());
						}
				}
			}
			if(!b.isEmpty())
				statement.executeUpdate(b.close());
		}
		catch(SQLException e)
		{
			RaidBossSpawnManager._log.warn("RaidBossSpawnManager: Couldnt update raidboss_points table");
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		pointsLock.unlock();
	}

	public void addPoints(final int ownerId, final int bossId, final int points)
	{
		if(points <= 0 || ownerId <= 0 || bossId <= 0)
			return;
		pointsLock.lock();
		Map<Integer, Integer> pointsTable = RaidBossSpawnManager._points.get(ownerId);
		if(pointsTable == null)
		{
			pointsTable = new HashMap<Integer, Integer>();
			_points.put(ownerId, pointsTable);
		}
		if(pointsTable.isEmpty())
			pointsTable.put(bossId, points);
		else
		{
			final Integer currentPoins = pointsTable.get(bossId);
			pointsTable.put(bossId, (currentPoins == null ? points : currentPoins + points));
		}
		pointsLock.unlock();
	}

	public TreeMap<Integer, Integer> calculateRanking()
	{
		final TreeMap<Integer, Integer> tmpRanking = new TreeMap<Integer, Integer>();
		pointsLock.lock();
		for(final Map.Entry<Integer, Map<Integer, Integer>> point : RaidBossSpawnManager._points.entrySet())
		{
			final Map<Integer, Integer> tmpPoint = point.getValue();
			tmpPoint.remove(RaidBossSpawnManager.KEY_RANK);
			tmpPoint.remove(RaidBossSpawnManager.KEY_TOTAL_POINTS);
			int totalPoints = 0;
			for(final Map.Entry<Integer, Integer> e : tmpPoint.entrySet())
				totalPoints += e.getValue();
			if(totalPoints != 0)
			{
				tmpPoint.put(RaidBossSpawnManager.KEY_TOTAL_POINTS, totalPoints);
				tmpRanking.put(totalPoints, point.getKey());
			}
		}
		int ranking = 1;
		for(final Map.Entry<Integer, Integer> entry : tmpRanking.descendingMap().entrySet())
		{
			final Map<Integer, Integer> tmpPoint2 = RaidBossSpawnManager._points.get(entry.getValue());
			tmpPoint2.put(RaidBossSpawnManager.KEY_RANK, ranking);
			++ranking;
		}
		pointsLock.unlock();
		return tmpRanking;
	}

	public void distributeRewards()
	{
		pointsLock.lock();
		final TreeMap<Integer, Integer> ranking = calculateRanking();
		final Iterator<Integer> e = ranking.descendingMap().values().iterator();
		for(int counter = 1; e.hasNext() && counter <= 100; ++counter)
		{
			int reward = 0;
			final int playerId = e.next();
			if(counter == 1)
				reward = 2500;
			else if(counter == 2)
				reward = 1800;
			else if(counter == 3)
				reward = 1400;
			else if(counter == 4)
				reward = 1200;
			else if(counter == 5)
				reward = 900;
			else if(counter == 6)
				reward = 700;
			else if(counter == 7)
				reward = 600;
			else if(counter == 8)
				reward = 400;
			else if(counter == 9)
				reward = 300;
			else if(counter == 10)
				reward = 200;
			else if(counter <= 50)
				reward = 50;
			else if(counter <= 100)
				reward = 25;
			final Player player = GameObjectsStorage.getPlayer(playerId);
			Clan clan = null;
			if(player != null)
				clan = player.getClan();
			else
				clan = ClanTable.getInstance().getClan(mysql.simple_get_int("clanid", "characters", "obj_Id=" + playerId));
			if(clan != null)
				clan.incReputation(reward, true, "RaidPoints");
		}
		RaidBossSpawnManager._points.clear();
		updatePointsDb();
		pointsLock.unlock();
	}

	public Map<Integer, Map<Integer, Integer>> getPoints()
	{
		return RaidBossSpawnManager._points;
	}

	public Map<Integer, Integer> getPointsForOwnerId(final int ownerId)
	{
		return RaidBossSpawnManager._points.get(ownerId);
	}

	public int getPointsForRaid(final int raid)
	{
		final Integer ret = RaidBossSpawnManager._pointsReward.get(raid);
		return ret != null ? ret : 0;
	}

	static
	{
		RaidBossSpawnManager._log = LoggerFactory.getLogger(RaidBossSpawnManager.class);
		RaidBossSpawnManager._spawntable = new HashMap<Integer, Spawn>();
		RaidBossSpawnManager._bosses = new HashMap<Integer, RaidBossInstance>();
		RaidBossSpawnManager._pointsReward = new HashMap<Integer, Integer>();
		KEY_RANK = new Integer(-1);
		KEY_TOTAL_POINTS = new Integer(0);
	}

	public enum Status
	{
		ALIVE,
		DEAD,
		UNDEFINED;
	}
}
