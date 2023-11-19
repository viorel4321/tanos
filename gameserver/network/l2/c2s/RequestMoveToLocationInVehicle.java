package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.data.BoatHolder;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.utils.Location;

public class RequestMoveToLocationInVehicle extends L2GameClientPacket
{
	private Location _pos;
	private Location _originPos;
	private int _boatObjectId;

	public RequestMoveToLocationInVehicle()
	{
		_pos = new Location();
		_originPos = new Location();
	}

	@Override
	protected void readImpl()
	{
		_boatObjectId = readD();
		_pos.x = readD();
		_pos.y = readD();
		_pos.z = readD();
		_originPos.x = readD();
		_originPos.y = readD();
		_originPos.z = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		Vehicle boat;
		if(player.isInVehicle())
		{
			boat = player.getVehicle();
			if(boat.getObjectId() != _boatObjectId || !player.isInRange(boat, 1000L))
			{
				player.sendActionFailed();
				return;
			}
		}
		else
		{
			boat = BoatHolder.getInstance().getBoat(_boatObjectId);
			if(boat == null || !player.isInRange(boat, 500L))
			{
				player.sendActionFailed();
				return;
			}
		}
		if(_pos.distance3D(0, 0, 0) > 1000.0 || _originPos.distance3D(0, 0, 0) > 1000.0)
		{
			player.sendActionFailed();
			return;
		}
		boat.moveInBoat(player, _originPos, _pos);
	}
}
