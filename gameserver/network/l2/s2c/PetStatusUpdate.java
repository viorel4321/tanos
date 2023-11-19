package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Servitor;
import l2s.gameserver.utils.Location;

public class PetStatusUpdate extends L2GameServerPacket
{
	private int type;
	private int obj_id;
	private int level;
	private int maxFed;
	private int curFed;
	private int maxHp;
	private int curHp;
	private int maxMp;
	private int curMp;
	private long exp;
	private long exp_this_lvl;
	private long exp_next_lvl;
	private Location _loc;
	private String title;

	public PetStatusUpdate(final Servitor summon)
	{
		type = summon.getSummonType();
		obj_id = summon.getObjectId();
		_loc = summon.getLoc();
		title = summon.getVisibleName(summon.getPlayer());
		curHp = (int) summon.getCurrentHp();
		maxHp = summon.getMaxHp();
		curMp = (int) summon.getCurrentMp();
		maxMp = summon.getMaxMp();
		curFed = summon.getCurrentFed();
		maxFed = summon.getMaxFed();
		level = summon.getLevel();
		exp = summon.getExp();
		exp_this_lvl = summon.getExpForThisLevel();
		exp_next_lvl = summon.getExpForNextLevel();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(181);
		writeD(type);
		writeD(obj_id);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
		writeS(title);
		writeD(curFed);
		writeD(maxFed);
		writeD(curHp);
		writeD(maxHp);
		writeD(curMp);
		writeD(maxMp);
		writeD(level);
		writeQ(exp);
		writeQ(exp_this_lvl);
		writeQ(exp_next_lvl);
	}
}
