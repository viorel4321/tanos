package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;

public class PledgePowerGradeList extends L2GameServerPacket
{
	private final Clan.RankPrivs[] _privs;

	public PledgePowerGradeList(final Clan.RankPrivs[] privs)
	{
		_privs = privs;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(59);
		writeD(_privs.length);
		for(final Clan.RankPrivs element : _privs)
		{
			writeD(element.getRank());
			writeD(element.getParty());
		}
	}
}
