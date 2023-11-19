package l2s.gameserver.network.l2.s2c;

import java.util.TreeSet;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Inventory;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.templates.item.ItemTemplate;

public class WareHouseDepositList extends L2GameServerPacket
{
	private int _whtype;
	private int char_adena;
	private TreeSet<ItemInstance> _itemslist;

	public WareHouseDepositList(final Player cha, final Warehouse.WarehouseType whtype)
	{
		_itemslist = new TreeSet<ItemInstance>(Inventory.OrderComparator);
		cha.setUsingWarehouseType(whtype);
		_whtype = whtype.getPacketValue();
		char_adena = cha.getAdena();
		for(final ItemInstance item : cha.getInventory().getItems())
			if(item != null && item.canBeStored(cha, _whtype == 1))
				_itemslist.add(item);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(65);
		writeH(_whtype);
		writeD(char_adena);
		writeH(_itemslist.size());
		for(final ItemInstance temp : _itemslist)
		{
			final ItemTemplate item = temp.getTemplate();
			writeH(item.getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(item.getType2());
			writeH(temp.getCustomType1());
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(0);
			writeD(temp.getObjectId());
			if(temp.isAugmented())
			{
				writeD(temp.getVariation1Id());
				writeD(temp.getVariation2Id());
			}
			else
				writeQ(0L);
		}
	}
}
