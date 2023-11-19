package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;

public class PledgeShowMemberListAll extends L2GameServerPacket
{
	private Clan _clan;
	private int clan_id;
	private int clan_crest_id;
	private int level;
	private int rank;
	private int rep;
	private int ally_id;
	private int ally_crest_id;
	private int _pledgeType;
	private int HasCastle;
	private int HasHideout;
	private int AtWar;
	private String clan_name;
	private String leader_name;
	private String ally_name;
	private List<ClanMemberInfo> infos;

	public PledgeShowMemberListAll(final Clan clan, final Player activeChar)
	{
		infos = new ArrayList<ClanMemberInfo>();
		_clan = clan;
		_pledgeType = 0;
		clan_id = _clan.getClanId();
		clan_name = _clan.getName();
		leader_name = _clan.getLeaderName();
		clan_crest_id = _clan.getCrestId();
		level = _clan.getLevel();
		HasCastle = _clan.getHasCastle();
		HasHideout = _clan.getHasHideout();
		rank = _clan.getRank();
		rep = _clan.getReputationScore();
		ally_id = _clan.getAllyId();
		if(_clan.getAlliance() != null)
		{
			ally_name = _clan.getAlliance().getAllyName();
			ally_crest_id = _clan.getAlliance().getAllyCrestId();
		}
		else
		{
			ally_name = "";
			ally_crest_id = 0;
		}
		AtWar = _clan.isAtWarOrUnderAttack();
		for(final Clan.SubPledge element : _clan.getAllSubPledges())
			activeChar.sendPacket(new PledgeReceiveSubPledgeCreated(element));
		for(final ClanMember m : _clan.getMembers())
			if(m.getPledgeType() == _pledgeType)
				infos.add(new ClanMemberInfo(m.getName(), m.getLevel(), m.getClassId(), m.getSex(), m.getObjectId(), m.isOnline(), m.hasSponsor() ? 1 : 0, m.getRace()));
			else
				activeChar.sendPacket(new PledgeShowMemberListAdd(m));
		activeChar.sendPacket(new UserInfo(activeChar));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(83);
		writeD(_pledgeType != 0 ? 1 : 0);
		writeD(clan_id);
		writeD(_pledgeType);
		writeS(clan_name);
		writeS(leader_name);
		writeD(clan_crest_id);
		writeD(level);
		writeD(HasCastle);
		writeD(HasHideout);
		writeD(rank);
		writeD(rep);
		writeD(0);
		writeD(0);
		writeD(ally_id);
		writeS(ally_name);
		writeD(ally_crest_id);
		writeD(AtWar);
		writeD(infos.size());
		for(final ClanMemberInfo _info : infos)
		{
			writeS(_info._name);
			writeD(_info.level);
			writeD(_info.class_id);
			writeD(_info.sex);
			writeD(_info.race);
			writeD(_info.online ? _info.obj_id : 0);
			writeD(_info.has_sponsor);
		}
		infos.clear();
	}

	static class ClanMemberInfo
	{
		public String _name;
		public int level;
		public int class_id;
		public int sex;
		public int obj_id;
		public int has_sponsor;
		public int race;
		public boolean online;

		public ClanMemberInfo(final String __name, final int _level, final int _class_id, final int _sex, final int _obj_id, final boolean _online, final int _has_sponsor, final int _race)
		{
			_name = __name;
			level = _level;
			class_id = _class_id;
			race = _race;
			sex = _sex;
			obj_id = _obj_id;
			online = _online;
			has_sponsor = _has_sponsor;
		}
	}
}
