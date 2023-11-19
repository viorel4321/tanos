package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import l2s.gameserver.model.Creature;
import l2s.gameserver.model.actor.instances.creature.Abnormal;
import l2s.gameserver.utils.EffectsComparator;

public class PartySpelled extends L2GameServerPacket
{
	private int _type;
	private int _objId;
	private List<Effect> _effects;

	public PartySpelled(final Creature activeChar, final boolean full)
	{
		if(activeChar == null)
			return;
		_objId = activeChar.getObjectId();
		_type = activeChar.isPet() ? 1 : activeChar.isSummon() ? 2 : 0;
		_effects = new ArrayList<Effect>();
		if(full)
		{
			final Abnormal[] abnormals = activeChar.getAbnormalList().getAllFirstEffects();
			Arrays.sort(abnormals, EffectsComparator.getInstance());
			for(final Abnormal abnormal : abnormals)
				if(abnormal != null && abnormal.isInUse())
					abnormal.addPartySpelledIcon(this);
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(_objId == 0)
			return;
		writeC(238);
		writeD(_type);
		writeD(_objId);
		writeD(_effects.size());
		for(final Effect temp : _effects)
		{
			writeD(temp._skillId);
			writeH(temp._level);
			writeD(temp._duration);
		}
	}

	public void addPartySpelledEffect(final int skillId, final int level, final int duration)
	{
		_effects.add(new Effect(skillId, level, duration));
	}

	static class Effect
	{
		final int _skillId;
		final int _level;
		final int _duration;

		public Effect(final int skillId, final int level, final int duration)
		{
			_skillId = skillId;
			_level = level;
			_duration = duration;
		}
	}
}
