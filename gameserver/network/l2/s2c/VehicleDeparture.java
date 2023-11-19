package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.utils.Location;

public class VehicleDeparture extends L2GameServerPacket
{
	private int _moveSpeed;
	private int _rotationSpeed;
	private int _boatObjId;
	private Location _loc;

	public VehicleDeparture(final Vehicle boat)
	{
		_boatObjId = boat.getObjectId();
		_moveSpeed = boat.getMoveSpeed();
		_rotationSpeed = boat.getRotationSpeed();
		_loc = boat.getDestination();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(90);
		writeD(_boatObjId);
		writeD(_moveSpeed);
		writeD(_rotationSpeed);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}
