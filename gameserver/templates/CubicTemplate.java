package l2s.gameserver.templates;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gnu.trove.map.hash.TIntIntHashMap;
import l2s.gameserver.model.Skill;

public class CubicTemplate
{
	private final int _id;
	private final int _level;
	private final int _delay;
	private List<Map.Entry<Integer, List<SkillInfo>>> _skills;

	public CubicTemplate(final int id, final int level, final int delay)
	{
		_skills = new ArrayList<Map.Entry<Integer, List<SkillInfo>>>(3);
		_id = id;
		_level = level;
		_delay = delay;
	}

	public void putSkills(final int chance, final List<SkillInfo> skill)
	{
		_skills.add(new AbstractMap.SimpleImmutableEntry<Integer, List<SkillInfo>>(chance, skill));
	}

	public Iterable<Map.Entry<Integer, List<SkillInfo>>> getSkills()
	{
		return _skills;
	}

	public int getDelay()
	{
		return _delay;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public static class SkillInfo
	{
		private final Skill _skill;
		private final int _chance;
		private final ActionType _actionType;
		private final boolean _canAttackDoor;
		private final TIntIntHashMap _chanceList;

		public SkillInfo(final Skill skill, final int chance, final ActionType actionType, final boolean canAttackDoor, final TIntIntHashMap set)
		{
			_skill = skill;
			_chance = chance;
			_actionType = actionType;
			_canAttackDoor = canAttackDoor;
			_chanceList = set;
		}

		public int getChance()
		{
			return _chance;
		}

		public ActionType getActionType()
		{
			return _actionType;
		}

		public Skill getSkill()
		{
			return _skill;
		}

		public boolean isCanAttackDoor()
		{
			return _canAttackDoor;
		}

		public int getChance(final int a)
		{
			return _chanceList.get(a);
		}
	}

	public enum ActionType
	{
		ATTACK,
		DEBUFF,
		CANCEL,
		HEAL;
	}
}
