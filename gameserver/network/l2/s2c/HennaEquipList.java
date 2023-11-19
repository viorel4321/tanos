package l2s.gameserver.network.l2.s2c;

import java.util.HashSet;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;

public class HennaEquipList extends L2GameServerPacket
{
	private int char_adena;
	private int HennaEmptySlots;
	private HashSet<HennaInstance> availHenna;

	public HennaEquipList(final Player player, final HennaInstance[] hennaEquipList)
	{
		availHenna = new HashSet<HennaInstance>();
		char_adena = player.getAdena();
		HennaEmptySlots = player.getHennaEmptySlots();
		for(final HennaInstance element : hennaEquipList)
			if(player.getInventory().findItemByItemId(element.getItemIdDye()) != null)
				availHenna.add(element);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(226);
		writeD(char_adena);
		writeD(HennaEmptySlots);
		if(availHenna.size() != 0)
		{
			writeD(availHenna.size());
			for(final HennaInstance henna : availHenna)
			{
				writeD((int) henna.getSymbolId());
				writeD((int) henna.getItemIdDye());
				writeD((int) henna.getAmountDyeRequire());
				writeD(henna.getPrice());
				writeD(1);
			}
		}
		else
		{
			writeD(1);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
			writeD(0);
		}
	}
}
