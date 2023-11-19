package l2s.gameserver.model.pledge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;
import org.napile.primitive.maps.impl.CTreeIntObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.commons.dbcp.DbUtils;
import l2s.gameserver.Config;
import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.xml.holder.ResidenceHolder;
import l2s.gameserver.database.DatabaseFactory;
import l2s.gameserver.model.Skill;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.residence.Castle;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.ClanWarehouse;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.PledgeReceiveSubPledgeCreated;
import l2s.gameserver.network.l2.s2c.PledgeShowInfoUpdate;
import l2s.gameserver.network.l2.s2c.PledgeShowMemberListAll;
import l2s.gameserver.network.l2.s2c.PledgeSkillList;
import l2s.gameserver.network.l2.s2c.PledgeSkillListAdd;
import l2s.gameserver.network.l2.s2c.SkillList;
import l2s.gameserver.tables.ClanTable;
import l2s.gameserver.tables.SkillTable;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.utils.Log;

public class Clan
{
	private static final Logger _log;
	private String _name;
	private int _clanId;
	private ClanMember _leader;
	private IntObjectMap<ClanMember> _members = new CHashIntObjectMap<>();
	private int _allyId;
	private byte _level;
	private int _hasCastle;
	private int _hiredGuards;
	private int _hasHideout;
	private int _crestId;
	private int _crestLargeId;
	private long _expelledMemberTime;
	private long _leavedAllyTime;
	private long _dissolvedAllyTime;
	private long _dissolvingExpiryTime;
	private ClanWarehouse _warehouse;
	private List<Clan> _atWarWith;
	private List<Clan> _underAttackFrom;
	protected IntObjectMap<Skill> _skills;
	protected IntObjectMap<RankPrivs> _privs;
	protected IntObjectMap<SubPledge> _subPledges;
	private int _reputation;
	public static final int CP_NOTHING = 0;
	public static final int CP_CL_JOIN_CLAN = 2;
	public static final int CP_CL_GIVE_TITLE = 4;
	public static final int CP_CL_VIEW_WAREHOUSE = 8;
	public static final int CP_CL_MANAGE_RANKS = 16;
	public static final int CP_CL_PLEDGE_WAR = 32;
	public static final int CP_CL_DISMISS = 64;
	public static final int CP_CL_REGISTER_CREST = 128;
	public static final int CP_CL_MASTER_RIGHTS = 256;
	public static final int CP_CL_MANAGE_LEVELS = 512;
	public static final int CP_CH_ENTRY_EXIT = 1024;
	public static final int CP_CH_OTHER_RIGHTS = 2048;
	public static final int CP_CH_AUCTION = 4096;
	public static final int CP_CH_DISMISS = 8192;
	public static final int CP_CH_SET_FUNCTIONS = 16384;
	public static final int CP_CS_ENTRY_EXIT = 32768;
	public static final int CP_CS_MANOR_ADMIN = 65536;
	public static final int CP_CS_MANAGE_SIEGE = 131072;
	public static final int CP_CS_USE_FUNCTIONS = 262144;
	public static final int CP_CS_DISMISS = 524288;
	public static final int CP_CS_TAXES = 1048576;
	public static final int CP_CS_MERCENARIES = 2097152;
	public static final int CP_CS_SET_FUNCTIONS = 4194304;
	public static final int CP_ALL = 8388606;
	public static final int SUBUNIT_ACADEMY = -1;
	public static final int SUBUNIT_NONE = 0;
	public static final int SUBUNIT_ROYAL1 = 100;
	public static final int SUBUNIT_ROYAL2 = 200;
	public static final int SUBUNIT_KNIGHT1 = 1001;
	public static final int SUBUNIT_KNIGHT2 = 1002;
	public static final int SUBUNIT_KNIGHT3 = 2001;
	public static final int SUBUNIT_KNIGHT4 = 2002;
	private static final ClanReputationComparator REPUTATION_COMPARATOR;
	private static final int REPUTATION_PLACES = 100;
	private String _notice;
	public static final int NOTICE_MAX_LENGHT = 512;
	private boolean _noticeEnabled;
	public long NEXT_RCM;
	public long block_invite;

