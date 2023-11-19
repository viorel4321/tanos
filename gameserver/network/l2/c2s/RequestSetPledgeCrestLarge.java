package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestSetPledgeCrestLarge extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length == 2176 && _length == _buf.remaining())
			readB(_data = new byte[_length]);
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final Clan clan = activeChar.getClan();
		if(clan == null)
			return;
		if((activeChar.getClanPrivileges() & 0x80) == 0x80)
		{
			if(clan.getHasCastle() == 0 && clan.getHasHideout() == 0)
			{
				activeChar.sendPacket(new SystemMessage(1663));
				return;
			}
			int crestId = 0;
			if(_data != null)
			{
				crestId = CrestCache.getInstance().savePledgeCrestLarge(clan.getClanId(), _data);
				activeChar.sendPacket(new SystemMessage(1663));
			}
			else if(clan.hasCrestLarge())
				CrestCache.getInstance().removePledgeCrestLarge(clan.getClanId());
			clan.setCrestLargeId(crestId);
			clan.broadcastClanStatus(false, true, false);
		}
	}
}
