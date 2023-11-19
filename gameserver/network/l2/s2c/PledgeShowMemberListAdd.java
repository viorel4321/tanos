package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.ClanMember;

public class PledgeShowMemberListAdd extends L2GameServerPacket
{
	private String member_name;
	private int member_level;
	private int member_class_id;
	private int member_online;
	private int member_PledgeType;
	private int member_sex;
	private int member_race;

	public PledgeShowMemberListAdd(final ClanMember member)
	{
		member_name = member.getName();
		member_level = member.getLevel();
		member_class_id = member.getClassId();
		member_online = member.isOnline() ? member.getObjectId() : 0;
		member_PledgeType = member.getPledgeType();
		member_sex = member.getSex();
		member_race = member.getRace();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(85);
		writeS((CharSequence) member_name);
		writeD(member_level);
		writeD(member_class_id);
		writeD(member_sex);
		writeD(member_race);
		writeD(member_online);
		writeD(member_PledgeType);
	}
}
