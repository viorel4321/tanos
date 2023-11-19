package l2s.gameserver.utils;

import java.util.Comparator;

import l2s.gameserver.model.Skill;

public class SkillsComparator implements Comparator<Skill>
{
	private static final SkillsComparator instance;

	public static final SkillsComparator getInstance()
	{
		return SkillsComparator.instance;
	}

	@Override
	public int compare(final Skill s1, final Skill s2)
	{
		if(s1 == null || s2 == null)
			return 0;
		final boolean toggle1 = s1.isToggle();
		final boolean toggle2 = s2.isToggle();
		if(toggle1 && toggle2)
			return compareId(s1.getId(), s2.getId());
		if(!toggle1 && !toggle2)
			return compareId(s1.getId(), s2.getId());
		if(toggle1)
			return 1;
		return -1;
	}

	private int compareId(final int id1, final int id2)
	{
		if(id1 > id2)
			return 1;
		if(id1 < id2)
			return -1;
		return 0;
	}

	static
	{
		instance = new SkillsComparator();
	}
}
