package l2s.gameserver.network.l2.s2c;

public class PledgeSkillListAdd extends L2GameServerPacket
{
	private static int _skillId;
	private static int _skillLevel;

	public PledgeSkillListAdd(final int skillId, final int skillLevel)
	{
		PledgeSkillListAdd._skillId = skillId;
		PledgeSkillListAdd._skillLevel = skillLevel;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(58);
		writeD(PledgeSkillListAdd._skillId);
		writeD(PledgeSkillListAdd._skillLevel);
	}
}
