package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.cache.CrestCache;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestSetPledgeCrest extends L2GameClientPacket
{
	private int _length;
	private byte[] _data;

	@Override
	protected void readImpl()
	{
		_length = readD();
		if(_length == 256 && _length == _buf.remaining())
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
		if(clan.getDissolvingExpiryTime() > System.currentTimeMillis())
		{
			activeChar.sendPacket(new SystemMessage(552));
			return;
		}
		if(_length == 0 || _data.length == 0)
		{
			if(clan.hasCrest())
				CrestCache.getInstance().removePledgeCrest(clan.getClanId());
			clan.setCrestId(0);
			activeChar.sendPacket(new SystemMessage(1861));
			clan.broadcastClanStatus(false, true, false);
			return;
		}
		if((activeChar.getClanPrivileges() & 0x80) == 0x80)
		{
			if(clan.getLevel() < 3)
			{
				activeChar.sendPacket(new SystemMessage(272));
				return;
			}
			int crestId = 0;
			if(_data != null)
				crestId = CrestCache.getInstance().savePledgeCrest(clan.getClanId(), _data);
			else if(clan.hasCrest())
				CrestCache.getInstance().removePledgeCrest(clan.getClanId());
			clan.setCrestId(crestId);
			clan.broadcastClanStatus(false, true, false);
		}
	}
}
