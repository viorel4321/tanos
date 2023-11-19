package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Creature;

public class ExFishingHpRegen extends L2GameServerPacket
{
	private int _time;
	private int _fishHP;
	private int _HPmode;
	private int _Anim;
	private int _GoodUse;
	private int _Penalty;
	private int _hpBarColor;
	private int char_obj_id;

	public ExFishingHpRegen(final Creature character, final int time, final int fishHP, final int HPmode, final int GoodUse, final int anim, final int penalty, final int hpBarColor)
	{
		char_obj_id = character.getObjectId();
		_time = time;
		_fishHP = fishHP;
		_HPmode = HPmode;
		_GoodUse = GoodUse;
		_Anim = anim;
		_Penalty = penalty;
		_hpBarColor = hpBarColor;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(22);
		writeD(char_obj_id);
		writeD(_time);
		writeD(_fishHP);
		writeC(_HPmode);
		writeC(_GoodUse);
		writeC(_Anim);
		writeD(_Penalty);
		writeC(_hpBarColor);
	}
}
