package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class MagicSkillUse extends L2GameServerPacket
{
	private int _targetId;
	private int _skillId;
	private int _skillLevel;
	private int _hitTime;
	private long _reuseDelay;
	private int _chaId;
	private int _x;
	private int _y;
	private int _z;
	private int _targetx;
	private int _targety;
	private int _targetz;

	public MagicSkillUse(final Creature cha, final Creature target, final int skillId, final int skillLevel, final int hitTime, final long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = target.getObjectId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = target.getX();
		_targety = target.getY();
		_targetz = target.getZ();
	}

	public MagicSkillUse(final Creature cha, final int skillId, final int skillLevel, final int hitTime, final long reuseDelay)
	{
		_chaId = cha.getObjectId();
		_targetId = cha.getTargetId();
		_skillId = skillId;
		_skillLevel = skillLevel;
		_hitTime = hitTime;
		_reuseDelay = reuseDelay;
		_x = cha.getX();
		_y = cha.getY();
		_z = cha.getZ();
		_targetx = cha.getX();
		_targety = cha.getY();
		_targetz = cha.getZ();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(72);
		writeD(_chaId);
		writeD(_targetId);
		writeD(_skillId);
		writeD(_skillLevel);
		writeD(_hitTime);
		writeD((int) _reuseDelay);
		writeD(_x);
		writeD(_y);
		writeD(_z);
		writeD(0);
		writeD(_targetx);
		writeD(_targety);
		writeD(_targetz);
	}
}
