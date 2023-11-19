package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.instancemanager.TownManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.Zone;
import l2s.gameserver.model.base.Race;
import l2s.gameserver.model.entity.Town;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.scripts.Scripts;
import l2s.gameserver.utils.Location;

public class MapRegionTable
{
	private static final Logger _log;
	private static MapRegionTable _instance;
	private final int[][] _regions;

	public static MapRegionTable getInstance()
	{
		if(MapRegionTable._instance == null)
			MapRegionTable._instance = new MapRegionTable();
		return MapRegionTable._instance;
	}

	private MapRegionTable()
	{
		_regions = new int[World.WORLD_SIZE_X][World.WORLD_SIZE_Y];
		int count = 0;
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM mapregion");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int y = rset.getInt("y10_plus");
				for(int i = Config.GEO_X_FIRST; i <= Config.GEO_X_LAST; ++i)
				{
					final int region = rset.getInt("x" + i);
					_regions[i - Config.GEO_X_FIRST][y] = region;
					++count;
				}
			}
			MapRegionTable._log.info("Loaded " + count + " mapregions.");
		}
		catch(Exception e)
		{
			MapRegionTable._log.warn("error while creating map region data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public final int getMapRegion(final int posX, final int posY)
	{
		final int tileX = posX - World.MAP_MIN_X >> 15;
		final int tileY = posY - World.MAP_MIN_Y >> 15;
		return _regions[tileX][tileY];
	}

	public static Location getTeleToClosestTown(final Creature activeChar)
	{
		return getInstance().getTeleToLocation(activeChar, TeleportWhereType.ClosestTown);
	}

	public static Location getTeleTo(final Creature activeChar, final TeleportWhereType teleportWhere)
	{
		return getInstance().getTeleToLocation(activeChar, teleportWhere);
	}

	public Location getTeleToLocation(final Creature activeChar, final TeleportWhereType teleportWhere)
	{
		final Player player = activeChar.getPlayer();
		if(player == null)
			return TownManager.getInstance().getClosestTown(activeChar).getSpawn();
		final Object[] script_args = { player };
		for(final Scripts.ScriptClassAndMethod handler : Scripts.onEscape)
			Scripts.getInstance().callScripts(activeChar, handler.className, handler.methodName, script_args);
		final Clan clan = player.getClan();
		if(clan != null)
		{
			if(teleportWhere == TeleportWhereType.ClanHall && clan.getHasHideout() != 0)
				return ResidenceHolder.getInstance().getResidence(clan.getHasHideout()).getOwnerRestartPoint();
			if(teleportWhere == TeleportWhereType.Castle && clan.getHasCastle() != 0)
				return ResidenceHolder.getInstance().getResidence(clan.getHasCastle()).getOwnerRestartPoint();
			final SiegeEvent<?, ?> se = player.getEvent(SiegeEvent.class);
			if(se != null && se.isInProgress())
			{
				if(teleportWhere == TeleportWhereType.Castle && se.getSiegeClan("defenders", clan) != null && se.getResidence() != null && ((Residence) se.getResidence()).getZone() != null)
					return player.getKarma() > 1 ? ((Residence) se.getResidence()).getZone().getPKSpawn() : ((Residence) se.getResidence()).getZone().getSpawn();
				return player.getKarma() > 1 ? TownManager.getInstance().getClosestTown(activeChar).getPKSpawn() : TownManager.getInstance().getClosestTown(activeChar).getSpawn();
			}
		}
		final Town cn = Config.TO_TOWN > 0 && (!Config.NO_TO_TOWN_PK || player.getKarma() < 1) ? TownManager.getInstance().getTown(Config.TO_TOWN) : TownManager.getInstance().getClosestTown(activeChar);
		if(Config.TO_TOWN <= 0)
		{
			if(Config.TO_TOWN <= 0 && player.getRace() == Race.elf && cn.getTownId() == 3)
				return player.getKarma() > 1 ? TownManager.getInstance().getTown(2).getPKSpawn() : TownManager.getInstance().getTown(2).getSpawn();
			if(player.getRace() == Race.darkelf && cn.getTownId() == 2)
				return player.getKarma() > 1 ? TownManager.getInstance().getTown(3).getPKSpawn() : TownManager.getInstance().getTown(3).getSpawn();
		}
		final Zone battle = activeChar.getZone(Zone.ZoneType.battle_zone);
		if(battle != null && battle.getRestartPoints() != null)
			return player.getKarma() > 1 ? battle.getPKSpawn() : battle.getSpawn();
		if(activeChar.isInZone(Zone.ZoneType.peace_zone) && activeChar.getZone(Zone.ZoneType.peace_zone).getRestartPoints() != null)
			return player.getKarma() > 1 ? activeChar.getZone(Zone.ZoneType.peace_zone).getPKSpawn() : activeChar.getZone(Zone.ZoneType.peace_zone).getSpawn();
		if(activeChar.isInZone(Zone.ZoneType.offshore) && activeChar.getZone(Zone.ZoneType.offshore).getRestartPoints() != null)
			return player.getKarma() > 1 ? activeChar.getZone(Zone.ZoneType.offshore).getPKSpawn() : activeChar.getZone(Zone.ZoneType.offshore).getSpawn();
		return player.getKarma() > 1 ? cn.getPKSpawn() : cn.getSpawn();
	}

	static
	{
		_log = LoggerFactory.getLogger(MapRegionTable.class);
	}

	public enum TeleportWhereType
	{
		Castle,
		ClanHall,
		ClosestTown,
		SecondClosestTown,
		Headquarter;
	}
}
