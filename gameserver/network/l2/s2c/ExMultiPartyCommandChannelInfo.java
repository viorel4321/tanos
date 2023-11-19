package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.CommandChannel;
import l2s.gameserver.model.Party;
import l2s.gameserver.model.Player;

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket
{
	private String ChannelLeaderName;
	private int MemberCount;
	private List<ChannelPartyInfo> parties;

	public ExMultiPartyCommandChannelInfo(final CommandChannel channel)
	{
		if(channel == null)
			return;
		ChannelLeaderName = channel.getChannelLeader().getName();
		MemberCount = channel.getMemberCount();
		parties = new ArrayList<ChannelPartyInfo>();
		for(final Party party : channel.getParties())
			if(party != null)
			{
				final Player leader = party.getPartyLeader();
				if(leader == null)
					continue;
				parties.add(new ChannelPartyInfo(leader.getName(), leader.getObjectId(), party.getMemberCount()));
			}
	}

	@Override
	protected void writeImpl()
	{
		if(parties == null)
		{
			writeD(0);
			return;
		}
		writeC(254);
		writeH(48);
		writeS(ChannelLeaderName);
		writeD(0);
		writeD(MemberCount);
		writeD(parties.size());
		for(final ChannelPartyInfo party : parties)
		{
			writeS(party.Leader_name);
			writeD(party.Leader_obj_id);
			writeD(party.MemberCount);
		}
		parties.clear();
	}

	static class ChannelPartyInfo
	{
		public String Leader_name;
		public int Leader_obj_id;
		public int MemberCount;

		public ChannelPartyInfo(final String _Leader_name, final int _Leader_obj_id, final int _MemberCount)
		{
			Leader_name = _Leader_name;
			Leader_obj_id = _Leader_obj_id;
			MemberCount = _MemberCount;
		}
	}
}
