package l2s.gameserver.tables;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.ThreadPoolManager;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.database.mysql;
import l2s.gameserver.idfactory.IdFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Alliance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListDeleteAll;
import l2s.gameserver.network.l2.s2c.SystemMessage;
import l2s.gameserver.utils.SiegeUtils;
import l2s.gameserver.utils.Util;

public class ClanTable
{
	private static final Logger _log;
	private static ClanTable _instance;
	private final TIntObjectHashMap<Clan> _clans;
	private final TIntObjectHashMap<Alliance> _alliances;

	public static ClanTable getInstance()
	{
		if(ClanTable._instance == null)
			new ClanTable();
		return ClanTable._instance;
	}

	public static void unload()
	{
		if(ClanTable._instance != null)
			try
			{
				ClanTable._instance.finalize();
			}
			catch(Throwable t)
			{}
	}

	private ClanTable()
	{
		_clans = new TIntObjectHashMap<Clan>();
		_alliances = new TIntObjectHashMap<Alliance>();
		(ClanTable._instance = this).restoreClans();
		restoreAllies();
		restorewars();
	}

	public Alliance createAlliance(final Player player, final String allyName)
	{
		Alliance alliance = null;
		if(getAllyByName(allyName) == null)
		{
			final Clan leader = player.getClan();
			alliance = new Alliance(IdFactory.getInstance().getNextId(), allyName, leader);
			alliance.store();
			_alliances.put(alliance.getAllyId(), alliance);
			player.getClan().setAllyId(alliance.getAllyId());
			for(final Player temp : player.getClan().getOnlineMembers(0))
				temp.broadcastUserInfo(true);
		}
		return alliance;
	}

	public Clan createClan(final Player player, final String clanName)
	{
		Clan clan = null;
		if(getClanByName(clanName) == null)
		{
			final ClanMember leader = new ClanMember(player);
			clan = new Clan(IdFactory.getInstance().getNextId(), clanName, leader);
			if(Config.CREATE_CLAN_LVL > 0)
			{
				clan.setLevel(Config.CREATE_CLAN_LVL);
				if(Config.CREATE_CLAN_LVL > 3)
					SiegeUtils.addSiegeSkills(player);
				if(Config.CREATE_CLAN_REP > 0 && clan.getLevel() > 4)
					clan.setReputationScore(Config.CREATE_CLAN_REP);
			}
			clan.store();
			player.setClan(clan);
			player.setPowerGrade(6);
			leader.setPlayerInstance(player);
			_clans.put(clan.getClanId(), clan);
		}
		return clan;
	}

