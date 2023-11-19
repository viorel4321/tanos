package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.Config;
import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;

public class ItemList extends L2GameServerPacket
{
	private final ItemInstance[] _items;
	private final boolean _showWindow;
	private final boolean _oe;

	public ItemList(final Player cha, final boolean showWindow)
	{
		_items = cha.getInventory().getItems();
		_showWindow = showWindow;
		_oe = cha.isInOlympiadMode() && Config.OLY_ENCHANT_LIMIT;
	}

	public ItemList(final ItemInstance[] items, final boolean showWindow, final boolean oe)
	{
		_items = items;
		_showWindow = showWindow;
		_oe = oe;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(27);
		writeH(_showWindow ? 1 : 0);
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
			writeH(_oe ? temp.getEnchantLevel2() : temp.getEnchantLevel());
			writeH(temp.getCustomType2());
			writeH(temp.getVariation1Id());
			writeH(temp.getVariation2Id());
			writeD(temp.isShadowItem() ? temp.getLifeTimeRemaining() : -1);
		}
	}
}
