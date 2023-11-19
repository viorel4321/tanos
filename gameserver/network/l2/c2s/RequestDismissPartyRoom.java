package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;

public class RequestDismissPartyRoom extends L2GameClientPacket
{
	private int _roomId;

	@Override
	protected void readImpl()
	{
		_roomId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final PartyRoom room = player.getPartyRoom();
		if(room == null || room.getId() != _roomId)
			return;
		if(room.getLeader() != player)
			return;
		room.disband();
	}
}