	public void deleteAllyFromDb(final int allyId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=0 WHERE ally_id=?");
			statement.setInt(1, allyId);
			statement.execute();
			DbUtils.closeQuietly(statement);
			statement = con.prepareStatement("DELETE FROM ally_data WHERE ally_id=?");
			statement.setInt(1, allyId);
			statement.execute();
		}
		catch(Exception e)
		{
			ClanTable._log.warn("could not dissolve clan: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void deleteClanFromDb(final int clanId)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_notices WHERE clanID=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, clanId);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");
			statement.setInt(1, clanId);
			statement.setInt(2, clanId);
			statement.execute();
			statement.close();
		}
		catch(Exception e)
		{
			ClanTable._log.warn("could not dissolve clan: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	public void dissolveAlly(final Player player)
	{
		final int allyId = player.getAllyId();
		for(final Clan member : player.getAlliance().getMembers())
		{
			member.setAllyId(0);
			member.broadcastClanStatus(false, true, true);
			member.broadcastToOnlineMembers(new SystemMessage(519));
			member.setLeavedAlly();
		}
		deleteAllyFromDb(allyId);
		_alliances.remove(allyId);
		player.sendPacket(new SystemMessage(523));
		player.getClan().setDissolvedAlly();
	}

	public void dissolveClan(final int clanId)
	{
		final Clan clan = getClan(clanId);
		if(clan == null)
			return;
		if(clan.getDissolvingExpiryTime() <= 0L)
		{
			clan.broadcastToOnlineMembers(new SystemMessage(194));
			return;
		}
		final long curtime = System.currentTimeMillis();
		clan.broadcastToOnlineMembers(new SystemMessage(193));
		for(final Player clanMember : clan.getOnlineMembers(0))
		{
			if(clanMember.isClanLeader())
			{
				clanMember.setDeleteClanTime(curtime);
				SiegeUtils.removeSiegeSkills(clanMember);
			}
			clanMember.setClan(null);
			clanMember.setTitle(null);
			clanMember.sendPacket(new PledgeShowMemberListDeleteAll());
			clanMember.sendPacket(new SystemMessage(199));
			clanMember.setLeaveClanTime(curtime);
			clanMember.broadcastUserInfo(true);
		}
		clan.flush();
		_clans.remove(clan.getClanId());
		deleteClanFromDb(clan.getClanId());
	}

	public void dissolveClan(final Player player)
	{
		if(player == null || player.getClan() == null)
			return;
		final Clan clan = player.getClan();
		if(!player.isClanLeader())
		{
			player.sendPacket(new SystemMessage(236));
			return;
		}
		if(clan.getAllyId() != 0)
		{
			player.sendPacket(new SystemMessage(554));
			return;
		}
		if(clan.isAtWar() > 0)
		{
			player.sendPacket(new SystemMessage(264));
			return;
		}
		if(clan.getHasCastle() != 0 || clan.getHasHideout() != 0)
		{
			player.sendPacket(new SystemMessage(14));
			return;
		}
		for(final Residence r : ResidenceHolder.getInstance().getResidences()) {
			SiegeEvent<?, ?> siegeEvent = r.getSiegeEvent();
			if (siegeEvent != null && (siegeEvent.getSiegeClan("attackers", clan) != null || siegeEvent.getSiegeClan("defenders", clan) != null || siegeEvent.getSiegeClan("defenders_waiting", clan) != null)) {
				player.sendPacket(new SystemMessage(13));
				return;
			}
		}
		if(clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			player.sendPacket(new SystemMessage(263));
			return;
		}
		clan.setDissolvingExpiryTime(System.currentTimeMillis() + Config.HoursDissolveClan * 3600000L);
		clan.updateClanInDB();
		if(Config.HoursDissolveClan > 0L)
			scheduleRemoveClan(clan.getClanId(), clan.getDissolvingExpiryTime() - System.currentTimeMillis());
		else
			this.dissolveClan(clan.getClanId());
	}

	public Alliance getAlliance(final int allyId)
	{
		if(allyId <= 0)
			return null;
		return _alliances.get(allyId);
	}

	public Alliance[] getAlliances()
	{
		return _alliances.values(new Alliance[_alliances.size()]);
	}

	public Alliance getAllyByName(final String allyName)
	{
		if(!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE))
			return null;
		for(final Alliance ally : getAlliances())
			if(ally.getAllyName().equalsIgnoreCase(allyName))
				return ally;
		return null;
	}

	public Clan getClan(final int clanId)
	{
		if(clanId <= 0)
			return null;
		return _clans.get(clanId);
	}

	public Map.Entry<Clan, Alliance> getClanAndAllianceByCharId(final int charId)
	{
		final Player player = GameObjectsStorage.getPlayer(charId);
		final Clan charClan = player != null ? player.getClan() : getClan(mysql.simple_get_int("clanid", "characters", "obj_Id=" + charId));
		return new AbstractMap.SimpleEntry<Clan, Alliance>(charClan, charClan == null ? null : charClan.getAlliance());
	}

	public Clan getClanByCharId(final int charId)
	{
		if(charId <= 0)
			return null;
		for(final Clan clan : getClans())
			if(clan != null && clan.isMember(charId))
				return clan;
		return null;
	}

	public Clan getClanByName(final String clanName)
	{
		if(!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE))
			return null;
		for(final Clan clan : getClans())
			if(clan.getName().equalsIgnoreCase(clanName))
				return clan;
		return null;
	}

	public Clan[] getClans()
	{
		return _clans.values(new Clan[_clans.size()]);
	}

