package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

public final class AcquireSkillList extends L2GameServerPacket
{
	private List<Skill> _skills;
	private SkillType _skillType;

	public AcquireSkillList(final SkillType type)
	{
		_skillType = type;
	}

	public void addSkill(final int id, final int nextLevel, final int maxLevel, final int spCost, final int requirements)
	{
		if(_skills == null)
			_skills = new ArrayList<Skill>();
		_skills.add(new Skill(id, nextLevel, maxLevel, spCost, requirements));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(138);
		writeD(_skillType.ordinal());
		writeD(_skills.size());
		for(final Skill temp : _skills)
		{
			writeD(temp.id);
			writeD(temp.nextLevel);
			writeD(temp.maxLevel);
			writeD(temp.spCost);
			writeD(temp.requirements);
		}
	}

	private static class Skill
	{
		public int id;
		public int nextLevel;
		public int maxLevel;
		public int spCost;
		public int requirements;

		public Skill(final int pId, final int pNextLevel, final int pMaxLevel, final int pSpCost, final int pRequirements)
		{
			id = pId;
			nextLevel = pNextLevel;
			maxLevel = pMaxLevel;
			spCost = pSpCost;
			requirements = pRequirements;
		}
	}

	public enum SkillType
	{
		Usual,
		Fishing,
		Clan;
	}
}
