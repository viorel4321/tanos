package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class PartySmallWindowAdd extends L2GameServerPacket
{
	private int member_obj_id;
	private int member_level;
	private int member_class_id;
	private int member_curHp;
	private int member_maxHp;
	private int member_curCp;
	private int member_maxCp;
	private int member_curMp;
	private int member_maxMp;
	private String member_name;
	private final int _leaderId;
	private final int _distribution;

	public PartySmallWindowAdd(final Player member, final int leaderId, final int distribution)
	{
		_leaderId = leaderId;
		_distribution = distribution;
		member_obj_id = member.getObjectId();
		member_name = member.getName();
		member_curCp = (int) member.getCurrentCp();
		member_maxCp = member.getMaxCp();
		member_curHp = (int) member.getCurrentHp();
		member_maxHp = member.getMaxHp();
		member_curMp = (int) member.getCurrentMp();
		member_maxMp = member.getMaxMp();
		member_level = member.getLevel();
		member_class_id = member.getClassId().getId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(79);
		writeD(_leaderId);
		writeD(_distribution);
		writeD(member_obj_id);
		writeS((CharSequence) member_name);
		writeD(member_curCp);
		writeD(member_maxCp);
		writeD(member_curHp);
		writeD(member_maxHp);
		writeD(member_curMp);
		writeD(member_maxMp);
		writeD(member_level);
		writeD(member_class_id);
		writeD(0);
		writeD(0);
	}
}
