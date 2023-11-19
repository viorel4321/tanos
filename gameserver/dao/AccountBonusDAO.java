package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;

public class AccountBonusDAO
{
	private static final Logger _log;
	private static final AccountBonusDAO _instance;
	public static final String SELECT_SQL_QUERY = "SELECT bonus, bonus_expire FROM account_bonus WHERE account=?";
	public static final String DELETE_SQL_QUERY = "DELETE FROM account_bonus WHERE account=?";
	public static final String INSERT_SQL_QUERY = "REPLACE INTO account_bonus(account, bonus, bonus_expire) VALUES (?,?,?)";

	public static AccountBonusDAO getInstance()
	{
		return AccountBonusDAO._instance;
	}

	public double[] select(final String account)
	{
		double bonus = 1.0;
		int time = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT bonus, bonus_expire FROM account_bonus WHERE account=?");
			statement.setString(1, account);
			rset = statement.executeQuery();
			if(rset.next())
			{
				bonus = rset.getDouble("bonus");
				time = rset.getInt("bonus_expire");
			}
		}
		catch(Exception e)
		{
			AccountBonusDAO._log.error("AccountBonusDAO.select(String): ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return new double[] { bonus, time };
	}

	public void delete(final String account)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM account_bonus WHERE account=?");
			statement.setString(1, account);
			statement.execute();
		}
		catch(Exception e)
		{
			AccountBonusDAO._log.error("AccountBonusDAO.delete(String): ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void insert(final String account, final double bonus, final int endTime)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO account_bonus(account, bonus, bonus_expire) VALUES (?,?,?)");
			statement.setString(1, account);
			statement.setDouble(2, bonus);
			statement.setInt(3, endTime);
			statement.execute();
		}
		catch(Exception e)
		{
			AccountBonusDAO._log.error("AccountBonusDAO.insert(String, double, int): ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(AccountBonusDAO.class);
		_instance = new AccountBonusDAO();
	}
}
