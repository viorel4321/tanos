package l2s.gameserver.skills;

import l2s.gameserver.model.Skill;

public class TimeStamp
{
	private final int _id;
	private final int _level;
	private final long _reuse;
	private final long _endTime;
	private final int _group;

	public TimeStamp(final int id, final long endTime, final long reuse, final int group)
	{
		_id = id;
		_level = 0;
		_reuse = reuse;
		_endTime = endTime;
		_group = group;
	}

	public TimeStamp(final Skill skill, final long reuse)
	{
		this(skill, System.currentTimeMillis() + reuse, reuse);
	}

	public TimeStamp(final Skill skill, final long endTime, final long reuse)
	{
		_id = skill.getId();
		_level = skill.getLevel();
		_reuse = reuse;
		_endTime = endTime;
		_group = skill.getReuseGroupId();
	}

	public long getReuseBasic()
	{
		if(_reuse == 0L)
			return getReuseCurrent();
		return _reuse;
	}

	public long getReuseCurrent()
	{
		return Math.max(_endTime - System.currentTimeMillis(), 0L);
	}

	public long getEndTime()
	{
		return _endTime;
	}

	public boolean hasNotPassed()
	{
		return System.currentTimeMillis() < _endTime;
	}

	public int getId()
	{
		return _id;
	}

	public int getLevel()
	{
		return _level;
	}

	public int getGroup()
	{
		return _group;
	}
}
