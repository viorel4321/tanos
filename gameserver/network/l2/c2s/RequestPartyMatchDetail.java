package l2s.gameserver.network.l2.c2s;

import java.nio.BufferUnderflowException;

import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;

public class RequestPartyMatchDetail extends L2GameClientPacket
{
	private int _roomId;
	private int _locations;
	private int _level;
	private boolean _fail;

	@Override
	protected void readImpl()
	{
		try
		{
			_roomId = readD();
			_locations = readD();
			_level = readD();
		}
		catch(BufferUnderflowException e)
		{
			_fail = true;
		}
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		if(_fail)
		{
			player.sendActionFailed();
			return;
		}
		if(player.getPartyRoom() != null)
			return;
		if(_roomId > 0)
		{
			final PartyRoom room = PartyRoomManager.getInstance().getMatchingRoom(_roomId);
			if(room == null)
				return;
			room.addMember(player);
		}
		else
			for(final PartyRoom room2 : PartyRoomManager.getInstance().getMatchingRooms(_locations, _level == 1, player))
				if(room2.addMember(player))
					break;
	}
}
