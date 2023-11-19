package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;
import l2s.gameserver.network.l2.s2c.HennaUnequipInfo;
import l2s.gameserver.tables.HennaTable;
import l2s.gameserver.templates.HennaTemplate;

public class RequestHennaUnequipInfo extends L2GameClientPacket
{
	private int _symbolId;

	@Override
	protected void readImpl()
	{
		_symbolId = readD();
	}

	@Override
	protected void runImpl()
	{
		final Player player = getClient().getActiveChar();
		if(player == null)
			return;
		final HennaTemplate h = HennaTable.getInstance().getTemplate(_symbolId);
		if(h != null)
			player.sendPacket(new HennaUnequipInfo(new HennaInstance(h), player));
	}
}
