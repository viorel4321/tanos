package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Skill;
import l2s.gameserver.model.pledge.Clan;

public class PledgeSkillList extends L2GameServerPacket
{
	private List<SkillInfo> infos;

	public PledgeSkillList(final Clan clan)
	{
		infos = new ArrayList<SkillInfo>();
		for(final Skill sk : clan.getAllSkills())
			infos.add(new SkillInfo(sk.getId(), sk.getLevel()));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(57);
		writeD(infos.size());
		for(final SkillInfo _info : infos)
		{
			writeD(_info._id);
			writeD(_info.level);
		}
		infos.clear();
	}

	static class SkillInfo
	{
		public int _id;
		public int level;

		public SkillInfo(final int __id, final int _level)
		{
			_id = __id;
			level = _level;
		}
	}
}
