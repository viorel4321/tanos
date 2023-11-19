package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;

public class TradeUpdate extends L2GameServerPacket
{
	private ItemInstance temp;
	private int _amount;

	public TradeUpdate(final ItemInstance x, final int amount)
	{
		temp = x;
		_amount = amount;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(116);
		writeH(1);
		boolean stackable = temp.isStackable();
		if(_amount == 0)
		{
			_amount = 1;
			stackable = false;
		}
		writeH(stackable ? 3 : 2);
		final int type = temp.getTemplate().getType1();
		writeH(type);
		writeD(temp.getObjectId());
		writeD(temp.getItemId());
		writeD(_amount);
		writeH(temp.getTemplate().getType2());
		writeH(0);
		writeD(temp.getBodyPart());
		writeH(temp.getEnchantLevel());
		writeH(0);
		writeH(0);
	}
}
