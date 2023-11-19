package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;

public class ExOlympiadSpelledInfo extends L2GameServerPacket
{
	private int char_obj_id;
	private List<Effect> _effects;

	public ExOlympiadSpelledInfo()
	{
		char_obj_id = 0;
		_effects = new ArrayList<Effect>();
	}

	public void addEffect(final int skillId, final int dat, final int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}

	public void addSpellRecivedPlayer(final Player cha)
	{
		if(cha != null)
			char_obj_id = cha.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		if(char_obj_id == 0)
			return;
		writeC(254);
		writeH(42);
		writeD(char_obj_id);
		writeD(_effects.size());
		for(final Effect temp : _effects)
		{
			writeD(temp.skillId);
			writeH(temp.dat);
			writeD(temp.duration);
		}
	}

	class Effect
	{
		int skillId;
		int dat;
		int duration;

		public Effect(final int skillId, final int dat, final int duration)
		{
			this.skillId = skillId;
			this.dat = dat;
			this.duration = duration;
		}
	}
}
