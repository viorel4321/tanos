package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.entity.Vehicle;

public class GetOffVehicle extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int char_obj_id;
	private int boat_obj_id;

	public GetOffVehicle(final Player activeChar, final Vehicle boat, final int x, final int y, final int z)
	{
		_x = x;
		_y = y;
		_z = z;
		char_obj_id = activeChar.getObjectId();
		boat_obj_id = boat.getObjectId();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(93);
		writeD(char_obj_id);
		writeD(boat_obj_id);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
