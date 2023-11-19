package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.PledgeReceiveWarList;

public class RequestPledgeWarList extends L2GameClientPacket
{
	static int _type;
	private int _page;

	@Override
	public void readImpl()
	{
		_page = readD();
		RequestPledgeWarList._type = readD();
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		final Clan clan = activeChar.getClan();
		if(clan != null)
			activeChar.sendPacket(new PledgeReceiveWarList(clan, RequestPledgeWarList._type, _page));
	}
}
