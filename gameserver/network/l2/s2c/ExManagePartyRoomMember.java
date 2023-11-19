package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.PartyRoom;

public class ExManagePartyRoomMember extends L2GameServerPacket
{
	public static final int ADDED = 0;
	public static final int MODIFIED = 1;
	public static final int REMOVED = 2;
	private int _type;
	private PartyRoomMemberInfo member_info;

	public ExManagePartyRoomMember(final int changeType, final PartyRoom room, final Player activeChar)
	{
		_type = changeType;
		member_info = new PartyRoomMemberInfo(activeChar, room.getMemberType(activeChar));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(16);
		writeD(_type);
		writeD(member_info.objectId);
		writeS((CharSequence) member_info.name);
		writeD(member_info.classId);
		writeD(member_info.level);
		writeD(member_info.location);
		writeD(member_info.memberType);
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
