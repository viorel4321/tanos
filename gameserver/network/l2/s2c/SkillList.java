package l2s.gameserver.network.l2.s2c;

import java.util.Arrays;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.Skill;
import l2s.gameserver.utils.SkillsComparator;

public class SkillList extends L2GameServerPacket
{
	private Skill[] _skills;
	private Player activeChar;

	public SkillList(final Player p)
	{
		Arrays.sort(_skills = p.getAllSkillsArray(), SkillsComparator.getInstance());
		activeChar = p;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(88);
		writeD(_skills.length);
		for(final Skill temp : _skills)
		{
			writeD(!temp.isActive() && !temp.isToggle() ? 1 : 0);
			writeD(temp.getDisplayLevel());
			writeD(temp.getId());
			writeC(activeChar.isUnActiveSkill(temp.getId()) ? 1 : 0);
		}
	}
}
