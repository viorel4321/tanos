package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.GameTimeController;

public class ClientSetTime extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		writeC(236);
		writeD(GameTimeController.getInstance().getGameTime());
		writeD(6);
	}
}
