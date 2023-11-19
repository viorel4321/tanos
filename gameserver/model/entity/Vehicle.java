package l2s.gameserver.model.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l2s.gameserver.ai.BoatAI;
import l2s.gameserver.ai.CharacterAI;
import l2s.gameserver.cache.Msg;
import l2s.gameserver.model.Creature;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.World;
import l2s.gameserver.model.entity.events.impl.BoatWayEvent;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.network.l2.s2c.GetOffVehicle;
import l2s.gameserver.network.l2.s2c.GetOnVehicle;
import l2s.gameserver.network.l2.s2c.L2GameServerPacket;
import l2s.gameserver.network.l2.s2c.MoveToLocationInVehicle;
import l2s.gameserver.network.l2.s2c.StopMove;
import l2s.gameserver.network.l2.s2c.StopMoveInVehicle;
import l2s.gameserver.network.l2.s2c.ValidateLocationInVehicle;
import l2s.gameserver.network.l2.s2c.VehicleCheckLocation;
import l2s.gameserver.network.l2.s2c.VehicleDeparture;
import l2s.gameserver.network.l2.s2c.VehicleInfo;
import l2s.gameserver.network.l2.s2c.VehicleStart;
import l2s.gameserver.templates.CreatureTemplate;
import l2s.gameserver.templates.item.WeaponTemplate;
import l2s.gameserver.utils.Location;
import l2s.gameserver.utils.Util;

public class Vehicle extends Creature
{
	private static final long serialVersionUID = 1L;
	private int _moveSpeed;
	private int _rotationSpeed;
	protected int _fromHome;
	protected int _runState;
	private final BoatWayEvent[] _ways;
	protected final Set<Player> _players;

	public Vehicle(final int objectId, final CreatureTemplate template)
	{
		super(objectId, template);
		_ways = new BoatWayEvent[2];
		_players = new CopyOnWriteArraySet<Player>();
	}

	@Override
	public void spawnMe()
	{
		super.spawnMe();
		_fromHome = 1;
		getCurrentWay().reCalcNextTime(false);
	}

	@Override
	public boolean setXYZ(final int x, final int y, final int z, final boolean move)
	{
		boolean result = super.setXYZ(x, y, z, move);
		updatePeopleInTheBoat(x, y, z, move);
		return result;
	}

	public void onEvtArrived()
	{
		getCurrentWay().moveNext();
	}

	protected void updatePeopleInTheBoat(final int x, final int y, final int z, final boolean moveTask)
	{
		for(final Player player : _players)
			if(player != null)
				player.setXYZ(x, y, z, moveTask);
	}

	public void addPlayer(final Player player, final Location boatLoc)
	{
		if(player.getServitor() != null)
		{
			player.sendPacket(Msg.BECAUSE_PET_OR_SERVITOR_MAY_BE_DROWNED_WHILE_THE_BOAT_MOVES_PLEASE_RELEASE_THE_SUMMON_BEFORE_DEPARTURE, Msg.ActionFail);
			return;
		}
		synchronized (_players)
		{
			_players.add(player);
		}
		player.setVehicle(this);
		player.setInVehiclePosition(boatLoc);
		player.setLoc(getLoc(), true);
		player.broadcastPacket(getOnPacket(player, boatLoc));
	}

	public void moveInBoat(final Player player, final Location ori, final Location loc)
	{
		if(player.getServitor() != null)
		{
			player.sendPacket(Msg.BECAUSE_PET_OR_SERVITOR_MAY_BE_DROWNED_WHILE_THE_BOAT_MOVES_PLEASE_RELEASE_THE_SUMMON_BEFORE_DEPARTURE, Msg.ActionFail);
			return;
		}
		if(player.isMovementDisabled() || player.isSitting())
		{
			player.sendActionFailed();
			return;
		}
		player.setHeading(loc.h = Util.getHeadingTo(ori, loc));
		if(player.isInVehicle())
			player.setInVehiclePosition(loc);
		player.broadcastPacket(inMovePacket(player, ori, loc));
	}

	public void trajetEnded(final boolean oust)
	{
		_runState = 0;
		_fromHome = _fromHome != 1 ? 1 : 0;
		final L2GameServerPacket checkLocation = checkLocationPacket();
		if(checkLocation != null)
			this.broadcastPacket(infoPacket(), checkLocation);
		if(oust)
			getCurrentWay().reCalcNextTime(false);
	}

	public void teleportShip(final int x, final int y, final int z)
	{
		if(isMoving)
			this.stopMove(false);
		setIsTeleporting(true);
		for(final Player player : _players)
			player.teleToLocation(x, y, z);
		this.setHeading(this.calcHeading(x, y));
		this.setXYZ(x, y, z);
		setIsTeleporting(false);
		getCurrentWay().moveNext();
	}

