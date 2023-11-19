package l2s.gameserver.network.l2.s2c;

import java.util.List;

public class ExCursedWeaponList extends L2GameServerPacket
{
	private int[] cursedWeapon_ids;

	public ExCursedWeaponList(final List<Integer> cursedWeaponIds)
	{
		cursedWeapon_ids = new int[cursedWeaponIds.size()];
		for(int i = 0; i < cursedWeaponIds.size(); ++i)
			cursedWeapon_ids[i] = cursedWeaponIds.get(i);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(254);
		writeH(69);
		writeD(cursedWeapon_ids.length);
		for(final int element : cursedWeapon_ids)
			writeD(element);
		cursedWeapon_ids = null;
	}
}
