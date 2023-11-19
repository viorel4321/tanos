package l2s.gameserver.network.l2.s2c;

import java.util.TreeSet;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;

public class SellList extends L2GameServerPacket
{
	private int _money;
	private final TreeSet<ItemInstance> _selllist;

	public SellList(final Player player)
	{
		_money = player.getAdena();
		_selllist = new TreeSet<ItemInstance>(Inventory.OrderComparator);
		for(final ItemInstance item : player.getInventory().getItems())
			if(item.getTemplate().isSellable() && item.canBeTraded(player))
				_selllist.add(item);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(16);
		writeD(_money);
		writeD(0);
		writeH(_selllist.size());
		for(final ItemInstance item : _selllist)
		{
			writeH(item.getTemplate().getType1());
			writeD(item.getObjectId());
			writeD(item.getItemId());
			writeD(item.getIntegerLimitedCount());
			writeH(item.getTemplate().getType2());
			writeH(item.getCustomType1());
			writeD(item.getBodyPart());
			writeH(item.getEnchantLevel());
			writeH(item.getCustomType2());
			writeH(0);
			if(Config.DIVIDER_SELL ==-1)
				writeD(0);
			else
			writeD(item.getTemplate().getReferencePrice() / Config.DIVIDER_SELL);
		}
	}
}
