package l2s.gameserver.network.l2.s2c;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import l2s.gameserver.Config;
import l2s.gameserver.data.xml.holder.MultiSellHolder;
import l2s.gameserver.model.base.MultiSellEntry;
import l2s.gameserver.model.base.MultiSellIngredient;
import l2s.gameserver.tables.ItemTable;
import l2s.gameserver.templates.item.ItemTemplate;

public class MultiSellList extends L2GameServerPacket
{
	private static Logger _log;
	private final int _page;
	private final int _finished;
	private final int _listId;
	private final List<MultiSellEntry> _list;

	public MultiSellList(final MultiSellHolder.MultiSellListContainer list, final int page, final int finished)
	{
		_list = list.getEntries();
		_listId = list.getListId();
		_page = page;
		_finished = finished;
	}

	@Override
	protected final void writeImpl()
	{
		writeC(208);
		writeD(_listId);
		writeD(_page);
		writeD(_finished);
		writeD(40);
		writeD(_list.size());
		for(final MultiSellEntry ent : _list)
		{
			final List<MultiSellIngredient> ingredients = ent.getIngredients();
			writeD(ent.getEntryId());
			writeD(0);
			writeD(0);
			writeC(!Config.MULTISELL_PTS || !ent.getProduction().isEmpty() && ent.getProduction().get(0).isStackable() ? 1 : 0);
			writeH(ent.getProduction().size());
			writeH(ingredients.size());
			for(final MultiSellIngredient prod : ent.getProduction())
			{
				final int itemId = prod.getItemId();
				final ItemTemplate template = itemId > 0 ? ItemTable.getInstance().getTemplate(itemId) : null;
				writeH(itemId);
				writeD(template != null ? template.getBodyPart() : 0);
				writeH(template != null ? template.getType2ForPackets() : 0);
				writeD(prod.getItemCount());
				writeH(prod.getItemEnchant());
				writeD(0);
				writeD(0);
			}
			for(final MultiSellIngredient i : ingredients)
			{
				final int itemId = i.getItemId();
				final ItemTemplate item = itemId > 0 ? ItemTable.getInstance().getTemplate(itemId) : null;
				writeH(itemId);
				writeH(item != null ? item.getType2() : 65535);
				writeD(i.getItemCount());
				writeH(i.getItemEnchant());
				writeD(0);
				writeD(0);
			}
		}
	}

	static
	{
		MultiSellList._log = LoggerFactory.getLogger(MultiSellList.class);
	}
}
