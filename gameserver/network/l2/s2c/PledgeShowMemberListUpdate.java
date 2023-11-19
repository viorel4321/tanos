package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.ClanMember;

public class PledgeShowMemberListUpdate extends L2GameServerPacket
{
	private String _name;
	private int _lvl;
	private int _classId;
	private int _race;
	private int _sex;
	private boolean _isOnline;
	private int _objectId;
	private int _pledgeType;
	private int _isApprentice;

	public PledgeShowMemberListUpdate(final Player player)
	{
		_isApprentice = 0;
		_name = player.getName();
		_lvl = player.getLevel();
		_classId = player.getClassId().getId();
		_race = player.getRace().ordinal();
		_sex = player.getSex();
		_objectId = player.getObjectId();
		_isOnline = player.isOnline();
		_pledgeType = player.getPledgeType();
		if(player.getClan() != null && player.getClan().getClanMember(Integer.valueOf(_objectId)) != null)
			_isApprentice = player.getClan().getClanMember(Integer.valueOf(_objectId)).hasSponsor() ? 1 : 0;
	}

	public PledgeShowMemberListUpdate(final ClanMember cm)
	{
		_isApprentice = 0;
		_name = cm.getName();
		_lvl = cm.getLevel();
		_classId = cm.getClassId();
		_race = cm.getRace();
		_sex = cm.getSex();
		_objectId = cm.getObjectId();
		_isOnline = cm.isOnline();
		_pledgeType = cm.getPledgeType();
		_isApprentice = cm.hasSponsor() ? 1 : 0;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(84);
		writeS((CharSequence) _name);
		writeD(_lvl);
		writeD(_classId);
		writeD(_sex);
		writeD(_race);
		writeD(_isOnline ? _objectId : 0);
		writeD(_pledgeType);
		writeD(_isApprentice);
	}
}
