package l2s.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.entity.residence.ClanHall;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.tables.ClanTable;

public class ClanDataDAO
{
	private static final Logger _log;
	private static final ClanDataDAO _instance;
	public static final String SELECT_CASTLE_OWNER = "SELECT clan_id FROM clan_data WHERE hasCastle = ? LIMIT 1";
	public static final String SELECT_CLANHALL_OWNER = "SELECT clan_id FROM clan_data WHERE hasHideout = ? LIMIT 1";

	public static ClanDataDAO getInstance()
	{
		return ClanDataDAO._instance;
	}

	public Clan getOwner(final Castle c)
	{
		return this.getOwner(c, "SELECT clan_id FROM clan_data WHERE hasCastle = ? LIMIT 1");
	}

	public Clan getOwner(final ClanHall c)
	{
		return this.getOwner(c, "SELECT clan_id FROM clan_data WHERE hasHideout = ? LIMIT 1");
	}

	private Clan getOwner(final Residence residence, final String sql)
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement(sql);
			statement.setInt(1, residence.getId());
			rset = statement.executeQuery();
			if(rset.next())
				return ClanTable.getInstance().getClan(rset.getInt("clan_id"));
		}
		catch(Exception e)
		{
			ClanDataDAO._log.error("ClanDataDAO.getOwner(Residence, String)", e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return null;
	}

	static
	{
		_log = LoggerFactory.getLogger(ClanDataDAO.class);
		_instance = new ClanDataDAO();
	}
}
