package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.utils.Location;

public class GetOnVehicle extends L2GameServerPacket
{
	private int _x;
	private int _y;
	private int _z;
	private int _char_obj_id;
	private int _boat_obj_id;
	private boolean can_write;

	public GetOnVehicle(final int char_obj_id, final int boat_obj_id, final Location loc)
	{
		if(loc != null)
		{
			_x = loc.x;
			_y = loc.y;
			_z = loc.z;
			_char_obj_id = char_obj_id;
			_boat_obj_id = boat_obj_id;
			can_write = true;
		}
	}

	@Override
	protected final void writeImpl()
	{
		if(!can_write)
			return;
		writeC(92);
		writeD(_char_obj_id);
		writeD(_boat_obj_id);
		writeD(_x);
		writeD(_y);
		writeD(_z);
	}
}
