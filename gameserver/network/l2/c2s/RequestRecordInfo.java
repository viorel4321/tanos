package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.WorldRegion;

public class RequestRecordInfo extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		player.sendUserInfo(true);
		if(player.getCurrentRegion() != null)
			for(final WorldRegion neighbor : player.getCurrentRegion().getNeighbors())
				neighbor.showObjectsToPlayer(player);
	}
}
