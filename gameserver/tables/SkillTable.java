package l2s.gameserver.tables;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.SkillsEngine;

public class SkillTable
{
	private static final TIntObjectHashMap<Skill> _skills;
	private static final TIntIntHashMap _skillMaxLevel;

	public static SkillTable getInstance()
	{
		return SingletonHolder._instance;
	}

	public static int getSkillHashCode(final Skill skill)
	{
		return getSkillHashCode(skill.getId(), skill.getLevel());
	}

	public static int getSkillHashCode(final int skillId, final int skillLevel)
	{
		return skillId * 1000 + skillLevel;
	}

	private SkillTable()
	{
		load();
	}

	public final Skill getInfo(final int skillId, final int level)
	{
		final Skill result = _skills.get(getSkillHashCode(skillId, level));
		if(result != null)
			return result;
		final int maxLvl = _skillMaxLevel.get(skillId);
		if(maxLvl > 0 && level >= maxLvl)
			return _skills.get(getSkillHashCode(skillId, maxLvl));
		return null;
	}

	public final int getMaxLevel(final int skillId)
	{
		return _skillMaxLevel.get(skillId);
	}

	public Skill[] getSiegeSkills(final boolean addNoble)
	{
		final Skill[] temp = new Skill[2 + (addNoble ? 1 : 0)];
		int i = 0;
		temp[i++] = _skills.get(getSkillHashCode(246, 1));
		temp[i++] = _skills.get(getSkillHashCode(247, 1));
		if(addNoble)
			temp[i++] = _skills.get(getSkillHashCode(326, 1));
		return temp;
	}

	private void load()
	{
		_skills.clear();
		_skillMaxLevel.clear();
		SkillsEngine.getInstance().loadAllSkills(_skills);
		for(final Skill skill : _skills.values(new Skill[_skills.size()]))
		{
			skill.setDefMagicLevel();
			final int skillId = skill.getId();
			final int skillLvl = skill.getLevel();
			final int maxLvl = _skillMaxLevel.get(skillId);
			if(skillLvl > maxLvl)
				_skillMaxLevel.put(skillId, skillLvl);
		}
		for(final FrequentSkill sk : FrequentSkill.values())
			sk._skill = getInfo(sk._id, sk._level);
	}

	public void reload()
	{
		load();
	}

	static
	{
		_skills = new TIntObjectHashMap<Skill>();
		_skillMaxLevel = new TIntIntHashMap();
	}

	public enum FrequentSkill
	{
		RAID_CURSE(4215, 1),
		RAID_CURSE2(4515, 1),
		SEAL_OF_RULER(246, 1),
		BUILD_HEADQUARTERS(247, 1),
		LUCKY(194, 1),
		DWARVEN_CRAFT(1321, 1),
		COMMON_CRAFT(1322, 1),
		WYVERN_BREATH(4289, 1),
		STRIDER_SIEGE_ASSAULT(325, 1),
		FAKE_PETRIFICATION(4616, 1),
		FIREWORK(5965, 1),
		LARGE_FIREWORK(2025, 1),
		BLESSING_OF_PROTECTION(5182, 1),
		ARENA_CP_RECOVERY(4380, 1),
		VOID_BURST(3630, 1),
		VOID_FLOW(3631, 1),
		THE_VICTOR_OF_WAR(5074, 1),
		THE_VANQUISHED_OF_WAR(5075, 1),
		SPECIAL_TREE_RECOVERY_BONUS(2139, 1),
		BAIUM_GIFT(4136, 1);

		final int _id;
		final int _level;
		Skill _skill;

		private FrequentSkill(final int id, final int level)
		{
			_skill = null;
			_id = id;
			_level = level;
		}

		public Skill getSkill()
		{
			return _skill;
		}
	}

	private static class SingletonHolder
	{
		protected static final SkillTable _instance = new SkillTable();
	}
}
