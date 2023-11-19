package l2s.gameserver.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;

public class IpManager
{
	private static final Logger _log;

	public static void BanIp(final String ip, final String admin, final int time, final String comments)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			long expiretime = 0L;
			if(time != 0)
				expiretime = System.currentTimeMillis() / 1000L + time;
			con = DatabaseFactory.getInstanceLogin().getConnection();
			statement = con.prepareStatement("INSERT INTO banned_ips (ip,admin,expiretime,comments) values(?,?,?,?)");
			statement.setString(1, ip);
			statement.setString(2, admin);
			statement.setLong(3, expiretime);
			statement.setString(4, comments);
			statement.execute();
			IpManager._log.info("Banning ip: " + ip + " for " + time + " seconds.");
		}
		catch(Exception e)
		{
			IpManager._log.error("error4 while writing banned_ips", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static void UnbanIp(final String ip)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstanceLogin().getConnection();
			statement = con.prepareStatement("DELETE FROM banned_ips WHERE ip=?");
			statement.setString(1, ip);
			statement.execute();
			IpManager._log.info("Removed ban for ip: " + ip);
		}
		catch(Exception e)
		{
			IpManager._log.error("error5 while deleting from banned_ips", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public static boolean CheckIp(final String ip)
	{
		boolean result = false;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstanceLogin().getConnection();
			statement = con.prepareStatement("SELECT expiretime FROM banned_ips WHERE ip=?");
			statement.setString(1, ip);
			rset = statement.executeQuery();
			if(rset.next())
			{
				final long expiretime = rset.getLong("expiretime");
				if(expiretime != 0L && expiretime <= System.currentTimeMillis() / 1000L)
					UnbanIp(ip);
				else
					result = true;
			}
		}
		catch(Exception e)
		{
			IpManager._log.error("error6 while reading banned_ips", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	public static List<BannedIp> getBanList()
	{
		final List<BannedIp> result = new ArrayList<BannedIp>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstanceLogin().getConnection();
			statement = con.prepareStatement("SELECT ip,admin FROM banned_ips");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final BannedIp temp = new BannedIp();
				temp.ip = rset.getString("ip");
				temp.admin = rset.getString("admin");
				result.add(temp);
			}
		}
		catch(Exception e)
		{
			IpManager._log.error("error7 while reading banned_ips", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return result;
	}

	static
	{
		_log = LoggerFactory.getLogger(IpManager.class);
	}
}
