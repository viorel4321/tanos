package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class Ride extends L2GameServerPacket
{
	private boolean _canWriteImpl;
	private int _mountType;
	private int _id;
	private int _rideClassID;
	private Location _loc;

	public Ride(final Player cha)
	{
		_canWriteImpl = false;
		if(cha == null)
			return;
		_id = cha.getObjectId();
		_mountType = cha.getMountType();
		_rideClassID = cha.getMountNpcId() + 1000000;
		_loc = cha.getLoc();
		_canWriteImpl = true;
	}

	@Override
	protected final void writeImpl()
	{
		if(!_canWriteImpl)
			return;
		writeC(134);
		writeD(_id);
		writeD(_mountType != 0 ? 1 : 0);
		writeD(_mountType);
		writeD(_rideClassID);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
	}
}
