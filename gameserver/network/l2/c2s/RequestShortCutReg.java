package l2s.gameserver.network.l2.c2s;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.ShortCut;
import l2s.gameserver.network.l2.s2c.ShortCutRegister;

public class RequestShortCutReg extends L2GameClientPacket
{
	private int _type;
	private int _id;
	private int _slot;
	private int _page;

	@Override
	public void readImpl()
	{
		_type = readD();
		final int slot = readD();
		_id = readD();
		readD();
		_slot = slot % 12;
		_page = slot / 12;
	}

	@Override
	public void runImpl()
	{
		final Player activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		if(_slot < 0 || _slot > 11 || _page < 0 || _page > 9)
		{
			activeChar.sendActionFailed();
			return;
		}
		switch(_type)
		{
			case 1:
			case 3:
			case 4:
			case 5:
			{
				final ShortCut sc = new ShortCut(_slot, _page, _type, _id, -1);
				this.sendPacket(new ShortCutRegister(sc));
				activeChar.registerShortCut(sc);
				break;
			}
			case 2:
			{
				final int level = activeChar.getSkillDisplayLevel(_id);
				if(level > 0)
				{
					final ShortCut sc2 = new ShortCut(_slot, _page, _type, _id, level);
					this.sendPacket(new ShortCutRegister(sc2));
					activeChar.registerShortCut(sc2);
					break;
				}
				break;
			}
		}
	}
}
