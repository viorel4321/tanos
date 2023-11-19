package l2s.gameserver.network.l2.s2c;

import java.util.TreeSet;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;

public class TradeStart extends L2GameServerPacket
{
	private boolean _canWrite = false;
	private TreeSet<ItemInstance> _tradelist;
	private int requester_obj_id;

	public TradeStart(final Player me, final Player other)
	{
		_tradelist = new TreeSet<ItemInstance>(Inventory.OrderComparator);
		if(me == null)
			return;
		requester_obj_id = other.getObjectId();
		final ItemInstance[] items;
		final ItemInstance[] inventory = items = me.getInventory().getItems();
		for(final ItemInstance item : items)
		{
			if(item != null && item.canBeTraded(me))
				_tradelist.add(item);
		}
		_canWrite = true;
	}

	@Override
	protected boolean canWrite()
	{
		return _canWrite;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(30);
		writeD(requester_obj_id);
		writeH(_tradelist.size());
		for(final ItemInstance temp : _tradelist)
		{
			writeH(temp.getTemplate().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getTemplate().getType2());
			writeH(temp.getCustomType1());
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0);
		}
	}
}
