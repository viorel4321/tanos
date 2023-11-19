package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class MagicEffectIcons extends L2GameServerPacket
{
	public static final int INFINITIVE_EFFECT = -1;
	private List<Effect> _effects;

	public MagicEffectIcons()
	{
		_effects = new ArrayList<Effect>();
	}

	public void addEffect(final int skillId, final int dat, final int duration)
	{
		_effects.add(new Effect(skillId, dat, duration));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(127);
		writeH(_effects.size());
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
