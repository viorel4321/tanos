package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExDuelUpdateUserInfo extends L2GameServerPacket
{
	private String _name;
	private int obj_id;
	private int class_id;
	private int level;
	private int curHp;
	private int maxHp;
	private int curMp;
	private int maxMp;
	private int curCp;
	private int maxCp;

	public ExDuelUpdateUserInfo(final Player attacker)
	{
		_name = attacker.getName();
		obj_id = attacker.getObjectId();
		class_id = attacker.getClassId().getId();
		level = attacker.getLevel();
		curHp = (int) attacker.getCurrentHp();
		maxHp = attacker.getMaxHp();
		curMp = (int) attacker.getCurrentMp();
		maxMp = attacker.getMaxMp();
		curCp = (int) attacker.getCurrentCp();
		maxCp = attacker.getMaxCp();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(79);
		writeS((CharSequence) _name);
		writeD(obj_id);
		writeD(class_id);
		writeD(level);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(curCp);
		writeD(maxCp);
	}
}
