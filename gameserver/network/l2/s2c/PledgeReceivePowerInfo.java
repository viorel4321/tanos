package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.model.pledge.ClanMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket
{
	private int PowerGrade;
	private int privs;
	private String member_name;

	public PledgeReceivePowerInfo(final ClanMember member)
	{
		PowerGrade = member.getPowerGrade();
		member_name = member.getName();
		if(member.isClanLeader())
			privs = 8388606;
		else
		{
			final Clan.RankPrivs temp = member.getClan().getRankPrivs(member.getPowerGrade());
			if(temp != null)
				privs = temp.getPrivs();
			else
				privs = 0;
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(60);
		writeD(PowerGrade);
		writeS((CharSequence) member_name);
		writeD(privs);
	}
}
