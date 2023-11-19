package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.TeleportLoc;

public class TeleportLocTable
{
	private static final Logger _log;
	private static TeleportLocTable _instance;
	private Map<Integer, TeleportLoc> _teleports;

	public static TeleportLocTable getInstance()
	{
		if(TeleportLocTable._instance == null)
			TeleportLocTable._instance = new TeleportLocTable();
		return TeleportLocTable._instance;
	}

	private TeleportLocTable()
	{
		reloadAll();
	}

	public void reloadAll()
	{
		_teleports = new HashMap<Integer, TeleportLoc>();
		Connection con = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection(con);
			final PreparedStatement statement = con.prepareStatement("SELECT Description, id, loc_x, loc_y, loc_z, price, fornoble FROM teleport");
			final ResultSet rset = statement.executeQuery();
			while(rset.next())
			{
				final TeleportLoc teleport = new TeleportLoc();
				teleport.setTeleId(rset.getInt("id"));
				teleport.setLocX(rset.getInt("loc_x"));
				teleport.setLocY(rset.getInt("loc_y"));
				teleport.setLocZ(rset.getInt("loc_z"));
				teleport.setPrice(rset.getInt("price"));
				teleport.setIsForNoble(rset.getInt("fornoble") == 1);
				_teleports.put(teleport.getTeleId(), teleport);
			}
			rset.close();
			statement.close();
			TeleportLocTable._log.info("TeleportTable: Loaded " + _teleports.size() + " location");
		}
		catch(Exception e)
		{
			TeleportLocTable._log.warn("error while creating teleport table: " + e);
		}
	}

	public TeleportLoc getTemplate(final int id)
	{
		return _teleports.get(id);
	}

	static
	{
		_log = LoggerFactory.getLogger(TeleportLocTable.class);
	}
}
