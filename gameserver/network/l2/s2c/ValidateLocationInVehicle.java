package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class ValidateLocationInVehicle extends L2GameServerPacket
{
	private int _charObjId;
	private int _boatObjId;
	private Location _loc;

	public ValidateLocationInVehicle(final Player player)
	{
		_charObjId = player.getObjectId();
		_boatObjId = player.getVehicle().getObjectId();
		_loc = player.getInVehiclePosition();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(115);
		writeD(_charObjId);
		writeD(_boatObjId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_loc.h);
	}
}
