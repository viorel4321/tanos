package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class StopMoveInVehicle extends L2GameServerPacket
{
	private int _boatObjectId;
	private int _playerObjectId;
	private int _heading;
	private Location _loc;

	public StopMoveInVehicle(final Player player)
	{
		_boatObjectId = player.getVehicle().getObjectId();
		_playerObjectId = player.getObjectId();
		_loc = player.getInVehiclePosition();
		_heading = player.getHeading();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(114);
		writeD(_playerObjectId);
		writeD(_boatObjectId);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeD(_heading);
	}
}
