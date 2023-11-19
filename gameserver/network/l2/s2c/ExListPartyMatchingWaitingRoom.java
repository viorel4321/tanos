package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import l2s.gameserver.instancemanager.PartyRoomManager;
import l2s.gameserver.model.Player;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket
{
	private List<PartyMatchingWaitingInfo> _waitingList;
	private final int _fullSize;

	public ExListPartyMatchingWaitingRoom(final Player searcher, final int minLevel, final int maxLevel, final int page, final boolean showAll)
	{
		_waitingList = Collections.emptyList();
		final int first = (page - 1) * 64;
		final int firstNot = page * 64;
		int i = 0;
		final List<Player> temp = PartyRoomManager.getInstance().getWaitingList(minLevel, maxLevel, showAll);
		_fullSize = temp.size();
		_waitingList = new ArrayList<PartyMatchingWaitingInfo>(_fullSize);
		for(final Player pc : temp)
			if(i >= first)
			{
				if(i >= firstNot)
					continue;
				_waitingList.add(new PartyMatchingWaitingInfo(pc));
				++i;
			}
	}

	@Override
	protected void writeImpl()
	{
		writeC(254);
		writeH(54);
		writeD(_fullSize);
		writeD(_waitingList.size());
		for(final PartyMatchingWaitingInfo waiting_info : _waitingList)
		{
			writeS((CharSequence) waiting_info.name);
			writeD(waiting_info.classId);
			writeD(waiting_info.level);
		}
	}

	static class PartyMatchingWaitingInfo
	{
		public final int classId;
		public final int level;
		public final String name;

		public PartyMatchingWaitingInfo(final Player member)
		{
			name = member.getName();
			classId = member.getClassId().getId();
			level = member.getLevel();
		}
	}
}
