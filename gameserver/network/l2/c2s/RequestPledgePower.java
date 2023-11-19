package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.ManagePledgePower;

public class RequestPledgePower extends L2GameClientPacket
{
	private int _rank;
	private int _action;
	private int _privs;

	@Override
	public void readImpl()
	{
		_rank = readD();
		_action = readD();
		if(_action == 2)
			_privs = readD();
		else
			_privs = 0;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.isOutOfControl() || activeChar.getClan() == null)
		{
			activeChar.sendActionFailed();
			return;
		}
		if(_action == 2)
		{
			if((activeChar.getClanPrivileges() & 0x10) == 0x10)
			{
				if(_rank == 9)
					_privs = (_privs & 0x400) + (_privs & 0x8000);
				activeChar.getClan().setRankPrivs(_rank, _privs);
				activeChar.getClan().updatePrivsForRank(_rank);
			}
		}
		else
			activeChar.sendPacket(new ManagePledgePower(activeChar, _action, _rank));
	}
}
