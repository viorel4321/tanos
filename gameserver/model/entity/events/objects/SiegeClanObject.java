package l2s.gameserver.model.entity.events.objects;

import java.io.Serializable;
import java.util.Comparator;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.events.impl.SiegeEvent;
import l2s.gameserver.model.instances.NpcInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.components.IBroadcastPacket;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;

public class SiegeClanObject implements Serializable
{
	private String _type;
	private Clan _clan;
	private NpcInstance _flag;
	private final long _date;

	public SiegeClanObject(final String type, final Clan clan, final long param)
	{
		this(type, clan, 0L, System.currentTimeMillis());
	}

	public SiegeClanObject(final String type, final Clan clan, final long param, final long date)
	{
		_type = type;
		_clan = clan;
		_date = date;
	}

	public int getObjectId()
	{
		return _clan.getClanId();
	}

	public Clan getClan()
	{
		return _clan;
	}

	public NpcInstance getFlag()
	{
		return _flag;
	}

	public void deleteFlag()
	{
		if(_flag != null)
		{
			_flag.deleteMe();
			_flag = null;
		}
	}

	public void setFlag(final NpcInstance npc)
	{
		_flag = npc;
	}

	public void setType(final String type)
	{
		_type = type;
	}

	public String getType()
	{
		return _type;
	}

	public void broadcast(final IBroadcastPacket... packet)
	{
		getClan().broadcastToOnlineMembers(packet);
	}

	public void broadcast(final L2GameServerPacket... packet)
	{
		getClan().broadcastToOnlineMembers(packet);
	}

	public void setEvent(final boolean start, final SiegeEvent<?, ?> event)
	{
		if(start)
			for(final Player player : _clan.getOnlineMembers(0))
			{
				player.addEvent(event);
				player.broadcastUserInfo(true);
			}
		else
			for(final Player player : _clan.getOnlineMembers(0))
			{
				player.removeEvent(event);
				player.broadcastUserInfo(true);
			}
	}

	public boolean isParticle(final Player player)
	{
		return true;
	}

	public long getParam()
	{
		return 0L;
	}

	public long getDate()
	{
		return _date;
	}

	public static class SiegeClanComparatorImpl implements Comparator<SiegeClanObject>
	{
		private static final SiegeClanComparatorImpl _instance;

		public static SiegeClanComparatorImpl getInstance()
		{
			return SiegeClanComparatorImpl._instance;
		}

		@Override
		public int compare(final SiegeClanObject o1, final SiegeClanObject o2)
		{
			return o2.getParam() < o1.getParam() ? -1 : o2.getParam() == o1.getParam() ? 0 : 1;
		}

		static
		{
			_instance = new SiegeClanComparatorImpl();
		}
	}
}
