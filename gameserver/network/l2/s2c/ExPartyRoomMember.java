package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;

public class ExPartyRoomMember extends L2GameServerPacket
{
	private int _type;
	private List<PartyRoomMemberInfo> _members;

	public ExPartyRoomMember(final PartyRoom room, final Player activeChar)
	{
		_members = Collections.emptyList();
		_type = room.getMemberType(activeChar);
		_members = new ArrayList<PartyRoomMemberInfo>(room.getPlayers().size());
		for(final Integer objectId : room.getPlayers())
		{
			final Player $member;
			if(($member = GameObjectsStorage.getPlayer(objectId)) != null)
				_members.add(new PartyRoomMemberInfo($member, room.getMemberType($member)));
		}
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(8);
		writeD(_type);
		writeD(_members.size());
		for(final PartyRoomMemberInfo member_info : _members)
		{
			writeD(member_info.objectId);
			writeS((CharSequence) member_info.name);
			writeD(member_info.classId);
			writeD(member_info.level);
			writeD(member_info.location);
			writeD(member_info.memberType);
		}
	}

	static class PartyRoomMemberInfo
	{
		public final int objectId;
		public final int classId;
		public final int level;
		public final int location;
		public final int memberType;
		public final String name;

		public PartyRoomMemberInfo(final Player member, final int type)
		{
			objectId = member.getObjectId();
			name = member.getName();
			classId = member.getClassId().ordinal();
			level = member.getLevel();
			location = PartyRoomManager.getInstance().getLocation(member);
			memberType = type;
		}
	}
}
