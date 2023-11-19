package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.s2c.ExCursedWeaponList;

public class RequestCursedWeaponList extends L2GameClientPacket
{
	@Override
	public void readImpl()
	{}

	@Override
	public void runImpl()
	{
		final Creature activeChar = getClient().getActiveChar();
		if(activeChar == null)
			return;
		final List<Integer> list = new ArrayList<Integer>();
		for(final int id : CursedWeaponsManager.getInstance().getCursedWeaponsIds())
			list.add(id);
		activeChar.sendPacket(new ExCursedWeaponList(list));
	}
}