	public void oustPlayer(final Player player, final Location loc, final boolean teleport)
	{
		synchronized (_players)
		{
			_players.remove(player);
		}
		player._stablePoint = null;
		player.setVehicle(null);
		player.setInVehiclePosition(null);
		player.broadcastPacket(getOffPacket(player, loc));
		if(teleport)
			player.teleToLocation(loc);
	}

	public void removePlayer(final Player player)
	{
		synchronized (_players)
		{
			_players.remove(player);
		}
	}

	public void broadcastPacketToPassengers(final L2GameServerPacket packet)
	{
		for(final Player player : _players)
			player.sendPacket(packet);
	}

	@Override
	protected CharacterAI initAI()
	{
		return new BoatAI(this);
	}

	@Override
	public void broadcastUserInfo(final boolean force)
	{
		this.broadcastPacket(infoPacket());
	}

	@Override
	public void broadcastPacket(final L2GameServerPacket... packets)
	{
		for(final Player player : World.getAroundPlayers(this))
			if(player.getReflectionId() == 0 && !player.inObserverMode() && !player.isInOlympiadMode() && !player.inEvent())
				player.sendPacket(packets);
	}

	@Override
	public void validateLocation(final int broadcast)
	{}

	@Override
	public void sendChanges()
	{}

	@Override
	public int getMoveSpeed()
	{
		return _moveSpeed;
	}

	@Override
	public int getRunSpeed()
	{
		return _moveSpeed;
	}

	@Override
	public ItemInstance getActiveWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getActiveWeaponItem()
	{
		return null;
	}

	@Override
	public ItemInstance getSecondaryWeaponInstance()
	{
		return null;
	}

	@Override
	public WeaponTemplate getSecondaryWeaponItem()
	{
		return null;
	}

	@Override
	public byte getLevel()
	{
		return 0;
	}

	@Override
	public boolean isAutoAttackable(final Creature attacker)
	{
		return false;
	}

	@Override
	public boolean isAttackable(final Creature attacker)
	{
		return false;
	}

	public int getRunState()
	{
		return _runState;
	}

	public void setRunState(final int runState)
	{
		_runState = runState;
	}

	public void setMoveSpeed(final int moveSpeed)
	{
		_moveSpeed = moveSpeed;
	}

	public void setRotationSpeed(final int rotationSpeed)
	{
		_rotationSpeed = rotationSpeed;
	}

	public int getRotationSpeed()
	{
		return _rotationSpeed;
	}

	public BoatWayEvent getCurrentWay()
	{
		return _ways[_fromHome];
	}

	public void setWay(final int id, final BoatWayEvent v)
	{
		_ways[id] = v;
	}

	public Set<Player> getPlayers()
	{
		return _players;
	}

	public boolean isDocked()
	{
		return _runState == 0;
	}

	public Location getReturnLoc()
	{
		return getCurrentWay().getReturnLoc();
	}

	public L2GameServerPacket startPacket()
	{
		return new VehicleStart(getObjectId(), getRunState());
	}

	public L2GameServerPacket validateLocationPacket(final Player player)
	{
		return new ValidateLocationInVehicle(player);
	}

	public L2GameServerPacket checkLocationPacket()
	{
		return new VehicleCheckLocation(this);
	}

	public L2GameServerPacket infoPacket()
	{
		return new VehicleInfo(this);
	}

	@Override
	public L2GameServerPacket movePacket()
	{
		return new VehicleDeparture(this);
	}

	public L2GameServerPacket inMovePacket(final Player player, final Location src, final Location desc)
	{
		return new MoveToLocationInVehicle(player, this, src, desc);
	}

	@Override
	public L2GameServerPacket stopMovePacket()
	{
		return new StopMove(this);
	}

	public L2GameServerPacket inStopMovePacket(final Player player)
	{
		return new StopMoveInVehicle(player);
	}

	public L2GameServerPacket getOnPacket(final Player player, final Location location)
	{
		return new GetOnVehicle(player.getObjectId(), getObjectId(), location);
	}

	public L2GameServerPacket getOffPacket(final Player player, final Location location)
	{
		return new GetOffVehicle(player, this, location.x, location.y, location.z);
	}

	@Override
	public boolean isVehicle()
	{
		return true;
	}

	@Override
	public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper)
	{
		if(!isMoving)
			return Collections.singletonList(infoPacket());
		final List<L2GameServerPacket> list = new ArrayList<L2GameServerPacket>(2);
		list.add(infoPacket());
		list.add(movePacket());
		return list;
	}
}
