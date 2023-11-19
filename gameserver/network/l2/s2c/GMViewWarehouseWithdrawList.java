package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.model.pledge.Clan;
import l2s.gameserver.templates.item.ItemTemplate.ItemClass;
import l2s.gameserver.templates.item.WeaponTemplate;

public class GMViewWarehouseWithdrawList extends L2GameServerPacket
{
	private final ItemInstance[] _items;
	private String _charName;
	private int _money;

	public GMViewWarehouseWithdrawList(final Player cha)
	{
		_charName = cha.getName();
		_money = cha.getAdena();
		_items = cha.getWarehouse().listItems(ItemClass.ALL);
	}

	public GMViewWarehouseWithdrawList(final Clan clan)
	{
		_charName = clan.getLeaderName();
		_money = (int) clan.getAdenaCount();
		_items = clan.getWarehouse().listItems(ItemClass.ALL);
	}

	@Override
	protected final void writeImpl()
	{
		writeC(149);
		writeS((CharSequence) _charName);
		writeD(_money);
		writeH(_items.length);
		for(final ItemInstance temp : _items)
		{
			writeH(temp.getTemplate().getType1());
			writeD(temp.getObjectId());
			writeD(temp.getItemId());
			writeD(temp.getIntegerLimitedCount());
			writeH(temp.getTemplate().getType2ForPackets());
			writeH(temp.getCustomType1());
			if(temp.getTemplate().isEquipable())
			{
				writeD(temp.getTemplate().getBodyPart());
				writeH(temp.getEnchantLevel());
				writeH(temp.isWeapon() ? ((WeaponTemplate) temp.getTemplate()).getSoulShotCount() : 0);
				writeH(temp.isWeapon() ? ((WeaponTemplate) temp.getTemplate()).getSpiritShotCount() : 0);
			}
			writeD(temp.getObjectId());
			if(temp.getTemplate().isEquipable())
			{
				writeD(temp.getVariation1Id());
				writeD(temp.getVariation2Id());
			}
		}
	}
}
