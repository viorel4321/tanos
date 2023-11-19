package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Residence;

public class SiegePlayerDAO
{
	private static final Logger _log;
	private static final SiegePlayerDAO _instance;
	public static final String INSERT_SQL_QUERY = "INSERT INTO siege_players(residence_id, object_id, clan_id) VALUES (?,?,?)";
	public static final String DELETE_SQL_QUERY = "DELETE FROM siege_players WHERE residence_id=? AND object_id=? AND clan_id=?";
	public static final String DELETE_SQL_QUERY2 = "DELETE FROM siege_players WHERE residence_id=?";
	public static final String SELECT_SQL_QUERY = "SELECT object_id FROM siege_players WHERE residence_id=? AND clan_id=?";

	public static SiegePlayerDAO getInstance()
	{
		return SiegePlayerDAO._instance;
	}

	public List<Integer> select(final Residence residence, final int clanId)
	{
		final List<Integer> set = new ArrayList<Integer>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT object_id FROM siege_players WHERE residence_id=? AND clan_id=?");
			statement.setInt(1, residence.getId());
			statement.setInt(2, clanId);
			rset = statement.executeQuery();
			while(rset.next())
				set.add(rset.getInt("object_id"));
		}
		catch(Exception e)
		{
			SiegePlayerDAO._log.error("SiegePlayerDAO.select(Residence, int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return set;
	}

	public void insert(final Residence residence, final int clanId, final int playerId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO siege_players(residence_id, object_id, clan_id) VALUES (?,?,?)");
			statement.setInt(1, residence.getId());
			statement.setInt(2, playerId);
			statement.setInt(3, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			SiegePlayerDAO._log.error("SiegePlayerDAO.insert(Residence, int, int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(final Residence residence, final int clanId, final int playerId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM siege_players WHERE residence_id=? AND object_id=? AND clan_id=?");
			statement.setInt(1, residence.getId());
			statement.setInt(2, playerId);
			statement.setInt(3, clanId);
			statement.execute();
		}
		catch(Exception e)
		{
			SiegePlayerDAO._log.error("SiegePlayerDAO.delete(Residence, int, int): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void delete(final Residence residence)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM siege_players WHERE residence_id=?");
			statement.setInt(1, residence.getId());
			statement.execute();
		}
		catch(Exception e)
		{
			SiegePlayerDAO._log.error("SiegePlayerDAO.delete(Residence): " + e, e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	static
	{
		_log = LoggerFactory.getLogger(SiegePlayerDAO.class);
		_instance = new SiegePlayerDAO();
	}
}
