package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;

public class ExEnchantSkillList extends L2GameServerPacket
{
	private final ArrayList<Skill> _skills;

	public ExEnchantSkillList()
	{
		_skills = new ArrayList<Skill>();
	}

	public void addSkill(final int id, final int level, final int exp, final long sp)
	{
		_skills.add(new Skill(id, level, exp, sp));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(23);
		writeD(_skills.size());
		for(final Skill sk : _skills)
		{
			writeD(sk._id);
			writeD(sk._nextLevel);
			writeD(sk._sp);
			writeQ(sk._exp);
		}
	}

	private class Skill
	{
		public int _id;
		public int _nextLevel;
		public long _exp;
		public int _sp;

		Skill(final int id, final int nextLevel, final int sp, final long exp)
		{
			_id = id;
			_nextLevel = nextLevel;
			_exp = exp;
			_sp = sp;
		}
	}
}
