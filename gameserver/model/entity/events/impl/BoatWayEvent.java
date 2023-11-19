package l2s.gameserver.model.entity.events.impl;

import java.util.List;

import l2s.commons.collections.LazyArrayList;
import l2s.commons.collections.MultiValueSet;
import l2s.gameserver.Config;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.model.GameObjectsStorage;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.model.entity.events.GlobalEvent;
import l2s.gameserver.model.entity.events.objects.BoatPoint;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.utils.Location;

public class BoatWayEvent extends GlobalEvent
{
	public static final String BOAT_POINTS = "boat_points";
	private final int _ticketId;
	private final Location _returnLoc;
	private final Vehicle _boat;
	private final Location[] _broadcastPoints;

	public BoatWayEvent(final MultiValueSet<String> set)
	{
		super(set);
		_ticketId = set.getInteger("ticketId", 0);
		_returnLoc = Location.parseLoc(set.getString("return_point"));
		final String className = set.getString("class", (String) null);
		if(className != null)
		{
			_boat = BoatHolder.getInstance().initBoat(getName(), className);
			final Location loc = Location.parseLoc(set.getString("spawn_point"));
			_boat.setLoc(loc, true);
			_boat.setHeading(loc.h);
		}
		else
			_boat = BoatHolder.getInstance().getBoat(getName());
		_boat.setWay(className != null ? 1 : 0, this);
		final String brPoints = set.getString("broadcast_point", (String) null);
		if(brPoints == null)
			(_broadcastPoints = new Location[1])[0] = _boat.getLoc();
		else
		{
			final String[] points = brPoints.split(";");
			_broadcastPoints = new Location[points.length];
			for(int i = 0; i < points.length; ++i)
				_broadcastPoints[i] = Location.parseLoc(points[i]);
		}
	}

	@Override
	public void initEvent()
	{}

	@Override
	public void startEvent()
	{
		final L2GameServerPacket startPacket = _boat.startPacket();
		for(final Player player : _boat.getPlayers())
			if(_ticketId > 0)
			{
				if(player.consumeItem(_ticketId, 1))
				{
					if(startPacket == null)
						continue;
					player.sendPacket(startPacket);
				}
				else
				{
					player.sendPacket(Msg.YOU_MAY_NOT_GET_ON_BOARD_WITHOUT_A_PASS);
					_boat.oustPlayer(player, _returnLoc, true);
				}
			}
			else
			{
				if(startPacket == null)
					continue;
				player.sendPacket(startPacket);
			}
		moveNext();
	}

	public void moveNext()
	{
		final List<BoatPoint> points = this.getObjects("boat_points");
		if(_boat.getRunState() >= points.size())
		{
			_boat.trajetEnded(true);
			clearActions();
			return;
		}
		final BoatPoint bp = points.get(_boat.getRunState());
		if(bp.getSpeed1() >= 0)
			_boat.setMoveSpeed(bp.getSpeed1());
		if(bp.getSpeed2() >= 0)
			_boat.setRotationSpeed(bp.getSpeed2());
		if(_boat.getRunState() == 0)
			_boat.broadcastUserInfo(true);
		_boat.setRunState(_boat.getRunState() + 1);
		if(bp.isTeleport())
			_boat.teleportShip(bp.getX(), bp.getY(), bp.getZ());
		else
			_boat.moveToLocation(bp.getX(), bp.getY(), bp.getZ(), 0, false);
	}

	@Override
	public void reCalcNextTime(final boolean onInit)
	{
		registerActions();
	}

	@Override
	protected long startTimeMillis()
	{
		return System.currentTimeMillis();
	}

	@Override
	public List<Player> broadcastPlayers(final int range)
	{
		final List<Player> players = new LazyArrayList<Player>(64);
		final int rne = range > 0 ? range : Config.BOAT_BROADCAST_RADIUS;
		for(final Location loc : _broadcastPoints)
			for(final Player player : GameObjectsStorage.getPlayers())
				if(player.getReflectionId() == 0 && !player.inObserverMode() && !player.isInOlympiadMode())
				{
					if(player.inEvent())
						continue;
					if(!player.isInRange(loc, rne) || players.contains(player))
						continue;
					players.add(player);
				}
		return players;
	}

	@Override
	public void printInfo()
	{}

	public Location getReturnLoc()
	{
		return _returnLoc;
	}
}