	public static Clan restore(final int clanId)
	{
		if(clanId == 0)
			return null;
		Clan clan = null;
		int leaderId = 0;
		Connection con1 = null;
		PreparedStatement statement1 = null;
		ResultSet clanData = null;
		try
		{
			con1 = DatabaseFactory.getInstance().getConnection();
			statement1 = con1.prepareStatement("SELECT clan_name,clan_level,hasCastle,hasHideout,ally_id,leader_id,reputation_score,expelled_member,leaved_ally,dissolved_ally,dissolving_expiry_time FROM clan_data where clan_id=?");
			statement1.setInt(1, clanId);
			clanData = statement1.executeQuery();
			if(!clanData.next())
			{
				Clan._log.warn("L2Clan.java clan " + clanId + " does't exist");
				return null;
			}
			clan = new Clan(clanId);
			clan.setName(clanData.getString("clan_name"));
			clan.setLevel(clanData.getByte("clan_level"));
			clan.setHasCastle(clanData.getByte("hasCastle"));
			clan.setHasHideout(clanData.getInt("hasHideout"));
			clan.setAllyId(clanData.getInt("ally_id"));
			clan._reputation = clanData.getInt("reputation_score");
			clan.setExpelledMemberTime(clanData.getLong("expelled_member") * 1000L);
			clan.setLeavedAllyTime(clanData.getLong("leaved_ally") * 1000L);
			clan.setDissolvedAllyTime(clanData.getLong("dissolved_ally") * 1000L);
			clan.setDissolvingExpiryTime(clanData.getLong("dissolving_expiry_time") * 1000L);
			leaderId = clanData.getInt("leader_id");
			if(clan.getName() == null)
				Clan._log.warn("null name for clan: " + clanId);
		}
		catch(Exception e)
		{
			Clan._log.warn("error while restoring clan " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con1, statement1, clanData);
		}
		if(clan == null)
		{
			Clan._log.warn("Clan " + clanId + " does't exist");
			return null;
		}
		if(leaderId == 0)
		{
			Clan._log.warn("Not found leader for clan: " + clanId);
			return null;
		}
		Connection con2 = null;
		PreparedStatement statement2 = null;
		ResultSet clanMembers = null;
		try
		{
			con2 = DatabaseFactory.getInstance().getConnection();
			statement2 = con2.prepareStatement("SELECT `c`.`char_name` AS `char_name`,`s`.`level` AS `level`,`s`.`class_id` AS `classid`,`c`.`obj_Id` AS `obj_id`,`c`.`title` AS `title`,`c`.`pledge_type` AS `pledge_type`,`c`.`pledge_rank` AS `pledge_rank`,`c`.`apprentice` AS `apprentice` FROM `characters` `c` LEFT JOIN `character_subclasses` `s` ON (`s`.`char_obj_id` = `c`.`obj_Id` AND `s`.`isBase` = '1') WHERE `c`.`clanid`=? ORDER BY `c`.`lastaccess` DESC");
			statement2.setInt(1, clanId);
			clanData = statement2.executeQuery();
			statement2.setInt(1, clan.getClanId());
			clanMembers = statement2.executeQuery();
			while(clanMembers.next())
			{
				final ClanMember member = new ClanMember(clan, clanMembers.getString("char_name"), clanMembers.getString("title"), clanMembers.getInt("level"), clanMembers.getInt("classid"), clanMembers.getInt("obj_id"), clanMembers.getInt("pledge_type"), clanMembers.getInt("pledge_rank"), clanMembers.getInt("apprentice"), clanMembers.getInt("obj_id") == leaderId);
				if(member.getObjectId() == leaderId)
					clan.setLeader(member);
				else
					clan.addClanMember(member);
			}
			if(clan.getLeader() == null)
				Clan._log.warn("Clan " + clan.getName() + " have no leader!");
		}
		catch(Exception e2)
		{
			Clan._log.warn("Error while restoring clan members for clan: " + clanId + " " + e2);
			e2.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con2, statement2, clanMembers);
		}
		clan.restoreSkills();
		clan.restoreSubPledges();
		clan.restoreRankPrivs();
		clan.setCrestId(CrestCache.getInstance().getPledgeCrestId(clanId));
		clan.setCrestLargeId(CrestCache.getInstance().getPledgeCrestLargeId(clanId));
		return clan;
	}

	private Clan(final int clanId)
	{
		_leader = null;
		_hasCastle = 0;
		_hasHideout = 0;
		_warehouse = new ClanWarehouse(this);
		_atWarWith = new ArrayList<Clan>();
		_underAttackFrom = new ArrayList<Clan>();
		_skills = new CTreeIntObjectMap<Skill>();
		_privs = new CTreeIntObjectMap<RankPrivs>();
		_subPledges = new CTreeIntObjectMap<SubPledge>();
		_reputation = 0;
		_noticeEnabled = true;
		NEXT_RCM = 0L;
		block_invite = 0L;
		_clanId = clanId;
		InitializePrivs();
	}

	public Clan(final int clanId, final String clanName, final ClanMember leader)
	{
		_leader = null;
		_hasCastle = 0;
		_hasHideout = 0;
		_warehouse = new ClanWarehouse(this);
		_atWarWith = new ArrayList<Clan>();
		_underAttackFrom = new ArrayList<Clan>();
		_skills = new CTreeIntObjectMap<Skill>();
		_privs = new CTreeIntObjectMap<RankPrivs>();
		_subPledges = new CTreeIntObjectMap<SubPledge>();
		_reputation = 0;
		_noticeEnabled = true;
		NEXT_RCM = 0L;
		block_invite = 0L;
		_clanId = clanId;
		_name = clanName;
		InitializePrivs();
		setLeader(leader);
		insertNotice();
	}

	public void addAndShowSkillsToPlayer(final Player activeChar)
	{
		for(final Skill s : _skills.valueCollection())
		{
			if(s == null)
				continue;
			activeChar.sendPacket(new PledgeSkillListAdd(s.getId(), s.getLevel()));
			if(s.getMinPledgeClass() <= activeChar.getPledgeClass())
				activeChar.addSkill(s, false);
			if(_reputation >= 0 && !activeChar.getPlayer().isInOlympiadMode())
				continue;
			activeChar.getPlayer().addUnActiveSkill(s);
		}
		activeChar.sendPacket(new PledgeSkillList(this));
		activeChar.sendPacket(new SkillList(activeChar));
	}

	private void addClanMember(final ClanMember member)
	{
		_members.put(member.getObjectId(), member);
	}

	public void addClanMember(final Player player)
	{
		final ClanMember member = new ClanMember(this, player.getName(), player.getTitle(), player.getLevel(), player.getClassId().getId(), player.getObjectId(), player.getPledgeType(), player.getPowerGrade(), player.getApprentice(), false, player.getSex());
		this.addClanMember(member);
	}

	public Skill addNewSkill(final Skill newSkill, final boolean store)
	{
		Skill oldSkill = null;
		if(newSkill != null)
		{
			oldSkill = _skills.put(newSkill.getId(), newSkill);
			if(store)
			{
				Connection con = null;
				PreparedStatement statement = null;
				try
				{
					con = DatabaseFactory.getInstance().getConnection();
					if(oldSkill != null)
					{
						statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
						statement.setInt(1, newSkill.getLevel());
						statement.setInt(2, oldSkill.getId());
						statement.setInt(3, getClanId());
						statement.execute();
					}
					else
					{
						statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level,skill_name) VALUES (?,?,?,?)");
						statement.setInt(1, getClanId());
						statement.setInt(2, newSkill.getId());
						statement.setInt(3, newSkill.getLevel());
						statement.setString(4, newSkill.getName());
						statement.execute();
					}
				}
				catch(Exception e)
				{
					Clan._log.warn("Error could not store char skills: " + e);
				}
				finally
				{
					DbUtils.closeQuietly(con, statement);
				}
			}
			for(ClanMember temp : _members.valueCollection())
				if(temp.isOnline() && temp.getPlayer() != null)
				{
					temp.getPlayer().sendPacket(new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel()));
					if(newSkill.getMinPledgeClass() > temp.getPlayer().getPledgeClass())
						continue;
					temp.getPlayer().addSkill(newSkill, false);
					temp.getPlayer().sendPacket(new SkillList(temp.getPlayer()));
					if(_reputation < 0 || temp.getPlayer().isInOlympiadMode())
						temp.getPlayer().addUnActiveSkill(newSkill);
					temp.getPlayer().updateStats();
				}
		}
		return oldSkill;
	}

	public final void addSubPledge(final SubPledge sp, final boolean updateDb)
	{
		_subPledges.put(sp.getType(), sp);
		if(updateDb)
		{
			this.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(sp));
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("INSERT INTO `clan_subpledges` (clan_id,type,leader_id,name) VALUES (?,?,?,?)");
				statement.setInt(1, getClanId());
				statement.setInt(2, sp.getType());
				statement.setInt(3, sp.getLeaderId());
				statement.setString(4, sp.getName());
				statement.execute();
			}
			catch(Exception e)
			{
				Clan._log.warn("Could not store clan Sub pledges: " + e);
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}

	public void boarcastSkillListToOnlineMembers()
	{
		for(ClanMember temp : _members.valueCollection())
			if(temp.isOnline() && temp.getPlayer() != null)
				addAndShowSkillsToPlayer(temp.getPlayer());
	}

	public void broadcastClanStatus(final boolean updateList, final boolean needUserInfo, final boolean relation)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.isOnline())
			{
				if(updateList)
					member.getPlayer().sendPacket(Msg.PledgeShowMemberListDeleteAll, new PledgeShowMemberListAll(this, member.getPlayer()));
				member.getPlayer().sendPacket(new PledgeShowInfoUpdate(this));
				if(needUserInfo)
					member.getPlayer().broadcastUserInfo(true);
				if(!relation)
					continue;
				member.getPlayer().broadcastRelationChanged();
			}
	}

	public void broadcastToOnlineMembers(final IBroadcastPacket... packets)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.isOnline())
				member.getPlayer().sendPacket(packets);
	}

	public void broadcastToOnlineMembers(final L2GameServerPacket... packets)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.isOnline())
				member.getPlayer().sendPacket(packets);
	}

	public void broadcastToOtherOnlineMembers(final L2GameServerPacket packet, final Player player)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.isOnline() && member.getPlayer() != player)
				member.getPlayer().sendPacket(packet);
	}

	public boolean canCreateAlly()
	{
		return System.currentTimeMillis() - _dissolvedAllyTime >= Config.DaysBeforeCreateNewAllyWhenDissolved * 24L * 3600000L;
	}

	public boolean canInvite()
	{
		return System.currentTimeMillis() - _expelledMemberTime >= Config.HoursBeforeInviteClan * 3600000L;
	}

	public boolean canJoinAlly()
	{
		return System.currentTimeMillis() - _leavedAllyTime >= Config.HoursBeforeJoinAlly * 3600000L;
	}

	public int createSubPledge(final Player player, int pledgeType, final int leaderId, final String name)
	{
		final int temp = pledgeType;
		pledgeType = getAvailablePledgeTypes(pledgeType);
		if(pledgeType == 0)
		{
			if(temp == -1)
				player.sendPacket(Msg.YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
			else
				player.sendMessage("You can't create any more sub-units of this type.");
			return 0;
		}
		switch(pledgeType)
		{
			case 100:
			case 200:
			{
				if(Config.ROYAL_REP > 0 && getReputationScore() < Config.ROYAL_REP)
				{
					player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return 0;
				}
				incReputation(-Config.ROYAL_REP, false, "SubunitRoyalCreate");
				break;
			}
			case 1001:
			case 1002:
			case 2001:
			case 2002:
			{
				if(Config.KNIGHT_REP > 0 && getReputationScore() < Config.KNIGHT_REP)
				{
					player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
					return 0;
				}
				incReputation(-Config.KNIGHT_REP, false, "SubunitKnightCreate");
				break;
			}
		}
		addSubPledge(new SubPledge(pledgeType, leaderId, name), true);
		return pledgeType;
	}

	public void deleteAttackerClan(final Clan clan)
	{
		_underAttackFrom.remove(clan);
	}

	public void deleteEnemyClan(final Clan clan)
	{
		_atWarWith.remove(clan);
	}

	public void flush()
	{
		for(final ClanMember member : getMembers())
			this.removeClanMember(member.getObjectId());
		for(final ItemInstance item : _warehouse.listItems(ItemClass.ALL))
			_warehouse.destroyItem(item.getItemId(), item.getCount());
		if(_hasCastle != 0)
			ResidenceHolder.getInstance().getResidence(Castle.class, _hasCastle).changeOwner(null);
	}

	public ItemInstance getAdena()
	{
		return _warehouse.findItemId(57);
	}

	public long getAdenaCount()
	{
		return _warehouse.getAdenaCount();
	}

	public long getAuctionItemCount()
	{
		return _warehouse.countOf(Config.CH_AUCTION_BID_ID);
	}

	public int getAffiliationRank(final int pledgeType)
	{
		if(isAcademy(pledgeType))
			return 9;
		if(isOrderOfKnights(pledgeType))
			return 8;
		if(isRoyalGuard(pledgeType))
			return 7;
		return 6;
	}

	public Alliance getAlliance()
	{
		return _allyId == 0 ? null : ClanTable.getInstance().getAlliance(_allyId);
	}

	public String getAllyName()
	{
		final Alliance a = getAlliance();
		return a != null ? a.getAllyName() : "";
	}

	public int getAllyCrestId()
	{
		final Alliance a = getAlliance();
		return a != null ? a.getAllyCrestId() : 0;
	}

	public final RankPrivs[] getAllRankPrivs()
	{
		if(_privs == null)
			return new RankPrivs[0];
		return _privs.values(new RankPrivs[_privs.size()]);
	}

	public final Skill[] getAllSkills()
	{
		if(_reputation < 0)
			return new Skill[0];
		return _skills.values(new Skill[_skills.size()]);
	}

	public final SubPledge[] getAllSubPledges()
	{
		return _subPledges.values(new SubPledge[_subPledges.size()]);
	}

	public int getAllyId()
	{
		return _allyId;
	}

	public List<Clan> getAttackerClans()
	{
		return _underAttackFrom;
	}

	public int getAvailablePledgeTypes(int pledgeType)
	{
		if(pledgeType == 0)
			return 0;
		if(_subPledges.get(pledgeType) != null)
			switch(pledgeType)
			{
				case -1:
				{
					return 0;
				}
				case 100:
				{
					pledgeType = getAvailablePledgeTypes(200);
					break;
				}
				case 200:
				{
					return 0;
				}
				case 1001:
				{
					pledgeType = getAvailablePledgeTypes(1002);
					break;
				}
				case 1002:
				{
					pledgeType = getAvailablePledgeTypes(2001);
					break;
				}
				case 2001:
				{
					pledgeType = getAvailablePledgeTypes(2002);
					break;
				}
				case 2002:
				{
					return 0;
				}
			}
		return pledgeType;
	}

	public int getClanId()
	{
		return _clanId;
	}

	public ClanMember getClanMember(final Integer id)
	{
		return _members.get(id);
	}

	public ClanMember getClanMember(final String name)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.getName().equals(name))
				return member;
		return null;
	}

	public int getCrestId()
	{
		return _crestId;
	}

	public int getCrestLargeId()
	{
		return _crestLargeId;
	}

	public long getDissolvedAllyTime()
	{
		return _dissolvedAllyTime;
	}

	public long getDissolvingExpiryTime()
	{
		return _dissolvingExpiryTime;
	}

	public List<Clan> getEnemyClans()
	{
		return _atWarWith;
	}

	public long getExpelledMemberTime()
	{
		return _expelledMemberTime;
	}

	public int getHasCastle()
	{
		return _hasCastle;
	}

	public int getHasHideout()
	{
		return _hasHideout;
	}

	public int getHiredGuards()
	{
		return _hiredGuards;
	}

	public ClanMember getLeader()
	{
		return _leader;
	}

	public int getLeaderId()
	{
		return _leader != null ? _leader.getObjectId() : 0;
	}

	public String getLeaderName()
	{
		return _leader.getName();
	}

	public long getLeavedAllyTime()
	{
		return _leavedAllyTime;
	}

	public byte getLevel()
	{
		return _level;
	}

	public ClanMember[] getMembers()
	{
		return _members.values(new ClanMember[_members.size()]);
	}

	public int getMembersCount()
	{
		return _members.size();
	}

	public String getName()
	{
		return _name;
	}

	public Player[] getOnlineMembers(final int exclude)
	{
		final List<Player> result = new ArrayList<Player>();
		for(ClanMember temp : _members.valueCollection())
			if(temp.isOnline() && temp.getObjectId() != exclude)
				result.add(temp.getPlayer());
		return result.toArray(new Player[result.size()]);
	}

	public int getRank()
	{
		final Clan[] clans = ClanTable.getInstance().getClans();
		Arrays.sort(clans, Clan.REPUTATION_COMPARATOR);
		final int place = 1;
		for(int i = 0; i < clans.length; ++i)
		{
			if(i == 100)
				return 0;
			final Clan clan = clans[i];
			if(clan == this)
				return place + i;
		}
		return 0;
	}

	public RankPrivs getRankPrivs(final int rank)
	{
		if(rank < 1 || rank > 9)
			return null;
		if(_privs.get(rank) == null)
			setRankPrivs(rank, 0);
		return _privs.get(rank);
	}

	public int getReputationScore()
	{
		return _reputation;
	}

	public Collection<Skill> getSkills()
	{
		return _skills.valueCollection();
	}

	public final SubPledge getSubPledge(final int pledgeType)
	{
		if(_subPledges == null)
			return null;
		return _subPledges.get(pledgeType);
	}

	public int getSubPledgeLeaderId(final int pledgeType)
	{
		return _subPledges.get(pledgeType).getLeaderId();
	}

	public int getSubPledgeLimit(final int pledgeType)
	{
		switch(pledgeType)
		{
			case 0:
			{
				switch(getLevel())
				{
					case 0:
						return 10;
					case 1:
						return 15;
					case 2:
						return 20;
					case 3:
						return 30;
					default:
						return Config.MAX_CLAN_MEMBERS;
				}
			}
			case -1:
			case 100:
			case 200:
			{
				return 20;
			}
			case 1001:
			case 1002:
			case 2001:
			case 2002:
			{
				return 10;
			}
			default:
			{
				return 0;
			}
		}
	}

	public int getSubPledgeMembersCount(final int pledgeType)
	{
		int result = 0;
		for(ClanMember temp : _members.valueCollection())
			if(temp.getPledgeType() == pledgeType)
				++result;
		return result;
	}

	public ClanWarehouse getWarehouse()
	{
		return _warehouse;
	}

	public int getWarsCount()
	{
		return _atWarWith.size();
	}

	public boolean hasCrest()
	{
		return _crestId > 0;
	}

	public boolean hasCrestLarge()
	{
		return _crestLargeId > 0;
	}

	public void incrementHiredGuards()
	{
		++_hiredGuards;
	}

	public int incReputation(int inc, final boolean rate, final String source)
	{
		if(inc == 0)
			return 0;
		if(getLevel() < 5)
			new Throwable("Trying to gauge clan reputation for clan below 5 lvl").printStackTrace();
		if(rate && Math.abs(inc) <= Config.RATE_CLAN_REP_SCORE_MAX_AFFECTED)
			inc = Math.round(inc * Config.RATE_CLAN_REP_SCORE);
		setReputationScore(_reputation + inc);
		Log.addLog("Clan [" + _name + "] get " + inc + " | Total: " + _reputation + " | " + source, "clan_reputation");
		return inc;
	}

	public void InitializePrivs()
	{
		for(int i = 1; i < 10; ++i)
			_privs.put(i, new RankPrivs(i, 0, 0));
	}

	public void insertNotice()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO clan_notices (clanID, notice, enabled) values (?,?,?)");
			statement.setInt(1, getClanId());
			statement.setString(2, "Change me");
			statement.setString(3, "false");
			statement.execute();
		}
		catch(Exception e)
		{
			Clan._log.error("BBS: Error while creating clan notice for clan " + getClanId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public String getNotice()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT notice FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
				_notice = rset.getString("notice");
		}
		catch(Exception e)
		{
			Clan._log.error("BBS: Error while getting notice from DB for clan " + getClanId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return _notice;
	}

	public String getNoticeForBBS()
	{
		String notice = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT notice FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
				notice = rset.getString("notice");
		}
		catch(Exception e)
		{
			Clan._log.error("BBS: Error while getting notice from DB for clan " + getClanId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		return notice.replaceAll("<br>", "\n");
	}

	public void setNotice(String notice)
	{
		notice = notice.replaceAll("\n", "<br>");
		if(notice.length() > 512)
			notice = notice.substring(0, 511);
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_notices SET notice=? WHERE clanID=?");
			statement.setString(1, notice);
			statement.setInt(2, getClanId());
			statement.execute();
			_notice = notice;
		}
		catch(Exception e)
		{
			Clan._log.error("BBS: Error while saving notice for clan " + getClanId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public boolean isNoticeEnabled()
	{
		String result = "";
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT enabled FROM clan_notices WHERE clanID=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
				result = rset.getString("enabled");
		}
		catch(Exception e)
		{
			Clan._log.error("BBS: Error while reading _noticeEnabled for clan " + getClanId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
		if(result.isEmpty())
			insertNotice();
		else if(result.compareToIgnoreCase("true") == 0)
			return true;
		return false;
	}

	public void setNoticeEnabled(final boolean noticeEnabled)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_notices SET enabled=? WHERE clanID=?");
			if(noticeEnabled)
				statement.setString(1, "true");
			else
				statement.setString(1, "false");
			statement.setInt(2, getClanId());
			statement.execute();
		}
		catch(Exception e)
		{
			Clan._log.error("BBS: Error while updating notice status for clan " + getClanId(), e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
		_noticeEnabled = noticeEnabled;
	}

	public final boolean isAcademy(final int pledgeType)
	{
		return pledgeType == -1;
	}

	public int isAtWar()
	{
		if(_atWarWith != null && _atWarWith.size() > 0)
			return 1;
		return 0;
	}

	public int isAtWarOrUnderAttack()
	{
		if(_atWarWith != null && _atWarWith.size() > 0 || _underAttackFrom != null && _underAttackFrom.size() > 0)
			return 1;
		return 0;
	}

	public boolean isAtWarWith(final Integer id)
	{
		final Clan clan = ClanTable.getInstance().getClan(id);
		return _atWarWith != null && _atWarWith.size() > 0 && _atWarWith.contains(clan);
	}

	public boolean isMember(final Integer id)
	{
		return _members.containsKey(id);
	}

	public final boolean isOrderOfKnights(final int pledgeType)
	{
		return pledgeType == 1001 || pledgeType == 1002 || pledgeType == 2001 || pledgeType == 2002;
	}

	public final boolean isRoyalGuard(final int pledgeType)
	{
		return pledgeType == 100 || pledgeType == 200;
	}

	public boolean isUnderAttackFrom(final Integer id)
	{
		final Clan clan = ClanTable.getInstance().getClan(id);
		return _underAttackFrom != null && _underAttackFrom.size() > 0 && _underAttackFrom.contains(clan);
	}

	public void removeClanMember(final int id)
	{
		if(id == getLeaderId())
			return;
		final ClanMember exMember = _members.remove(id);
		if(exMember == null)
			return;
		final SubPledge sp = _subPledges.get(exMember.getPledgeType());
		if(sp != null && sp.getLeaderId() == exMember.getObjectId())
			sp.setLeaderId(0);
		if(exMember.hasSponsor())
			this.getClanMember(Integer.valueOf(exMember.getSponsor())).setApprentice(0);
		removeMemberInDatabase(exMember);
	}

	public void removeClanMember(final String name)
	{
		if(name.equals(getLeaderName()))
			return;
		final ClanMember exMember = this.getClanMember(name);
		if(exMember == null)
			return;
		_members.remove(exMember.getObjectId());
		final SubPledge sp = _subPledges.get(exMember.getPledgeType());
		if(sp != null && sp.getLeaderId() == exMember.getObjectId())
			sp.setLeaderId(0);
		removeMemberInDatabase(exMember);
	}

	private void removeMemberInDatabase(final ClanMember member)
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE characters SET clanid=0, pledge_type=0, pledge_rank=0, lvl_joined_academy=0, apprentice=0, title='', leaveclan=? WHERE obj_Id=?");
			statement.setLong(1, System.currentTimeMillis() / 1000L);
			statement.setInt(2, member.getObjectId());
			statement.execute();
			if(Config.DEBUG)
				Clan._log.info("clan member removed in db: " + getClanId());
		}
		catch(Exception e)
		{
			Clan._log.warn("error while removing clan member in db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void removeSkill(final Skill skill)
	{
		_skills.remove(skill.getId());
		for(ClanMember temp : _members.valueCollection())
			if(temp.isOnline() && temp.getPlayer() != null)
				temp.getPlayer().removeSkill(skill);
	}

	private void restoreRankPrivs()
	{
		if(_privs == null)
			InitializePrivs();
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT `privilleges`, `rank` FROM `clan_privs` WHERE `clan_id`=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int rank = rset.getInt("rank");
				final int privileges = rset.getInt("privilleges");
				final RankPrivs p = _privs.get(rank);
				if(p != null)
					p.setPrivs(privileges);
				else
					Clan._log.warn("Invalid rank value (" + rank + "), please check clan_privs table");
			}
		}
		catch(Exception e)
		{
			Clan._log.warn("Could not restore clan privs by rank: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private void restoreSkills()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int id = rset.getInt("skill_id");
				final int level = rset.getInt("skill_level");
				final Skill skill = SkillTable.getInstance().getInfo(id, level);
				if(skill != null)
					_skills.put(skill.getId(), skill);
			}
		}
		catch(Exception e)
		{
			Clan._log.warn("Could not restore clan skills: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	private void restoreSubPledges()
	{
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rset = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("SELECT * FROM clan_subpledges WHERE clan_id=?");
			statement.setInt(1, getClanId());
			rset = statement.executeQuery();
			while(rset.next())
			{
				final int type = rset.getInt("type");
				final int leaderId = rset.getInt("leader_id");
				final String name = rset.getString("name");
				final SubPledge pledge = new SubPledge(type, leaderId, name);
				addSubPledge(pledge, false);
			}
		}
		catch(Exception e)
		{
			Clan._log.warn("Could not restore clan SubPledges: " + e);
			e.printStackTrace();
		}
		finally
		{
			DbUtils.closeQuietly(con, statement, rset);
		}
	}

	public void sendMessageToAll(final String message)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.isOnline() && member.getPlayer() != null)
				member.getPlayer().sendMessage(message);
	}

	public void sendMessageToAll(final String message, final String message_ru)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.isOnline() && member.getPlayer() != null)
			{
				final Player player = member.getPlayer();
				if(!player.isLangRus() || message_ru.equals(""))
					player.sendMessage(message);
				else
					player.sendMessage(message_ru);
			}
	}

	public void setAllyId(final int allyId)
	{
		_allyId = allyId;
	}

	public void setAttackerClan(final Integer clan)
	{
		final Clan Clan = ClanTable.getInstance().getClan(clan);
		_underAttackFrom.add(Clan);
	}

	public void setAttackerClan(final Clan clan)
	{
		_underAttackFrom.add(clan);
	}

	public void setClanId(final int clanId)
	{
		_clanId = clanId;
	}

	public void setCrestId(final int newcrest)
	{
		_crestId = newcrest;
	}

	public void setCrestLargeId(final int newcrest)
	{
		_crestLargeId = newcrest;
	}

	public void setDissolvedAlly()
	{
		_dissolvedAllyTime = System.currentTimeMillis();
		updateClanInDB();
	}

	public void setDissolvedAllyTime(final long time)
	{
		_dissolvedAllyTime = time;
	}

	public void setDissolvingExpiryTime(final long time)
	{
		_dissolvingExpiryTime = time;
	}

	public void setEnemyClan(final Integer clan)
	{
		Clan._log.warn("setEnemyClan");
		final Clan Clan = ClanTable.getInstance().getClan(clan);
		_atWarWith.add(Clan);
	}

	public void setEnemyClan(final Clan clan)
	{
		_atWarWith.add(clan);
	}

	public void setExpelledMember()
	{
		_expelledMemberTime = System.currentTimeMillis();
		updateClanInDB();
	}

	public void setExpelledMemberTime(final long time)
	{
		_expelledMemberTime = time;
	}

	public void setHasCastle(final int castle)
	{
		_hasCastle = castle;
	}

	public void setHasHideout(final int hasHideout)
	{
		_hasHideout = hasHideout;
	}

	public void setLeader(final ClanMember leader)
	{
		_leader = leader;
		_members.put(leader.getObjectId(), leader);
	}

	public void setLeavedAlly()
	{
		_leavedAllyTime = System.currentTimeMillis();
		updateClanInDB();
	}

	public void setLeavedAllyTime(final long time)
	{
		_leavedAllyTime = time;
	}

	public void setLevel(final byte level)
	{
		_level = level;
	}

	public void setName(final String name)
	{
		_name = name;
	}

	public int countMembersByRank(final int rank)
	{
		int ret = 0;
		for(final ClanMember m : getMembers())
			if(m.getPowerGrade() == rank)
				++ret;
		return ret;
	}

	public void setRankPrivs(final int rank, final int privs)
	{
		if(rank < 1 || rank > 9)
			return;
		if(_privs.get(rank) != null)
			_privs.get(rank).setPrivs(privs);
		else
			_privs.put(rank, new RankPrivs(rank, countMembersByRank(rank), privs));
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("REPLACE INTO `clan_privs` (`clan_id`, `rank`, `privilleges`) VALUES (?,?,?)");
			statement.setInt(1, getClanId());
			statement.setInt(2, rank);
			statement.setInt(3, privs);
			statement.execute();
		}
		catch(Exception e)
		{
			Clan._log.warn("Could not store clan privs for rank: " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con, statement);
		}
	}

	public void setReputationScore(final int rep)
	{
		if(_reputation >= 0 && rep < 0)
		{
			this.broadcastToOnlineMembers(Msg.SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DE_ACTIVATED);
			final Skill[] skills = getAllSkills();
			for(ClanMember member : _members.valueCollection())
				if(member.isOnline())
					for(final Skill sk : skills)
						member.getPlayer().addUnActiveSkill(sk);
		}
		else if(_reputation < 0 && rep >= 0)
		{
			this.broadcastToOnlineMembers(Msg.THE_CLAN_SKILL_WILL_BE_ACTIVATED_BECAUSE_THE_CLANS_REPUTATION_SCORE_HAS_REACHED_TO_0_OR_HIGHER);
			final Skill[] skills = getAllSkills();
			for(ClanMember member : _members.valueCollection())
				if(member.isOnline())
				{
					if(member.getPlayer().isInOlympiadMode())
						return;
					for(final Skill sk : skills)
					{
						member.getPlayer().sendPacket(new PledgeSkillListAdd(sk.getId(), sk.getLevel()));
						if(sk.getMinPledgeClass() <= member.getPlayer().getPledgeClass())
							member.getPlayer().removeUnActiveSkill(sk);
					}
				}
		}
		if(_reputation != rep)
		{
			_reputation = rep;
			this.broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
		}
		updateClanInDB();
	}

	public void store()
	{
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,hasHideout,ally_id,leader_id,expelled_member,leaved_ally,dissolved_ally,dissolving_expiry_time) values (?,?,?,?,?,?,?,?,?,?,?)");
			statement.setInt(1, _clanId);
			statement.setString(2, _name);
			statement.setInt(3, _level);
			statement.setInt(4, _hasCastle);
			statement.setInt(5, _hasHideout);
			statement.setInt(6, _allyId);
			statement.setInt(7, getLeaderId());
			statement.setLong(8, getExpelledMemberTime() / 1000L);
			statement.setLong(9, getLeavedAllyTime() / 1000L);
			statement.setLong(10, getDissolvedAllyTime() / 1000L);
			statement.setLong(11, getDissolvingExpiryTime() / 1000L);
			statement.execute();
			statement.close();
			statement = con.prepareStatement("UPDATE characters SET clanid=?,pledge_type=0 WHERE obj_Id=?");
			statement.setInt(1, getClanId());
			statement.setInt(2, getLeaderId());
			statement.execute();
			statement.close();
			if(Config.DEBUG)
				Clan._log.info("New clan saved in db: " + getClanId());
		}
		catch(Exception e)
		{
			Clan._log.warn("error while saving new clan to db " + e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	@Override
	public String toString()
	{
		return getName();
	}

	public void updateClanInDB()
	{
		if(getLeaderId() == 0)
		{
			Clan._log.warn("updateClanInDB with empty LeaderId");
			Thread.dumpStack();
			return;
		}
		if(getClanId() == 0)
		{
			Clan._log.warn("updateClanInDB with empty ClanId");
			Thread.dumpStack();
			return;
		}
		Connection con = null;
		PreparedStatement statement = null;
		try
		{
			con = DatabaseFactory.getInstance().getConnection();
			statement = con.prepareStatement("UPDATE clan_data SET leader_id=?,ally_id=?,reputation_score=?,expelled_member=?,leaved_ally=?,dissolved_ally=?,clan_level=?,dissolving_expiry_time=?,clan_name=? WHERE clan_id=?");
			statement.setInt(1, getLeaderId());
			statement.setInt(2, getAllyId());
			statement.setInt(3, getReputationScore());
			statement.setLong(4, getExpelledMemberTime() / 1000L);
			statement.setLong(5, getLeavedAllyTime() / 1000L);
			statement.setLong(6, getDissolvedAllyTime() / 1000L);
			statement.setInt(7, getLevel());
			statement.setLong(8, getDissolvingExpiryTime() / 1000L);
			statement.setString(9, getName());
			statement.setInt(10, getClanId());
			statement.execute();
			statement.close();
			if(Config.DEBUG)
				Clan._log.info("Clan data saved in db: " + getClanId());
		}
		catch(Exception e)
		{
			Clan._log.error("while updating clan '" + getClanId() + "' data in db: ", e);
		}
		finally
		{
			DbUtils.closeQuietly(con);
		}
	}

	public void updatePrivsForRank(final int rank)
	{
		for(ClanMember member : _members.valueCollection())
			if(member.isOnline() && member.getPlayer() != null && member.getPlayer().getPowerGrade() == rank)
			{
				if(member.getPlayer().isClanLeader())
					continue;
				member.getPlayer().sendUserInfo(false);
			}
	}

	static
	{
		_log = LoggerFactory.getLogger(Clan.class);
		REPUTATION_COMPARATOR = new ClanReputationComparator();
	}

	public class RankPrivs
	{
		private int _rank;
		private int _party;
		private int _privs;

		public RankPrivs(final int rank, final int party, final int privs)
		{
			_rank = rank;
			_party = party;
			_privs = privs;
		}

		public int getRank()
		{
			return _rank;
		}

		public int getParty()
		{
			return _party;
		}

		public void setParty(final int party)
		{
			_party = party;
		}

		public int getPrivs()
		{
			return _privs;
		}

		public void setPrivs(final int privs)
		{
			_privs = privs;
		}
	}

	public class SubPledge
	{
		private int _type;
		private int _leaderId;
		private String _name;

		public SubPledge(final int type, final int leaderId, final String name)
		{
			_type = type;
			_leaderId = leaderId;
			_name = name;
		}

		public int getLeaderId()
		{
			return _leaderId;
		}

		public String getLeaderName()
		{
			for(ClanMember member : _members.valueCollection())
				if(member.getObjectId() == _leaderId)
					return member.getName();
			return "";
		}

		public String getName()
		{
			return _name;
		}

		public int getType()
		{
			return _type;
		}

		public void setLeaderId(final int leaderId)
		{
			_leaderId = leaderId;
			Connection con = null;
			PreparedStatement statement = null;
			try
			{
				con = DatabaseFactory.getInstance().getConnection();
				statement = con.prepareStatement("UPDATE clan_subpledges SET leader_id=? WHERE clan_id=? and type=?");
				statement.setInt(1, _leaderId);
				statement.setInt(2, getClanId());
				statement.setInt(3, _type);
				statement.execute();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			finally
			{
				DbUtils.closeQuietly(con, statement);
			}
		}
	}

	private static class ClanReputationComparator implements Comparator<Clan>
	{
		@Override
		public int compare(final Clan o1, final Clan o2)
		{
			if(o1 == null || o2 == null)
				return 0;
			return o2.getReputationScore() - o1.getReputationScore();
		}
	}
}
