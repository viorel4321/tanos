package l2s.gameserver.instancemanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.napile.primitive.maps.IntObjectMap;
import org.napile.primitive.maps.impl.CHashIntObjectMap;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;

public class PartyRoomManager
{
	private static final PartyRoomManager _instance;
	private RoomsHolder _holder;
	private Set<Player> _players;

	public static PartyRoomManager getInstance()
	{
		return PartyRoomManager._instance;
	}

	public PartyRoomManager()
	{
		_holder = new RoomsHolder();
		_players = new CopyOnWriteArraySet<Player>();
		_holder = new RoomsHolder();
	}

	public void addToWaitingList(final Player player)
	{
		_players.add(player);
	}

	public void removeFromWaitingList(final Player player)
	{
		_players.remove(player);
	}

	public List<Player> getWaitingList(final int minLevel, final int maxLevel, final boolean showAll)
	{
		final List<Player> res = new ArrayList<Player>();
		for(final Player $member : _players)
			if(showAll || $member.getLevel() >= minLevel && $member.getLevel() <= maxLevel)
				res.add($member);
		return res;
	}

	public List<PartyRoom> getMatchingRooms(final int region, final boolean allLevels, final Player activeChar)
	{
		final List<PartyRoom> res = new ArrayList<PartyRoom>();
		for(final PartyRoom room : _holder._rooms.valueCollection())
		{
			if(region > 0 && room.getLocationId() != region)
				continue;
			if(region == -2 && room.getLocationId() != getInstance().getLocation(activeChar))
				continue;
			if(!allLevels)
			{
				if(room.getMinLevel() > activeChar.getLevel())
					continue;
				if(room.getMaxLevel() < activeChar.getLevel())
					continue;
			}
			res.add(room);
		}
		return res;
	}

	public int addMatchingRoom(final PartyRoom r)
	{
		return _holder.addRoom(r);
	}

	public void removeMatchingRoom(final PartyRoom r)
	{
		_holder._rooms.remove(r.getId());
	}

	public PartyRoom getMatchingRoom(final int id)
	{
		return _holder._rooms.get(id);
	}

	public int getLocation(final Player player)
	{
		if(player == null)
			return 0;
		int loc = 0;
		final int town = TownManager.getInstance().getClosestTownNumber(player);
		switch(town)
		{
			case 1:
			{
				loc = 1;
				break;
			}
			case 2:
			{
				loc = 4;
				break;
			}
			case 3:
			{
				loc = 3;
				break;
			}
			case 6:
			{
				loc = 2;
				break;
			}
			case 8:
			{
				loc = 5;
				break;
			}
			case 9:
			{
				loc = 6;
				break;
			}
			case 10:
			{
				loc = 10;
				break;
			}
			case 11:
			{
				loc = 13;
				break;
			}
			case 12:
			{
				loc = 11;
				break;
			}
			case 14:
			{
				loc = 14;
				break;
			}
			case 15:
			{
				loc = 15;
				break;
			}
			case 16:
			{
				loc = 9;
				break;
			}
			case 4:
			case 5:
			case 7:
			case 13:
			case 18:
			{
				loc = 7;
				break;
			}
		}
		return loc;
	}

	static
	{
		_instance = new PartyRoomManager();
	}

	private class RoomsHolder
	{
		private int _id;
		private IntObjectMap<PartyRoom> _rooms;

		private RoomsHolder()
		{
			_id = 1;
			_rooms = new CHashIntObjectMap<PartyRoom>();
		}

		public int addRoom(final PartyRoom r)
		{
			final int val = _id++;
			_rooms.put(val, r);
			return val;
		}
	}
}
