package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.PartyRoom;

public class PartyRoomInfo extends L2GameServerPacket
{
	private int _id;
	private int _minLevel;
	private int _maxLevel;
	private int _lootDist;
	private int _maxMembers;
	private int _location;
	private String _title;

	public PartyRoomInfo(final PartyRoom room)
	{
		_id = room.getId();
		_minLevel = room.getMinLevel();
		_maxLevel = room.getMaxLevel();
		_lootDist = room.getLootType();
		_maxMembers = room.getMaxMembersSize();
		_location = room.getLocationId();
		_title = room.getTopic();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(151);
		writeD(_id);
		writeD(_maxMembers);
		writeD(_minLevel);
		writeD(_maxLevel);
		writeD(_lootDist);
		writeD(_location);
		writeS((CharSequence) _title);
	}
}
