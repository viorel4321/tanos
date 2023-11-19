package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.model.Player;

public class RequestExitPartyMatchingWaitingRoom extends L2GameClientPacket
{
	@Override
	protected void readImpl()
	{}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		PartyRoomManager.getInstance().removeFromWaitingList(player);
	}
}
