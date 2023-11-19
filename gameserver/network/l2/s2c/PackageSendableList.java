package l2s.gameserver.network.l2.s2c;

import java.util.ArrayList;
import java.util.List;

import l2s.gameserver.model.Player;
import l2s.gameserver.model.items.ItemInstance;
import l2s.gameserver.templates.item.ItemTemplate;

public class PackageSendableList extends L2GameServerPacket
{
	private int player_obj_id;
	private int char_adena;
	private List<ItemInstance> _itemslist;

	public PackageSendableList(final Player cha, final int playerObjId)
	{
		_itemslist = new ArrayList<ItemInstance>();
		player_obj_id = playerObjId;
		char_adena = cha.getAdena();
		for(final ItemInstance item : cha.getInventory().getItems())
			if(item != null && item.canBeStored(cha, false) && item.canBeDropped(cha))
				_itemslist.add(item);
	}

	@Override
	protected final void writeImpl()
	{
		if(player_obj_id == 0)
			return;
		writeC(195);
		writeD(player_obj_id);
		writeD(char_adena);
		writeD(_itemslist.size());
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
		}
	}
}
