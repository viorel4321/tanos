package l2s.gameserver.instancemanager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.commons.util.Rnd;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Util;

public class MMOTopManager
{
	private static Logger _log;
	private static final String SELECT_PLAYER_OBJID = "SELECT obj_Id FROM characters WHERE char_name=?";
	private static final String SELECT_CHARACTER_MMOTOP_DATA = "SELECT * FROM character_mmotop_votes WHERE id=? AND date=? AND multipler=?";
	private static final String INSERT_MMOTOP_DATA = "INSERT INTO character_mmotop_votes (date, id, nick, multipler) values (?,?,?,?)";
	private static final String DELETE_MMOTOP_DATA = "DELETE FROM character_mmotop_votes WHERE date<?";
	private static final String SELECT_MULTIPLER_MMOTOP_DATA = "SELECT id,multipler FROM character_mmotop_votes WHERE has_reward=0";
	private static final String UPDATE_MMOTOP_DATA = "UPDATE character_mmotop_votes SET has_reward=1 WHERE has_reward=0 AND id=?";
	private BufferedReader reader;
	private HashMap<Integer, Integer> rewards;
	private static MMOTopManager _instance;

	public static MMOTopManager getInstance()
	{
		if(MMOTopManager._instance == null && Config.MMO_TOP_MANAGER_ENABLED)
			MMOTopManager._instance = new MMOTopManager();
		return MMOTopManager._instance;
	}

