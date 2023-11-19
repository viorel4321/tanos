package l2s.gameserver.model.pledge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.tables.ClanTable;

public class Alliance
{
	private static final Logger _log;
	private String _allyName;
	private int _allyId;
	private Clan _leader;
	private Map<Integer, Clan> _members;
	private int _allyCrestId;
	private long _expelledMemberTime;

	public Alliance(final int allyId)
	{
		_leader = null;
		_members = new HashMap<Integer, Clan>();
		_allyId = allyId;
		restore();
	}

	public Alliance(final int allyId, final String allyName, final Clan leader)
	{
		_leader = null;
		_members = new HashMap<Integer, Clan>();
		_allyId = allyId;
		_allyName = allyName;
		setLeader(leader);
	}

	public int getLeaderId()
	{
		return _leader != null ? _leader.getClanId() : 0;
	}

	public Clan getLeader()
	{
		return _leader;
	}

	public void setLeader(final Clan leader)
	{
		_leader = leader;
		_members.put(leader.getClanId(), leader);
	}

	public String getAllyLeaderName()
	{
		return _leader != null ? _leader.getLeaderName() : "";
	}

	public void addAllyMember(final Clan member, final boolean storeInDb)
	{
		_members.put(member.getClanId(), member);
		if(storeInDb)
			storeNewMemberInDatabase(member);
	}

	public Clan getAllyMember(final int id)
	{
		return _members.get(id);
	}

	public void removeAllyMember(final int id)
	{
		if(_leader != null && _leader.getClanId() == id)
			return;
		final Clan exMember = _members.remove(id);
		if(exMember == null)
		{
			Alliance._log.warn("Clan " + id + " not found in alliance while trying to remove");
			return;
		}
		removeMemberInDatabase(exMember);
	}

	public Clan[] getMembers()
	{
		return _members.values().toArray(new Clan[_members.size()]);
	}

	public int getMembersCount()
	{
		return _members.size();
	}

	public int getAllyId()
	{
		return _allyId;
	}

	public String getAllyName()
	{
		return _allyName;
	}

	public void setAllyCrestId(final int allyCrestId)
	{
		_allyCrestId = allyCrestId;
	}

	public int getAllyCrestId()
	{
		return _allyCrestId;
	}

	public void setAllyId(final int allyId)
	{
		_allyId = allyId;
	}

	public void setAllyName(final String allyName)
	{
		_allyName = allyName;
	}

	public boolean isMember(final int id)
	{
		return _members.containsKey(id);
	}

	public void setExpelledMemberTime(final long time)
	{
		_expelledMemberTime = time;
	}

	public long getExpelledMemberTime()
	{
		return _expelledMemberTime;
	}

	public void setExpelledMember()
	{
		_expelledMemberTime = System.currentTimeMillis();
		updateAllyInDB();
	}

	public boolean canInvite()
	{
		return System.currentTimeMillis() - _expelledMemberTime >= Config.HoursBeforeInviteAlly * 3600000L;
	}

	public void updateAllyInDB()
	{
		if(getLeaderId() == 0)
		{
			Alliance._log.warn("updateAllyInDB with empty LeaderId");
			Thread.dumpStack();
			return;
		}
		if(getAllyId() == 0)
		{
			Alliance._log.warn("updateAllyInDB with empty AllyId");
			Thread.dumpStack();
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE ally_data SET leader_id=?,expelled_member=? WHERE ally_id=?");
			statement.setInt(1, getLeaderId());
			statement.setLong(2, getExpelledMemberTime() / 1000L);
			statement.setInt(3, getAllyId());
			statement.execute();
		}
		catch(Exception e)
		{
			Alliance._log.warn("error while updating ally '" + getAllyId() + "' data in db: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void store()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO ally_data (ally_id,ally_name,leader_id) values (?,?,?)");
			statement.setInt(1, getAllyId());
			statement.setString(2, getAllyName());
			statement.setInt(3, getLeaderId());
			statement.execute();
			DbUtils.close(statement);
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=? WHERE clan_id=?");
			statement.setInt(1, getAllyId());
			statement.setInt(2, getLeaderId());
			statement.execute();
			if(Config.DEBUG)
				Alliance._log.info("New ally saved in db: " + getAllyId());
		}
		catch(Exception e)
		{
			Alliance._log.warn("error while saving new ally to db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void storeNewMemberInDatabase(final Clan member)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=? WHERE clan_id=?");
			statement.setInt(1, getAllyId());
			statement.setInt(2, member.getClanId());
			statement.execute();
			if(Config.DEBUG)
				Alliance._log.info("New alliance member saved in db: " + getAllyId());
		}
		catch(Exception e)
		{
			Alliance._log.warn("error while saving new alliance member to db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void removeMemberInDatabase(final Clan member)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET ally_id=0 WHERE clan_id=?");
			statement.setInt(1, member.getClanId());
			statement.execute();
			if(Config.DEBUG)
				Alliance._log.info("ally member removed in db: " + getAllyId());
		}
		catch(Exception e)
		{
			Alliance._log.warn("error while removing ally member in db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private void restore()
	{
		if(getAllyId() == 0)
			return;
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet rset = null;
		ResultSet rset2 = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT ally_name,leader_id FROM ally_data where ally_id=?");
			statement.setInt(1, getAllyId());
			rset = statement.executeQuery();
			if(rset.next())
			{
				setAllyName(rset.getString("ally_name"));
				final int leaderId = rset.getInt("leader_id");
				statement2 = con.prepareStatement("SELECT clan_id,clan_name FROM clan_data WHERE ally_id=?");
				statement2.setInt(1, getAllyId());
				rset2 = statement2.executeQuery();
				while(rset2.next())
				{
					final Clan member = ClanTable.getInstance().getClan(rset2.getInt("clan_id"));
					if(member != null)
						if(member.getClanId() == leaderId)
							setLeader(member);
						else
							addAllyMember(member, false);
				}
			}
			setAllyCrestId(CrestCache.getInstance().getAllyCrestId(getAllyId()));
		}
		catch(Exception e)
		{
			Alliance._log.warn("error while restoring ally");
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(statement2, rset2);
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void broadcastToOnlineMembers(final L2GameServerPacket packet)
	{
		for(final Clan member : _members.values())
			if(member != null)
				member.broadcastToOnlineMembers(packet);
	}

	public void broadcastToOtherOnlineMembers(final L2GameServerPacket packet, final Player player)
	{
		for(final Clan member : _members.values())
			if(member != null)
				member.broadcastToOtherOnlineMembers(packet, player);
	}

	@Override
	public String toString()
	{
		return getAllyName();
	}

	public boolean hasAllyCrest()
	{
		return _allyCrestId > 0;
	}

	public Player[] getOnlineMembers(final String exclude)
	{
		final List<Player> result = new ArrayList<Player>();
		for(final Clan temp : _members.values())
			for(final ClanMember temp2 : temp.getMembers())
				if(temp2.isOnline() && temp2.getPlayer() != null && (exclude == null || !temp2.getName().equals(exclude)))
					result.add(temp2.getPlayer());
		return result.toArray(new Player[result.size()]);
	}

	public void broadcastAllyStatus(final boolean relation)
	{
		for(final Clan member : getMembers())
			member.broadcastClanStatus(false, true, relation);
	}

	static
	{
		_log = LoggerFactory.getLogger(Alliance.class);
	}
}
