package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;
import l2s.gameserver.utils.Location;

public class MoveToLocationInVehicle extends L2GameServerPacket
{
	private int char_id;
	private int boat_id;
	private Location _origin;
	private Location _destination;

	public MoveToLocationInVehicle(final Player cha, final Vehicle boat, final Location origin, final Location destination)
	{
		char_id = cha.getObjectId();
		boat_id = boat.getObjectId();
		_origin = origin;
		_destination = destination;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(113);
		writeD(char_id);
		writeD(boat_id);
		writeD(_destination.x);
		writeD(_destination.y);
		writeD(_destination.z);
		writeD(_origin.x);
		writeD(_origin.y);
		writeD(_origin.z);
	}
}
