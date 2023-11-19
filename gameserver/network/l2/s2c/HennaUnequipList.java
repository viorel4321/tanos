package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.instances.HennaInstance;

public class HennaUnequipList extends L2GameServerPacket
{
	private int _emptySlots;
	private int _adena;
	private List<HennaInstance> availHenna;

	public HennaUnequipList(final Player player)
	{
		availHenna = new ArrayList<HennaInstance>(3);
		_adena = player.getAdena();
		_emptySlots = player.getHennaEmptySlots();
		for(int i = 1; i <= 3; ++i)
			if(player.getHenna(i) != null)
				availHenna.add(player.getHenna(i));
	}

	@Override
	protected final void writeImpl()
	{
		writeC(229);
		writeD(_adena);
		writeD(_emptySlots);
		writeD(availHenna.size());
		for(final HennaInstance henna : availHenna)
		{
			writeD((int) henna.getSymbolId());
			writeD((int) henna.getItemIdDye());
			writeD(henna.getAmountDyeRequire() / 2);
			writeD(henna.getPrice() / 5);
			writeD(1);
		}
	}
}
