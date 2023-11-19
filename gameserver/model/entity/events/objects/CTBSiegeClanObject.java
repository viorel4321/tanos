package l2s.gameserver.model.entity.events.objects;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.dao.SiegePlayerDAO;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.entity.residence.Residence;
import l2s.gameserver.model.pledge.Clan;

public class CTBSiegeClanObject extends SiegeClanObject
{
	private List<Integer> _players;
	private long _npcId;

	public CTBSiegeClanObject(final String type, final Clan clan, final long param, final long date)
	{
		super(type, clan, param, date);
		_players = new ArrayList<Integer>();
		_npcId = param;
	}

	public CTBSiegeClanObject(final String type, final Clan clan, final long param)
	{
		this(type, clan, param, System.currentTimeMillis());
	}

	public void select(final Residence r)
	{
		_players.addAll(SiegePlayerDAO.getInstance().select(r, getObjectId()));
	}

	public List<Integer> getPlayers()
	{
		return _players;
	}

	@Override
	public void setEvent(final boolean start, final SiegeEvent<?, ?> event)
	{
		for(final int i : getPlayers())
		{
			final Player player = GameObjectsStorage.getPlayer(i);
			if(player != null)
			{
				if(start)
					player.addEvent(event);
				else
					player.removeEvent(event);
				player.broadcastUserInfo(false);
			}
		}
	}

	@Override
	public boolean isParticle(final Player player)
	{
		return _players.contains(player.getObjectId());
	}

	@Override
	public long getParam()
	{
		return _npcId;
	}

	public void setParam(final int npcId)
	{
		_npcId = npcId;
	}
}
