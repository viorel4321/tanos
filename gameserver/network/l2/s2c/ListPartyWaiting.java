package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;

public class ListPartyWaiting extends L2GameServerPacket
{
	private Collection<PartyRoom> _rooms;
	private int _fullSize;

	public ListPartyWaiting(final int region, final boolean allLevels, final int page, final Player activeChar)
	{
		final int first = (page - 1) * 64;
		final int firstNot = page * 64;
		_rooms = new ArrayList<PartyRoom>();
		int i = 0;
		final List<PartyRoom> temp = PartyRoomManager.getInstance().getMatchingRooms(region, allLevels, activeChar);
		_fullSize = temp.size();
		for(final PartyRoom room : temp)
			if(i >= first)
			{
				if(i >= firstNot)
					continue;
				_rooms.add(room);
				++i;
			}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(150);
		writeD(_fullSize);
		writeD(_rooms.size());
		for(final PartyRoom room : _rooms)
		{
			writeD(room.getId());
			writeS((CharSequence) (room.getLeader() == null ? "None" : room.getLeader().getName()));
			writeD(room.getLocationId());
			writeD(room.getMinLevel());
			writeD(room.getMaxLevel());
			writeD(room.getMaxMembersSize());
			writeS((CharSequence) room.getTopic());
			final Vector<Integer> players = room.getPlayers();
			writeD(players.size());
			for(final Integer objectId : players)
			{
				final Player player;
				if((player = GameObjectsStorage.getPlayer(objectId)) != null)
				{
					writeD(player.getClassId().getId());
					writeS((CharSequence) player.getName());
				}
			}
		}
	}
}
