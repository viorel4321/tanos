package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.entity.SevenSigns;

public class ShowMiniMap extends L2GameServerPacket
{
	private int _mapId;
	private int period;

	public ShowMiniMap(final int mapId)
	{
		_mapId = mapId;
		period = SevenSigns.getInstance().getCurrentPeriod();
	}

	@Override
	protected final void writeImpl()
	{
		writeC(157);
		writeD(_mapId);
		writeD(period);
	}
}
