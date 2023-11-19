package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.utils.Location;

public class ObserverEnd extends L2GameServerPacket
{
	private Location _loc;

	public ObserverEnd(final Player observer)
	{
		_loc = observer.getObsLoc();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(224);
		writeD(_loc.getX());
		writeD(_loc.getY());
		writeD(_loc.getZ());
	}
}
