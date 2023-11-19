package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;

public class PledgeInfo extends L2GameServerPacket
{
	private int clan_id;
	private String clan_name;
	private String ally_name;

	public PledgeInfo(final Clan clan)
	{
		clan_id = clan.getClanId();
		clan_name = clan.getName();
		ally_name = clan.getAllyName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(131);
		writeD(clan_id);
		writeS((CharSequence) clan_name);
		writeS((CharSequence) ally_name);
	}
}
