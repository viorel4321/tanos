package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;
import l2s.gameserver.network.l2.s2c.HennaItemInfo;
import l2s.gameserver.tables.HennaTable;
import l2s.gameserver.templates.HennaTemplate;

public class RequestHennaItemInfo extends L2GameClientPacket
{
	private int SymbolId;

	@Override
	public void readImpl()
	{
		SymbolId = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final HennaTemplate template = HennaTable.getInstance().getTemplate(SymbolId);
		if(template != null)
			activeChar.sendPacket(new HennaItemInfo(new HennaInstance(template), activeChar));
	}
}
