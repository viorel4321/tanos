package l2s.gameserver.taskmanager.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;

public class RestoreOfflineTraders implements Runnable
{
	private static final Logger _log;

	@Override
	public void run()
	{
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L)
			{
				final int expireTimeSecs = (int) (System.currentTimeMillis() / 1000L - Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK);
				statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND value < ?");
				statement.setLong(1, expireTimeSecs);
				statement.executeUpdate();
				DbUtils.close(statement);
			}
			statement = con.prepareStatement("DELETE FROM character_variables WHERE name = 'offline' AND obj_id IN (SELECT obj_id FROM characters WHERE accessLevel < 0)");
			statement.executeUpdate();
			DbUtils.close(statement);
			statement = con.prepareStatement("SELECT obj_id, value FROM character_variables WHERE name = 'offline'");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int objectId = rset.getInt("obj_id");
				final int expireTimeSecs2 = rset.getInt("value");
				final Player p = Player.restore(objectId);
				if(p == null)
					continue;
				if(p.isDead())
					p.kick(false);
				else
				{
					if(Config.SERVICES_ALLOW_OFFLINE_TRADE_NAME_COLOR)
						p.setNameColor(Config.SERVICES_OFFLINE_TRADE_NAME_COLOR, false);
					p.setOfflineMode(true);
					p.setOnlineStatus(true);
					p.spawnMe();
					if(p.getClan() != null && p.getClan().getClanMember(Integer.valueOf(p.getObjectId())) != null)
						p.getClan().getClanMember(Integer.valueOf(p.getObjectId())).setPlayerInstance(p);
					if(Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK > 0L)
						p.startKickTask((Config.SERVICES_OFFLINE_TRADE_SECONDS_TO_KICK + expireTimeSecs2 - System.currentTimeMillis() / 1000L) * 1000L, false);
					if(Config.SERVICES_TRADE_ONLY_FAR)
						for(final Player player : World.getAroundPlayers(p, Config.SERVICES_TRADE_RADIUS, 200))
							if(player.isInStoreMode())
								if(player.isInOfflineMode())
								{
									player.setOfflineMode(false);
									player.kick(false);
									RestoreOfflineTraders._log.warn("Offline trader: " + player + " kicked.");
								}
								else
								{
									player.setPrivateStoreType((short) 0);
									player.standUp();
									player.broadcastUserInfo(false);
								}
					++count;
				}
			}
		}
		catch(Exception e)
		{
			RestoreOfflineTraders._log.warn("Error while restoring offline traders! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if(count > 0)
			RestoreOfflineTraders._log.info("Restored " + count + " offline traders");
	}

	static
	{
		_log = LoggerFactory.getLogger(RestoreOfflineTraders.class);
	}
}
