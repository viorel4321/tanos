package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.items.ItemInstance;

public class TradeOwnAdd extends L2GameServerPacket
{
	private ItemInstance temp;
	private int _amount;

	public TradeOwnAdd(final ItemInstance x, final int amount)
	{
		temp = x;
		_amount = amount;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(32);
		writeH(1);
		writeH(temp.getTemplate().getType1());
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
