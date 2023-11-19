package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;

public class ManagePledgePower extends L2GameServerPacket
{
	private int _action;
	private int _clanId;
	private int privs;

	public ManagePledgePower(final Player player, final int action, final int rank)
	{
		_clanId = player.getClanId();
		_action = action;
		final Clan.RankPrivs temp = player.getClan().getRankPrivs(rank);
		privs = temp == null ? 0 : temp.getPrivs();
		player.sendPacket(new PledgeReceiveUpdatePower(privs));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(48);
		writeD(_clanId);
		writeD(_action);
		writeD(privs);
	}
}
