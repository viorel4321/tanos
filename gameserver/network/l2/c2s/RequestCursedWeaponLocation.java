package l2s.gameserver.network.l2.c2s;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.instancemanager.CursedWeaponsManager;
import l2s.gameserver.model.CursedWeapon;
import l2s.gameserver.model.Creature;
import l2s.gameserver.network.l2.s2c.ExCursedWeaponLocation;
import l2s.gameserver.utils.Location;

public class RequestCursedWeaponLocation extends L2GameClientPacket
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
		final List<ExCursedWeaponLocation.CursedWeaponInfo> list = new ArrayList<ExCursedWeaponLocation.CursedWeaponInfo>();
		for(final CursedWeapon cw : CursedWeaponsManager.getInstance().getCursedWeapons())
		{
			final Location pos = cw.getWorldPosition();
			if(pos != null)
				list.add(new ExCursedWeaponLocation.CursedWeaponInfo(pos, cw.getItemId(), cw.isActivated() ? 1 : 0));
		}
		activeChar.sendPacket(new ExCursedWeaponLocation(list));
	}
}
