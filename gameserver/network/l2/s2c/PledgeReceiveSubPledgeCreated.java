package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.pledge.Clan;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket
{
	private int type;
	private String _name;
	private String leader_name;

	public PledgeReceiveSubPledgeCreated(final Clan.SubPledge subPledge)
	{
		type = subPledge.getType();
		_name = subPledge.getName();
		leader_name = subPledge.getLeaderName();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(63);
		writeD(1);
		writeD(type);
		writeS((CharSequence) _name);
		writeS((CharSequence) leader_name);
	}
}
