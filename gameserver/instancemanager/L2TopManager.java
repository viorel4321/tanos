package l2s.gameserver.instancemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.concurrent.ScheduledFuture;

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

public class L2TopManager
{
	private static Logger _log;
	private static final String SELECT_PLAYER_OBJID = "SELECT obj_Id,account_name,last_hwid FROM characters WHERE char_name=?";
	private static final String SELECT_CHARACTER_L2TOP_DATA = "SELECT * FROM character_l2top_votes WHERE id=? AND date=? AND multipler=?";
	private static final String SELECT_CHARACTER_L2TOP_HWID = "SELECT * FROM character_l2top_votes WHERE HWID=? AND fixDate=? AND sms=0";
	private static final String SELECT_CHARACTER_L2TOP_ACC = "SELECT * FROM character_l2top_votes WHERE account=? AND fixDate=? AND sms=0";
	private static final String INSERT_L2TOP_DATA = "INSERT INTO character_l2top_votes (date, id, nick, multipler, sms, fixDate, account, HWID) values (?,?,?,?,?,?,?,?)";
	private static final String DELETE_L2TOP_DATA = "DELETE FROM character_l2top_votes WHERE date<?";
	private static final String SELECT_MULTIPLER_L2TOP_DATA = "SELECT id,multipler FROM character_l2top_votes WHERE has_reward=0";
	private static final String UPDATE_L2TOP_DATA = "UPDATE character_l2top_votes SET has_reward=1 WHERE has_reward=0 AND id=?";
	private static final String voteWeb;
	private static final String voteSms;
	private static L2TopManager _instance;
	public static boolean started;
	private ScheduledFuture<?> uTask;
	private ScheduledFuture<?> cTask;
	private HashMap<Integer, Integer> rewards;

	public static L2TopManager getInstance()
	{
		if(L2TopManager._instance == null && Config.L2TopManagerEnabled)
			L2TopManager._instance = new L2TopManager();
		return L2TopManager._instance;
	}

