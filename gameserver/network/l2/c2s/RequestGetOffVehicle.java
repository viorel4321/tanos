package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.utils.Location;

public class RequestGetOffVehicle extends L2GameClientPacket
{
	private int _objectId;
	private Location _location;

	public RequestGetOffVehicle()
	{
		_location = new Location();
	}

	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_location.x = readD();
		_location.y = readD();
		_location.z = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Vehicle boat = player.getVehicle();
		if(boat == null || boat.isMoving || boat.getObjectId() != _objectId || !boat.isInRange(_location, 500L))
		{
			player.sendActionFailed();
			return;
		}
		boat.oustPlayer(player, _location, false);
	}
}
