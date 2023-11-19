package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Macro;
import l2s.gameserver.model.Player;
import l2s.gameserver.network.l2.s2c.SystemMessage;

public class RequestMakeMacro extends L2GameClientPacket
{
	private Macro _macro;

	@Override
	public void readImpl()
	{
		final int _id = readD();
		final String _name = readS(32);
		final String _desc = readS(64);
		final String _acronym = readS(4);
		final int _icon = readC();
		int _count = readC();
		if(_count > 12)
			_count = 12;
		final Macro.L2MacroCmd[] commands = new Macro.L2MacroCmd[_count];
		for(int i = 0; i < _count; ++i)
		{
			final int entry = readC();
			final int type = readC();
			final int d1 = readD();
			final int d2 = readC();
			final String command = readS().replace(";", "").replace(",", "");
			commands[i] = new Macro.L2MacroCmd(entry, type, d1, d2, command);
		}
		_macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(activeChar.getMacroses().getAllMacroses().length > 48)
		{
			activeChar.sendPacket(new SystemMessage(797));
			return;
		}
		if(_macro.name.length() == 0)
		{
			activeChar.sendPacket(new SystemMessage(838));
			return;
		}
		if(_macro.descr.length() > 32)
		{
			activeChar.sendPacket(new SystemMessage(837));
			return;
		}
		activeChar.registerMacro(_macro);
	}
}