	public void restoreAllies()
	{
		final List<Integer> allyIds = new ArrayList<Integer>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT ally_id FROM ally_data");
			result = statement.executeQuery();
			while(result.next())
				allyIds.add(result.getInt("ally_id"));
		}
		catch(Exception e)
		{
			ClanTable._log.warn("Error while restoring allies!!! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, result);
		}
		for(final int allyId : allyIds)
		{
			final Alliance ally = new Alliance(allyId);
			if(ally.getMembersCount() <= 0)
				ClanTable._log.warn("membersCount = 0 for allyId: " + allyId);
			else if(ally.getLeader() == null)
				ClanTable._log.warn("Not found leader for allyId: " + allyId);
			else
				_alliances.put(ally.getAllyId(), ally);
		}
	}

	public void restoreClans()
	{
		final List<Integer> clanIds = new ArrayList<Integer>();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet result = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan_id FROM clan_data");
			result = statement.executeQuery();
			while(result.next())
				clanIds.add(result.getInt("clan_id"));
		}
		catch(Exception e)
		{
			ClanTable._log.warn("Error while restoring clans!!! " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, result);
		}
		for(final int clanId : clanIds)
		{
			final Clan clan = Clan.restore(clanId);
			if(clan == null)
				ClanTable._log.warn("Error while restoring clanId: " + clanId);
			else if(clan.getMembersCount() <= 0)
				ClanTable._log.warn("membersCount = 0 for clanId: " + clanId);
			else if(clan.getLeader() == null)
				ClanTable._log.warn("Not found leader for clanId: " + clanId);
			else
			{
				if(clan.getDissolvingExpiryTime() != 0L)
					if(clan.getDissolvingExpiryTime() < System.currentTimeMillis() || Config.HoursDissolveClan < 1L)
						this.dissolveClan(clan.getClanId());
					else
						scheduleRemoveClan(clan.getClanId(), clan.getDissolvingExpiryTime() - System.currentTimeMillis());
				_clans.put(clan.getClanId(), clan);
			}
		}
	}

	private void restorewars()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT clan1, clan2 FROM clan_wars");
			rset = statement.executeQuery();
			while(rset.next())
			{
				final Clan clan1 = getClan(rset.getInt("clan1"));
				final Clan clan2 = getClan(rset.getInt("clan2"));
				if(clan1 != null && clan2 != null)
				{
					clan1.setEnemyClan(clan2);
					clan2.setAttackerClan(clan1);
				}
			}
		}
		catch(Exception e)
		{
			ClanTable._log.warn("could not restore clan wars data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private void scheduleRemoveClan(final int clanId, final long time)
	{
		ThreadPoolManager.getInstance().schedule(new Runnable(){
			@Override
			public void run()
			{
				if(ClanTable.this.getClan(clanId) == null)
					return;
				if(ClanTable.this.getClan(clanId).getDissolvingExpiryTime() != 0L)
					ClanTable.this.dissolveClan(clanId);
			}
		}, time);
	}

	public void startClanWar(final Clan clan1, final Clan clan2)
	{
		clan1.setEnemyClan(clan2);
		clan2.setAttackerClan(clan1);
		clan1.broadcastClanStatus(false, true, true);
		clan2.broadcastClanStatus(false, true, true);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)");
			statement.setInt(1, clan1.getClanId());
			statement.setInt(2, clan2.getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			ClanTable._log.warn("could not store clan war data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		clan1.broadcastToOnlineMembers(new SystemMessage(1562).addString(clan2.getName()));
		clan2.broadcastToOnlineMembers(new SystemMessage(1561).addString(clan1.getName()));
	}

	public void stopClanWar(final Clan clan1, final Clan clan2)
	{
		clan1.deleteEnemyClan(clan2);
		clan2.deleteAttackerClan(clan1);
		clan1.broadcastClanStatus(false, true, true);
		clan2.broadcastClanStatus(false, true, true);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");
			statement.setInt(1, clan1.getClanId());
			statement.setInt(2, clan2.getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			ClanTable._log.warn("could not delete war data: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		clan1.broadcastToOnlineMembers(new SystemMessage(1567).addString(clan2.getName()));
		clan2.broadcastToOnlineMembers(new SystemMessage(1566).addString(clan1.getName()));
	}

	static
	{
		_log = LoggerFactory.getLogger(ClanTable.class);
	}
}
