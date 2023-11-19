package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class PartySmallWindowUpdate extends L2GameServerPacket
{
	private int obj_id;
	private int class_id;
	private int level;
	private int curCp;
	private int maxCp;
	private int curHp;
	private int maxHp;
	private int curMp;
	private int maxMp;
	private String obj_name;

	public PartySmallWindowUpdate(final Player member)
	{
		obj_id = member.getObjectId();
		obj_name = member.getName();
		curCp = (int) member.getCurrentCp();
		maxCp = member.getMaxCp();
		curHp = (int) member.getCurrentHp();
		maxHp = member.getMaxHp();
		curMp = (int) member.getCurrentMp();
		maxMp = member.getMaxMp();
		level = member.getLevel();
		class_id = member.getClassId().getId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(82);
		writeD(obj_id);
		writeS((CharSequence) obj_name);
		writeD(curCp);
		writeD(maxCp);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
		writeD(class_id);
	}
}
