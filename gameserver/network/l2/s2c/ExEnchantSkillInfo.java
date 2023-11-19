package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public class ExEnchantSkillInfo extends L2GameServerPacket
{
	private List<EnchantSkill> _skill;
	private int _id;
	private int _level;
	private int _spCost;
	private long _xpCost;
	private int _rate;

	public ExEnchantSkillInfo(final int id, final int level, final int spCost, final long xpCost, final int rate)
	{
		_skill = new ArrayList<EnchantSkill>();
		_id = id;
		_level = level;
		_spCost = spCost;
		_xpCost = xpCost;
		_rate = rate;
	}

	public void addRequirement(final int type, final int id, final int count, final int unk)
	{
		_skill.add(new EnchantSkill(type, id, count, unk));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(24);
		writeD(_id);
		writeD(_level);
		writeD(_spCost);
		writeQ(_xpCost);
		writeD(_rate);
		writeD(_skill.size());
		for(final EnchantSkill temp : _skill)
		{
			writeD(temp._type);
			writeD(temp._id);
			writeD(temp._count);
			writeD(temp._unk);
		}
	}

	private class EnchantSkill
	{
		public int _id;
		public int _count;
		public int _type;
		public int _unk;

		EnchantSkill(final int type, final int id, final int count, final int unk)
		{
			_id = id;
			_type = type;
			_count = count;
			_unk = unk;
		}
	}
}
