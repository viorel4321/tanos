package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;

public class PledgeStatusChanged extends L2GameServerPacket
{
	private int leader_id;
	private int clan_id;
	private int level;

	public PledgeStatusChanged(final Clan clan)
	{
		leader_id = clan.getLeaderId();
		clan_id = clan.getClanId();
		level = clan.getLevel();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(205);
		writeD(leader_id);
		writeD(clan_id);
		writeD(0);
		writeD(level);
		writeD(0);
		writeD(0);
		writeD(0);
	}
}
