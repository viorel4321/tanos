package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.skills.TimeStamp;

public class SkillCoolTime extends L2GameServerPacket
{
	private List<SkillInfo> _list;

	public SkillCoolTime(final Player player)
	{
		_list = Collections.emptyList();
		final Collection<TimeStamp> list = player.getSkillReuses();
		_list = new ArrayList<SkillInfo>(list.size());
		for(final TimeStamp stamp : list)
		{
			if(!stamp.hasNotPassed())
				continue;
			final int reuseCurrent = (int) Math.round(stamp.getReuseCurrent() / 1000.0);
			if(reuseCurrent < 1)
				continue;
			if(stamp.getGroup() > 0)
				for(final Skill gsk : player.getGSkills(stamp.getGroup()))
				{
					final SkillInfo sk = new SkillInfo();
					sk.skillId = gsk.getId();
					sk.level = gsk.getLevel();
					sk.reuseBase = (int) Math.round(stamp.getReuseBasic() / 1000.0);
					sk.reuseCurrent = reuseCurrent;
					_list.add(sk);
				}
			else
			{
				final Skill skill = player.getKnownSkill(stamp.getId());
				if(skill == null)
					continue;
				final SkillInfo sk2 = new SkillInfo();
				sk2.skillId = skill.getId();
				sk2.level = skill.getLevel();
				sk2.reuseBase = (int) Math.round(stamp.getReuseBasic() / 1000.0);
				sk2.reuseCurrent = reuseCurrent;
				_list.add(sk2);
			}
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(193);
		writeD(_list.size());
		for(int i = 0; i < _list.size(); ++i)
		{
			final SkillInfo sk = _list.get(i);
			writeD(sk.skillId);
			writeD(sk.level);
			writeD(sk.reuseBase);
			writeD(sk.reuseCurrent);
		}
	}

	private static class SkillInfo
	{
		public int skillId;
		public int level;
		public int reuseBase;
		public int reuseCurrent;
	}
}
