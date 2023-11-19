package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.entity.SevenSigns;

public class SSQInfo extends L2GameServerPacket
{
	@Override
	protected final void writeImpl()
	{
		if(!Config.ALLOW_SEVEN_SIGNS)
			return;

		writeC(248);
		writeH(SevenSigns.getInstance().getSky());
	}
}
