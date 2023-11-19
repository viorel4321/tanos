package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.utils.Location;

public class RequestGetOnVehicle extends L2GameClientPacket
{
	private int _objectId;
	private Location _loc;

	public RequestGetOnVehicle()
	{
		_loc = new Location();
	}

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null || player.isInVehicle())
			return;
		final Vehicle boat = BoatHolder.getInstance().getBoat(_objectId);
		if(boat == null || boat.isMoving || !boat.isInRange(player, 600L) || _loc.distance3D(0, 0, 0) > 1000.0)
			return;
		player._stablePoint = boat.getCurrentWay().getReturnLoc();
		boat.addPlayer(player, _loc);
	}
}
