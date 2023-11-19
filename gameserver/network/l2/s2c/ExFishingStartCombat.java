package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class ExFishingStartCombat extends L2GameServerPacket
{
	int _time;
	int _hp;
	int _lureType;
	int _deceptiveMode;
	int _mode;
	private int char_obj_id;

	public ExFishingStartCombat(final Creature character, final int time, final int hp, final int mode, final int lureType, final int deceptiveMode)
	{
		char_obj_id = character.getObjectId();
		_time = time;
		_hp = hp;
		_mode = mode;
		_lureType = lureType;
		_deceptiveMode = deceptiveMode;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(21);
		writeD(char_obj_id);
		writeD(_time);
		writeD(_hp);
		writeC(_mode);
		writeC(_lureType);
		writeC(_deceptiveMode);
	}
}
