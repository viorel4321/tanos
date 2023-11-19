package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class GMViewItemList extends L2GameServerPacket
{
	private ItemInstance[] _items;
	private Player _player;

	public GMViewItemList(final Player cha)
	{
		_items = cha.getInventory().getItems();
		_player = cha;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(148);
		writeS(_player.getName());
		writeD(_player.getInventoryLimit());
		writeH(1);
		writeH(_items.length);
		for(final ItemInstance temp : _items)
		{
			writeH(temp.getTemplate().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getTemplate().getType2());
			writeH(temp.getCustomType1());
			writeH(temp.isEquipped() ? 1 : 0);
			writeD(temp.getBodyPart());
			writeH(temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(temp.getVariation1Id());
			writeH(temp.getVariation2Id());
			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
		}
	}
}