	public L2TopManager()
	{
		rewards = new HashMap<Integer, Integer>();
		uTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ConnectAndUpdate(), Config.L2TopManagerInterval, Config.L2TopManagerInterval);
		cTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Clean(), 120000L, 3600000L);
		L2TopManager.started = true;
		L2TopManager._log.info("L2TopManager: Loaded sucesfully.");
	}

	public void stop()
	{
		if(uTask != null)
		{
			uTask.cancel(false);
			uTask = null;
		}
		if(cTask != null)
		{
			cTask.cancel(false);
			cTask = null;
		}
		L2TopManager.started = false;
		rewards = null;
		L2TopManager._instance = null;
	}

	private boolean update()
	{
		final String out_sms = getPage(Config.L2TopSmsAddress);
		final String out_web = getPage(Config.L2TopWebAddress);
		final File sms = new File(L2TopManager.voteSms);
		final File web = new File(L2TopManager.voteWeb);
		FileWriter SaveWeb = null;
		FileWriter SaveSms = null;
		try
		{
			SaveSms = new FileWriter(sms);
			SaveSms.write(out_sms);
			SaveWeb = new FileWriter(web);
			SaveWeb.write(out_web);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		finally
		{
			try
			{
				if(SaveSms != null)
					SaveSms.close();
				if(SaveWeb != null)
					SaveWeb.close();
			}
			catch(Exception e2)
			{
				e2.printStackTrace();
			}
		}
		return true;
	}

	private static String getPage(final String address)
	{
		final StringBuffer buf = new StringBuffer();
		try
		{
			final Socket s = new Socket("l2top.ru", 80);
			s.setSoTimeout(30000);
			final String request = "GET " + address + " HTTP/1.1\r\nUser-Agent: http:\\" + Config.EXTERNAL_HOSTNAME + " server\r\nHost: http:\\" + Config.EXTERNAL_HOSTNAME + " \r\nAccept: */*\r\nConnection: close\r\n\r\n";
			s.getOutputStream().write(request.getBytes());
			final BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), "Cp1251"));
			for(String line = in.readLine(); line != null; line = in.readLine())
			{
				buf.append(line);
				buf.append("\r\n");
			}
			s.close();
		}
		catch(Exception e)
		{
			buf.append("Connection error");
		}
		return buf.toString();
	}

	private void parse(final boolean sms)
	{
		try
		{
			final BufferedReader in = new BufferedReader(new FileReader(sms ? L2TopManager.voteSms : L2TopManager.voteWeb));
			String line = in.readLine();
			while(line != null)
			{
				final Calendar cal = Calendar.getInstance();
				if(line.startsWith(String.valueOf(cal.get(1))))
					try
					{
						final StringTokenizer st = new StringTokenizer(line, "\t -:");
						if(Config.L2TopPrefix.equals("") && (sms && st.countTokens() > 8 || !sms && st.countTokens() > 7))
						{
							line = in.readLine();
							continue;
						}
						final String Y = st.nextToken();
						final String M = st.nextToken();
						final String D = st.nextToken();
						cal.set(1, Integer.parseInt(Y));
						cal.set(2, Integer.parseInt(M) - 1);
						cal.set(5, Integer.parseInt(D));
						cal.set(11, Integer.parseInt(st.nextToken()));
						cal.set(12, Integer.parseInt(st.nextToken()));
						cal.set(13, Integer.parseInt(st.nextToken()));
						cal.set(14, 0);
						String nick = st.nextToken();
						if(!Config.L2TopPrefix.equals(""))
						{
							if(!nick.equals(Config.L2TopPrefix))
							{
								line = in.readLine();
								continue;
							}
							nick = st.nextToken();
						}
						int mult = 1;
						if(sms)
							mult = Integer.parseInt(new StringBuffer(st.nextToken()).delete(0, 1).toString());
						if(cal.getTimeInMillis() + Config.L2TopSaveDays * 86400000L > System.currentTimeMillis())
							checkAndSave(cal.getTimeInMillis(), nick, mult, sms ? 1 : 0, D + "-" + M + "-" + Y);
					}
					catch(NoSuchElementException nsee)
					{
						line = in.readLine();
						continue;
					}
				line = in.readLine();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private synchronized void clean()
	{
		final Calendar cal = Calendar.getInstance();
		cal.add(6, -Config.L2TopSaveDays);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM character_l2top_votes WHERE date<?");
			statement.setLong(1, cal.getTimeInMillis());
			statement.execute();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private synchronized void checkAndSave(final long date, final String nick, final int mult, final int sms, final String fixDate)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT obj_Id,account_name,last_hwid FROM characters WHERE char_name=?");
			statement.setString(1, nick);
			rset = statement.executeQuery();
			int objId = 0;
			String account = "";
			String hwid = "";
			if(rset.next())
			{
				objId = rset.getInt("obj_Id");
				account = rset.getString("account_name");
				hwid = rset.getString("last_hwid");
			}
			if(objId > 0)
			{
				boolean ok = true;
				DbUtils.closeQuietly(statement, rset);
				if(sms == 1 || !Config.L2TopHWID && !Config.L2TopAccount)
				{
					statement = con.prepareStatement("SELECT * FROM character_l2top_votes WHERE id=? AND date=? AND multipler=?");
					statement.setInt(1, objId);
					statement.setLong(2, date);
					statement.setInt(3, mult);
					rset = statement.executeQuery();
					ok = !rset.next();
				}
				else
				{
					if(Config.L2TopHWID)
					{
						statement = con.prepareStatement("SELECT * FROM character_l2top_votes WHERE HWID=? AND fixDate=? AND sms=0");
						statement.setString(1, hwid);
						statement.setString(2, fixDate);
						rset = statement.executeQuery();
						ok = !rset.next();
					}
					if(ok && Config.L2TopAccount)
					{
						if(Config.L2TopHWID)
							DbUtils.closeQuietly(statement, rset);
						statement = con.prepareStatement("SELECT * FROM character_l2top_votes WHERE account=? AND fixDate=? AND sms=0");
						statement.setString(1, account);
						statement.setString(2, fixDate);
						rset = statement.executeQuery();
						ok = !rset.next();
					}
				}
				if(ok)
				{
					DbUtils.closeQuietly(statement);
					statement = con.prepareStatement("INSERT INTO character_l2top_votes (date, id, nick, multipler, sms, fixDate, account, HWID) values (?,?,?,?,?,?,?,?)");
					statement.setLong(1, date);
					statement.setInt(2, objId);
					statement.setString(3, nick);
					statement.setInt(4, mult);
					statement.setInt(5, sms);
					statement.setString(6, fixDate);
					statement.setString(7, account);
					statement.setString(8, hwid);
					statement.execute();
				}
			}
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
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
			statement = con.prepareStatement("SELECT id,multipler FROM character_l2top_votes WHERE has_reward=0");
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
					statement = con.prepareStatement("UPDATE character_l2top_votes SET has_reward=1 WHERE has_reward=0 AND id=?");
					statement.setInt(1, id);
					statement.executeUpdate();
					DbUtils.closeQuietly(statement);
					mult = rewards.get(id);
					final Player player = GameObjectsStorage.getPlayer(id);
					if(player != null)
						if(player.isLangRus())
							player.sendMessage("\u0410\u0434\u043c\u0438\u043d\u0438\u0441\u0442\u0440\u0430\u0446\u0438\u044f " + Config.L2TopServerAddress + " \u0431\u043b\u0430\u0433\u043e\u0434\u0430\u0440\u0438\u0442 \u0412\u0430\u0441 \u0437\u0430 \u0433\u043e\u043b\u043e\u0441\u043e\u0432\u0430\u043d\u0438\u0435.");
						else
							player.sendMessage("Administration " + Config.L2TopServerAddress + " thank you for your vote.");
					for(int n = 0; n < mult; ++n)
						for(int i = 0; i < Config.L2TopReward.length; i += 4)
							if(Rnd.chance(Config.L2TopReward[i + 3]))
								if(player != null)
									player.getInventory().addItem(Config.L2TopReward[i], Rnd.get(Config.L2TopReward[i + 1], Config.L2TopReward[i + 2]));
								else
									Util.giveItem(id, Config.L2TopReward[i], Rnd.get(Config.L2TopReward[i + 1], Config.L2TopReward[i + 2]));
				}
				rewards.clear();
			}
		}
		catch(SQLException e)
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
		L2TopManager._log = LoggerFactory.getLogger(L2TopManager.class);
		voteWeb = Config.DATAPACK_ROOT + "/data/l2top_vote-web.txt";
		voteSms = Config.DATAPACK_ROOT + "/data/l2top_vote-sms.txt";
	}

	private class ConnectAndUpdate implements Runnable
	{
		@Override
		public void run()
		{
			if(update())
			{
				parse(true);
				parse(false);
				giveReward();
			}
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
}
