package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Creature;
import l2s.gameserver.utils.Location;

public class ExFishingStart extends L2GameServerPacket
{
	private int _charObjId;
	private Location _loc;
	private int _fishType;
	private boolean _isNightLure;

	public ExFishingStart(final Creature character, final int fishType, final Location loc, final boolean isNightLure)
	{
		_charObjId = character.getObjectId();
		_fishType = fishType;
		_loc = loc;
		_isNightLure = isNightLure;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(19);
		writeD(_charObjId);
		writeD(_fishType);
		writeD(_loc.x);
		writeD(_loc.y);
		writeD(_loc.z);
		writeC(_isNightLure ? 1 : 0);
		writeC(Config.ALT_FISH_CHAMPIONSHIP_ENABLED ? 1 : 0);
	}
}
