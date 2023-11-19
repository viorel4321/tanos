package l2s.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.PlayerManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.network.l2.components.CustomMessage;

public final class AutoBan
{
	private static Logger _log;

	public static boolean isBanned(final int ObjectId)
	{
		boolean res = false;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT MAX(endban) AS endban FROM bans WHERE obj_Id=? AND endban IS NOT NULL");
			statement.setInt(1, ObjectId);
			rset = statement.executeQuery();
			if(rset.next())
			{
				final Long endban = rset.getLong("endban") * 1000L;
				res = endban > System.currentTimeMillis();
			}
		}
		catch(Exception e)
		{
			AutoBan._log.warn("Could not restore ban data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return res;
	}

	public static void Banned(final Player actor, final int period, final String msg, final String GM)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			int endban = 0;
			if(period <= 0)
				endban = Integer.MAX_VALUE;
			else
			{
				final Calendar end = Calendar.getInstance();
				end.add(5, period);
				endban = (int) (end.getTimeInMillis() / 1000L);
			}
			final String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
			final String enddate = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date(endban * 1000L));
			if(endban * 1000L <= Calendar.getInstance().getTimeInMillis())
			{
				AutoBan._log.warn("Negative ban period | From " + date + " to " + enddate);
				return;
			}
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO bans (account_name, obj_id, baned, unban, reason, GM, endban) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, actor.getAccountName());
			statement.setInt(2, actor.getObjectId());
			statement.setString(3, date);
			statement.setString(4, enddate);
			statement.setString(5, msg);
			statement.setString(6, GM);
			statement.setLong(7, endban);
			statement.execute();
		}
		catch(Exception e)
		{
			AutoBan._log.warn("Could not store bans data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static boolean Banned(final String actor, final int acc_level, final int period, final String msg, final String GM)
	{
		final int obj_id = PlayerManager.getObjectIdByName(actor);
		boolean res = obj_id > 0;
		Connection con = null;
		PreparedStatement statement = null;
		if(res)
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE obj_Id=? LIMIT 1");
				statement.setInt(1, acc_level);
				statement.setInt(2, obj_id);
				statement.executeUpdate();
				DbUtils.close(statement);
				if(acc_level < 0)
				{
					int endban = 0;
					if(period <= 0)
						endban = Integer.MAX_VALUE;
					else
					{
						final Calendar end = Calendar.getInstance();
						end.add(5, period);
						endban = (int) (end.getTimeInMillis() / 1000L);
					}
					final String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
					final String enddate = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date(endban * 1000L));
					if(endban * 1000L <= Calendar.getInstance().getTimeInMillis())
					{
						AutoBan._log.warn("Negative ban period | From " + date + " to " + enddate);
						return false;
					}
					statement = con.prepareStatement("INSERT INTO bans (obj_id, baned, unban, reason, GM, endban) VALUES(?,?,?,?,?,?)");
					statement.setInt(1, obj_id);
					statement.setString(2, date);
					statement.setString(3, enddate);
					statement.setString(4, msg);
					statement.setString(5, GM);
					statement.setLong(6, endban);
					statement.execute();
				}
				else
				{
					statement = con.prepareStatement("DELETE FROM bans WHERE obj_id=?");
					statement.setInt(1, obj_id);
					statement.execute();
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
				AutoBan._log.warn("Could not store bans data: " + e);
				res = false;
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		return res;
	}

	public static void Karma(final Player actor, final int karma, String msg, final String GM)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			final String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
			msg = "Add karma(" + karma + ") " + msg;
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO bans (account_name, obj_id, baned, reason, GM) VALUES(?,?,?,?,?)");
			statement.setString(1, actor.getAccountName());
			statement.setInt(2, actor.getObjectId());
			statement.setString(3, date);
			statement.setString(4, msg);
			statement.setString(5, GM);
			statement.execute();
		}
		catch(Exception e)
		{
			AutoBan._log.warn("Could not store bans data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static boolean Karma(final String actor, final int karma, String msg, final String GM)
	{
		final int obj_id = PlayerManager.getObjectIdByName(actor);
		boolean res = obj_id > 0;
		Connection con = null;
		PreparedStatement statement = null;
		if(res)
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("update characters set karma=karma + ? where obj_Id=? LIMIT 1");
				statement.setInt(1, karma);
				statement.setInt(2, obj_id);
				statement.execute();
				DbUtils.close(statement);
				final String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
				msg = "Add karma(" + karma + ") " + msg;
				statement = con.prepareStatement("INSERT INTO bans (obj_id, baned, reason, GM) VALUES(?,?,?,?)");
				statement.setInt(1, obj_id);
				statement.setString(2, date);
				statement.setString(3, msg);
				statement.setString(4, GM);
				statement.execute();
			}
			catch(Exception e)
			{
				AutoBan._log.warn("Could not store bans data: " + e);
				res = false;
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		return res;
	}

	public static void Banned(final Player actor, final int period, final String msg)
	{
		Banned(actor, period, msg, "AutoBan");
	}

	public static boolean ChatBan(final String actor, final int period, final String msg, final String GM)
	{
		boolean res = true;
		final long NoChannel = period * 60000;
		final int obj_id = PlayerManager.getObjectIdByName(actor);
		if(obj_id == 0)
			return false;
		final Player plyr = World.getPlayer(actor);
		Connection con = null;
		PreparedStatement statement = null;
		if(plyr != null)
		{
			plyr.sendMessage(new CustomMessage("l2s.gameserver.utils.AutoBan.ChatBan").addString(GM).addNumber(period));
			plyr.updateNoChannel(NoChannel);
		}
		else
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=? LIMIT 1");
				statement.setLong(1, NoChannel > 0L ? NoChannel / 1000L : NoChannel);
				statement.setInt(2, obj_id);
				statement.executeUpdate();
			}
			catch(Exception e)
			{
				res = false;
				AutoBan._log.warn("Could not activate nochannel: " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		return res;
	}

	public static boolean ChatUnBan(final String actor, final String GM)
	{
		boolean res = true;
		final Player plyr = World.getPlayer(actor);
		final int obj_id = PlayerManager.getObjectIdByName(actor);
		if(obj_id == 0)
			return false;
		Connection con = null;
		PreparedStatement statement = null;
		if(plyr != null)
		{
			plyr.sendMessage(new CustomMessage("l2s.gameserver.utils.AutoBan.ChatUnBan").addString(GM));
			plyr.updateNoChannel(0L);
		}
		else
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET nochannel = ? WHERE obj_Id=? LIMIT 1");
				statement.setLong(1, 0L);
				statement.setInt(2, obj_id);
				statement.executeUpdate();
			}
			catch(Exception e)
			{
				res = false;
				AutoBan._log.warn("Could not activate nochannel: " + e);
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		return res;
	}

	public static boolean addHwidBan(final String name, final String hwid, final String reason, final long time, final String gm)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO hwid_bans (player,HWID,reason,GM,end_date) values (?,?,?,?,?)");
			statement.setString(1, name);
			statement.setString(2, hwid);
			statement.setString(3, reason);
			statement.setString(4, gm);
			statement.setLong(5, time);
			statement.executeUpdate();
		}
		catch(Exception e)
		{
			return false;
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		return true;
	}

	static
	{
		AutoBan._log = LoggerFactory.getLogger(AutoBan.class);
	}
}
