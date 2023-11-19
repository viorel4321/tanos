package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.utils.Location;

public class CannotMoveAnymoreInVehicle extends L2GameClientPacket
{
	private Location _loc;
	private int _boatid;

	public CannotMoveAnymoreInVehicle()
	{
		_loc = new Location();
	}

	@Override
	protected void readImpl()
	{
		_boatid = readD();
		_loc.x = readD();
		_loc.y = readD();
		_loc.z = readD();
		_loc.h = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final Vehicle boat = player.getVehicle();
		if(boat != null && boat.getObjectId() == _boatid && _loc.distance3D(0, 0, 0) < 1000.0)
		{
			player.setInVehiclePosition(_loc);
			player.setHeading(_loc.h);
			player.broadcastPacket(boat.inStopMovePacket(player));
		}
	}
}