	public MMOTopManager()
	{
		rewards = new HashMap<Integer, Integer>();
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new ConnectAndUpdate(), Config.MMO_TOP_MANAGER_INTERVAL, Config.MMO_TOP_MANAGER_INTERVAL);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Clean(), 120000L, 3600000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new GiveReward(), Config.MMO_TOP_MANAGER_INTERVAL, Config.MMO_TOP_MANAGER_INTERVAL);
		MMOTopManager._log.info("MMOTopManager: Loaded sucesfully.");
	}

	public void getPage(final String address)
	{
		try
		{
			final URL url = new URL(address);
			reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF8"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public void parse()
	{
		try
		{
			String line;
			while((line = reader.readLine()) != null)
			{
				final StringTokenizer st = new StringTokenizer(line, "\t. :");
				while(st.hasMoreTokens())
					try
					{
						st.nextToken();
						final int day = Integer.parseInt(st.nextToken());
						final int month = Integer.parseInt(st.nextToken()) - 1;
						final int year = Integer.parseInt(st.nextToken());
						final int hour = Integer.parseInt(st.nextToken());
						final int minute = Integer.parseInt(st.nextToken());
						final int second = Integer.parseInt(st.nextToken());
						st.nextToken();
						st.nextToken();
						st.nextToken();
						st.nextToken();
						final String charName = st.nextToken();
						final int voteType = Integer.parseInt(st.nextToken());
						final Calendar calendar = Calendar.getInstance();
						calendar.set(1, year);
						calendar.set(2, month);
						calendar.set(5, day);
						calendar.set(11, hour);
						calendar.set(12, minute);
						calendar.set(13, second);
						calendar.set(14, 0);
						final long voteTime = calendar.getTimeInMillis() / 1000L;
						if(voteTime + Config.MMO_TOP_SAVE_DAYS * 86400 <= System.currentTimeMillis() / 1000L)
							continue;
						checkAndSave(voteTime, charName, voteType);
					}
					catch(Exception ex)
					{}
			}
		}
		catch(Exception e)
		{
			MMOTopManager._log.warn("MMOTopManager: Cant store MMOTop data.");
			e.printStackTrace();
		}
	}

	public void checkAndSave(final long voteTime, final String charName, final int voteType)
	{
		Connection con = null;
		PreparedStatement selectObjectStatement = null;
		PreparedStatement selectMmotopStatement = null;
		PreparedStatement insertStatement = null;
		ResultSet rsetObject = null;
		ResultSet rsetMmotop = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			selectObjectStatement = con.prepareStatement("SELECT obj_Id FROM characters WHERE char_name=?");
			selectObjectStatement.setString(1, charName);
			rsetObject = selectObjectStatement.executeQuery();
			int objId = 0;
			if(rsetObject.next())
				objId = rsetObject.getInt("obj_Id");
			if(objId > 0)
			{
				selectMmotopStatement = con.prepareStatement("SELECT * FROM character_mmotop_votes WHERE id=? AND date=? AND multipler=?");
				selectMmotopStatement.setInt(1, objId);
				selectMmotopStatement.setLong(2, voteTime);
				selectMmotopStatement.setInt(3, voteType);
				rsetMmotop = selectMmotopStatement.executeQuery();
				if(!rsetMmotop.next())
				{
					insertStatement = con.prepareStatement("INSERT INTO character_mmotop_votes (date, id, nick, multipler) values (?,?,?,?)");
					insertStatement.setLong(1, voteTime);
					insertStatement.setInt(2, objId);
					insertStatement.setString(3, charName);
					insertStatement.setInt(4, voteType);
					insertStatement.execute();
					insertStatement.close();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, selectObjectStatement, rsetObject);
			DbUtils.closeQuietly(selectMmotopStatement, rsetMmotop);
			DbUtils.closeQuietly(insertStatement);
		}
	}

	private synchronized void clean()
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.add(6, -Config.MMO_TOP_SAVE_DAYS);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_mmotop_votes WHERE date<?");
			statement.setLong(1, calendar.getTimeInMillis() / 1000L);
			statement.execute();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private synchronized void giveReward()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			int objId = 0;
			int mult = 0;
			if(!rewards.isEmpty())
				rewards.clear();
			statement = con.prepareStatement("SELECT id,multipler FROM character_mmotop_votes WHERE has_reward=0");
			rset = statement.executeQuery();
			while(rset.next())
			{
				objId = rset.getInt("id");
				mult = rset.getInt("multipler");
				if(!rewards.containsKey(objId))
					rewards.put(objId, mult);
				else
				{
					mult += rewards.get(objId);
					rewards.remove(objId);
					rewards.put(objId, mult);
				}
			}
			if(!rewards.isEmpty())
			{
				DbUtils.closeQuietly(statement, rset);
				for(final int id : rewards.keySet())
				{
					statement = con.prepareStatement("UPDATE character_mmotop_votes SET has_reward=1 WHERE has_reward=0 AND id=?");
					statement.setInt(1, id);
					statement.executeUpdate();
					DbUtils.closeQuietly(statement);
					mult = rewards.get(id);
					final Player player = GameObjectsStorage.getPlayer(id);
					for(int n = 0; n < mult; ++n)
						for(int i = 0; i < Config.MMO_TOP_REWARD.length; i += 4)
							if(Rnd.chance(Config.MMO_TOP_REWARD[i + 3]))
								if(player != null)
									player.getInventory().addItem(Config.MMO_TOP_REWARD[i], Rnd.get(Config.MMO_TOP_REWARD[i + 1], Config.MMO_TOP_REWARD[i + 2]));
								else
									Util.giveItem(id, Config.MMO_TOP_REWARD[i], Rnd.get(Config.MMO_TOP_REWARD[i + 1], Config.MMO_TOP_REWARD[i + 2]));
				}
				rewards.clear();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	static
	{
		MMOTopManager._log = LoggerFactory.getLogger(MMOTopManager.class);
	}

	private class ConnectAndUpdate implements Runnable
	{
		@Override
		public void run()
		{
			getPage(Config.MMO_TOP_WEB_ADDRESS);
			parse();
		}
	}

	private class Clean implements Runnable
	{
		@Override
		public void run()
		{
			clean();
		}
	}

	private class GiveReward implements Runnable
	{
		@Override
		public void run()
		{
			giveReward();
		}
	}
}
