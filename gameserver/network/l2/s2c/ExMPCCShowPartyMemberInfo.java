package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;

public class ExMPCCShowPartyMemberInfo extends L2GameServerPacket
{
	private List<PartyMemberInfo> members;

	public ExMPCCShowPartyMemberInfo(final Player partyLeader)
	{
		if(!partyLeader.isInParty())
			return;
		final Party _party = partyLeader.getParty();
		if(_party == null)
			return;
		if(!_party.isInCommandChannel())
			return;
		members = new ArrayList<PartyMemberInfo>();
		for(final Player _member : _party.getPartyMembers())
			members.add(new PartyMemberInfo(_member.getName(), _member.getObjectId(), _member.getClassId().getId()));
	}

	@Override
	protected final void writeImpl()
	{
		if(members == null)
			return;
		writeC(254);
		writeH(74);
		writeD(members.size());
		for(final PartyMemberInfo _member : members)
		{
			writeS(_member.name);
			writeD(_member.object_id);
			writeD(_member.class_id);
		}
		members.clear();
	}

	static class PartyMemberInfo
	{
		public String name;
		public int object_id;
		public int class_id;

		public PartyMemberInfo(final String _name, final int _object_id, final int _class_id)
		{
			name = _name;
			object_id = _object_id;
			class_id = _class_id;
		}
	}
}
