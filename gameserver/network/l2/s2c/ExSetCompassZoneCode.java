package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;

public class ExSetCompassZoneCode extends L2GameServerPacket
{
	public static int ZONE_ALTERED;
	public static int ZONE_SIEGE;
	public static int ZONE_PEACE;
	public static int ZONE_SS;
	public static int ZONE_PVP;
	public static int ZONE_GENERAL_FIELD;
	int _zone;

	public ExSetCompassZoneCode(final Player player)
	{
		_zone = -1;
		if(player.isInDangerArea())
			_zone = ExSetCompassZoneCode.ZONE_ALTERED;
		else if(player.isOnSiegeField())
			_zone = ExSetCompassZoneCode.ZONE_SIEGE;
		else if(player.isInCombatZone())
			_zone = ExSetCompassZoneCode.ZONE_PVP;
		else if(player.isInPeaceZone())
			_zone = ExSetCompassZoneCode.ZONE_PEACE;
		else if(player.isInSSZone())
			_zone = ExSetCompassZoneCode.ZONE_SS;
		else
			_zone = ExSetCompassZoneCode.ZONE_GENERAL_FIELD;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(50);
		writeD(_zone);
	}

	static
	{
		ExSetCompassZoneCode.ZONE_ALTERED = 8;
		ExSetCompassZoneCode.ZONE_SIEGE = 11;
		ExSetCompassZoneCode.ZONE_PEACE = 12;
		ExSetCompassZoneCode.ZONE_SS = 13;
		ExSetCompassZoneCode.ZONE_PVP = 14;
		ExSetCompassZoneCode.ZONE_GENERAL_FIELD = 15;
	}
}
