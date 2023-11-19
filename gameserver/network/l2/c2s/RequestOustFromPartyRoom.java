package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;

public class RequestOustFromPartyRoom extends L2GameClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		_objectId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		final PartyRoom room = player.getPartyRoom();
		if(room == null)
			return;
		if(room.getLeader() != player)
			return;
		final Player member = GameObjectsStorage.getPlayer(_objectId);
		if(member == null)
			return;
		if(member == room.getLeader())
			return;
		room.removeMember(member, true);
	}
}
