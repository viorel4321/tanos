package l2s.gameserver.network.l2.s2c;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.GameClient;

public class PartyMemberPosition extends L2GameServerPacket
{
	private final Map<Integer, int[]> positions;

	public PartyMemberPosition()
	{
		positions = new HashMap<Integer, int[]>();
	}

	public PartyMemberPosition add(final Collection<Player> members)
	{
		if(members != null)
			for(final Player member : members)
				this.add(member);
		return this;
	}

	public PartyMemberPosition add(final Player actor)
	{
		if(actor != null)
			positions.put(actor.getObjectId(), new int[] { actor.getX(), actor.getY(), actor.getZ() });
		return this;
	}

	public void clear()
	{
		positions.clear();
	}

	public int size()
	{
		return positions.size();
	}

	@Override
	protected final void writeImpl()
	{
		final GameClient client = getClient();
		if(client == null || positions.isEmpty())
			return;
		final Player player = client.getActiveChar();
		if(player == null)
			return;
		final int this_player_id = player.getObjectId();
		final int sz = positions.containsKey(this_player_id) ? positions.size() - 1 : positions.size();
		if(sz < 1)
			return;
		writeC(167);
		writeD(sz);
		for(final Integer id : positions.keySet())
			if(id != this_player_id)
			{
				final int[] pos = positions.get(id);
				writeD(id);
				writeD(pos[0]);
				writeD(pos[1]);
				writeD(pos[2]);
			}
	}
}
