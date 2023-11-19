package l2s.gameserver.model.entity.events.objects;

import org.napile.primitive.sets.IntSet;
import org.napile.primitive.sets.impl.HashIntSet;

import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.pledge.Clan;

public class CMGSiegeClanObject extends SiegeClanObject
{
	private IntSet _players;
	private long _param;

	public CMGSiegeClanObject(final String type, final Clan clan, final long param, final long date)
	{
		super(type, clan, param, date);
		_players = new HashIntSet();
		_param = param;
	}

	public CMGSiegeClanObject(final String type, final Clan clan, final long param)
	{
		super(type, clan, param);
		_players = new HashIntSet();
		_param = param;
	}

	public void addPlayer(final int objectId)
	{
		_players.add(objectId);
	}

	@Override
	public long getParam()
	{
		return _param;
	}

	@Override
	public boolean isParticle(final Player player)
	{
		return _players.contains(player.getObjectId());
	}

	@Override
	public void setEvent(final boolean start, final SiegeEvent<?, ?> event)
	{
		for(final int i : _players.toArray())
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

	public void setParam(final long param)
	{
		_param = param;
	}

	public IntSet getPlayers()
	{
		return _players;
	}
}
