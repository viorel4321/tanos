package l2s.gameserver.network.l2.s2c;

import java.util.Collection;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;

public class GMViewSkillInfo extends L2GameServerPacket
{
	private String char_name;
	private Collection<Skill> _skills;
	private Player _targetChar;

	public GMViewSkillInfo(final Player cha)
	{
		char_name = cha.getName();
		_skills = cha.getAllSkills();
		_targetChar = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(145);
		writeS(char_name);
		writeD(_skills.size());
		for(final Skill skill : _skills)
		{
			writeD(skill.isPassive() ? 1 : 0);
			writeD(skill.getDisplayLevel());
			writeD(skill.getId());
			writeC(_targetChar.isUnActiveSkill(skill.getId()) ? 1 : 0);
		}
	}
}
