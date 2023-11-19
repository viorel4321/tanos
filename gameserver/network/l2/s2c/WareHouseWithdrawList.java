package l2s.gameserver.network.l2.s2c;

import java.util.NoSuchElementException;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.items.Warehouse;
import l2s.gameserver.templates.item.ItemTemplate;

public class WareHouseWithdrawList extends L2GameServerPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3;
	public static final int FREIGHT = 4;

	private boolean _canWrite = false;
	private int _money;
	private ItemInstance[] _items;
	private int _type;

	public WareHouseWithdrawList(final Player cha, final Warehouse.WarehouseType type, final ItemTemplate.ItemClass clss)
	{
		if(cha == null)
			return;
		_money = cha.getAdena();
		_type = type.getPacketValue();
		cha.setUsingWarehouseType(type);
		switch(type)
		{
			case PRIVATE:
			{
				_items = cha.getWarehouse().listItems(clss);
				break;
			}
			case CLAN:
			case CASTLE:
			{
				_items = cha.getClan().getWarehouse().listItems(clss);
				break;
			}
			case FREIGHT:
			{
				_items = cha.getFreight().listItems(clss);
				break;
			}
			default:
			{
				throw new NoSuchElementException("Invalid value of 'type' argument");
			}
		}
		if(_items.length == 0)
		{
			cha.sendPacket(new SystemMessage(282));
			return;
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
		writeC(66);
		writeH(_type);
		writeD(_money);
		writeH(_items.length);
		for(final ItemInstance temp : _items)
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
