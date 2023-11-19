package l2s.gameserver.model.pledge;

import java.sql.Connection;
import java.sql.PreparedStatement;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;

public class ClanMember
{
	private Player _player;
	private Clan _clan;
	private String _name;
	private String _title;
	private int _level;
	private int _classId;
	private int _sex;
	private int playerObjectId;
	private int _pledgeType;
	private int _powerGrade;
	private int _apprentice;
	private Boolean _clanLeader;

	public ClanMember(final Clan clan, final String name, final String title, final int level, final int classId, final int objectId, final int pledgeType, final int powerGrade, final int apprentice, final Boolean clanLeader, final int sex)
	{
		this(clan, name, title, level, classId, objectId, pledgeType, powerGrade, apprentice, clanLeader);
		_sex = sex;
	}

	public ClanMember(final Clan clan, final String name, final String title, final int level, final int classId, final int objectId, final int pledgeType, final int powerGrade, final int apprentice, final Boolean clanLeader)
	{
		_clan = clan;
		_name = name;
		_title = title;
		_level = level;
		_classId = classId;
		_pledgeType = pledgeType;
		_powerGrade = powerGrade;
		_apprentice = apprentice;
		_clanLeader = clanLeader;
		playerObjectId = objectId;
		if(powerGrade != 0)
		{
			final Clan.RankPrivs r = clan.getRankPrivs(powerGrade);
			r.setParty(clan.countMembersByRank(powerGrade));
		}
	}

	public ClanMember(final Player player)
	{
		playerObjectId = player.getObjectId();
	}

	public void setPlayerInstance(final Player player)
	{
		_player = player;
		if(player == null)
			return;

		playerObjectId = player.getObjectId();
		_clan = player.getClan();
		_name = player.getName();
		_title = player.getTitle();
		_level = player.getLevel();
		_classId = player.getClassId().getId();
		_pledgeType = player.getPledgeType();
		_powerGrade = player.getPowerGrade();
		_apprentice = player.getApprentice();
		_clanLeader = player.isClanLeader();
	}

	public Player getPlayer()
	{
		return _player;
	}

	public boolean isOnline()
	{
		final Player player = getPlayer();
		return player != null && !player.isInOfflineMode();
	}

	public Clan getClan()
	{
		final Player player = getPlayer();
		return player == null ? _clan : player.getClan();
	}

	public int getClassId()
	{
		final Player player = getPlayer();
		return player == null ? _classId : player.getClassId().getId();
	}

	public int getRace()
	{
		final Player player = getPlayer();
		return player == null ? 0 : player.getRace().ordinal();
	}

	public int getSex()
	{
		final Player player = getPlayer();
		return player == null ? _sex : player.getSex();
	}

	public int getLevel()
	{
		final Player player = getPlayer();
		return player == null ? _level : player.getLevel();
	}

	public String getName()
	{
		final Player player = getPlayer();
		return player == null ? _name : player.getName();
	}

	public int getObjectId()
	{
		return playerObjectId;
	}

	public String getTitle()
	{
		final Player player = getPlayer();
		return player == null ? _title : player.getTitle();
	}

	public void setTitle(final String title)
	{
		final Player player = getPlayer();
		_title = title;
		if(player != null)
			player.setTitle(title);
		else
		{
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE characters SET title=? WHERE obj_Id=?");
				statement.setString(1, title);
				statement.setInt(2, getObjectId());
				statement.execute();
			}
			catch(Exception ex)
			{}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}

	public int getPledgeType()
	{
		final Player player = getPlayer();
		return player == null ? _pledgeType : player.getPledgeType();
	}

	public void setPledgeType(final int pledgeType)
	{
		final Player player = getPlayer();
		_pledgeType = pledgeType;
		if(player != null)
			player.setPledgeType(pledgeType);
		else
			updatePledgeType();
	}

	public void updatePledgeType()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET pledge_type=? WHERE obj_Id=?");
			statement.setInt(1, _pledgeType);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception ex)
		{}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public int getPowerGrade()
	{
		final Player player = getPlayer();
		return player == null ? _powerGrade : player.getPowerGrade();
	}

	public void setPowerGrade(final int newPowerGrade)
	{
		final Player player = getPlayer();
		final int oldPowerGrade = getPowerGrade();
		_powerGrade = newPowerGrade;
		if(player != null)
			player.setPowerGrade(newPowerGrade);
		else
			updatePowerGrade();
		updatePowerGradeParty(oldPowerGrade, newPowerGrade);
	}

	private void updatePowerGradeParty(final int oldGrade, final int newGrade)
	{
		if(oldGrade != 0)
		{
			final Clan.RankPrivs r1 = getClan().getRankPrivs(oldGrade);
			r1.setParty(getClan().countMembersByRank(oldGrade));
		}
		if(newGrade != 0)
		{
			final Clan.RankPrivs r2 = getClan().getRankPrivs(newGrade);
			r2.setParty(getClan().countMembersByRank(newGrade));
		}
	}

	public void updatePowerGrade()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET pledge_rank=? WHERE obj_Id=?");
			statement.setInt(1, _powerGrade);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception ex)
		{}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	private int getApprentice()
	{
		final Player player = getPlayer();
		return player == null ? _apprentice : player.getApprentice();
	}

	public void setApprentice(final int apprentice)
	{
		final Player player = getPlayer();
		_apprentice = apprentice;
		if(player != null)
			player.setApprentice(apprentice);
		else
			updateApprentice();
	}

	public void updateApprentice()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET apprentice=? WHERE obj_Id=?");
			statement.setInt(1, _apprentice);
			statement.setInt(2, getObjectId());
			statement.execute();
		}
		catch(Exception ex)
		{}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public String getApprenticeName()
	{
		if(getApprentice() != 0 && getClan().getClanMember(Integer.valueOf(getApprentice())) != null)
			return getClan().getClanMember(Integer.valueOf(getApprentice())).getName();
		return "";
	}

	public Boolean hasApprentice()
	{
		return getApprentice() != 0;
	}

	public int getSponsor()
	{
		if(getPledgeType() != -1)
			return 0;
		final int id = getObjectId();
		for(final ClanMember element : getClan().getMembers())
			if(element.getApprentice() == id)
				return element.getObjectId();
		return 0;
	}

	public String getSponsorName()
	{
		final int _sponsorId = getSponsor();
		if(_sponsorId == 0)
			return "";
		if(getClan().getClanMember(Integer.valueOf(_sponsorId)) != null)
			return getClan().getClanMember(Integer.valueOf(_sponsorId)).getName();
		return "";
	}

	public Boolean hasSponsor()
	{
		return getSponsor() != 0;
	}

	public String getRelatedName()
	{
		if(getPledgeType() == -1)
			return getSponsorName();
		return getApprenticeName();
	}

	public boolean isClanLeader()
	{
		final Player player = getPlayer();
		return player == null ? _clanLeader : player.isClanLeader();
	}

	public void setLeader(final boolean v)
	{
		_clanLeader = v;
	}

	public int isSubLeader()
	{
		final Clan c = getClan();
		if(c != null)
			for(final Clan.SubPledge pledge : c.getAllSubPledges())
				if(pledge.getLeaderId() == getObjectId())
					return pledge.getType();
		return 0;
	}
}
