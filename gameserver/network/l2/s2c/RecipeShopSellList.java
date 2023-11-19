package l2s.gameserver.network.l2.s2c;

import l2s.gameserver.model.ManufactureItem;
import l2s.gameserver.model.ManufactureList;
import l2s.gameserver.model.Player;

public class RecipeShopSellList extends L2GameServerPacket
{
	public int obj_id;
	public int curMp;
	public int maxMp;
	public int buyer_adena;
	private ManufactureList createList;

	public RecipeShopSellList(final Player buyer, final Player manufacturer)
	{
		obj_id = manufacturer.getObjectId();
		curMp = (int) manufacturer.getCurrentMp();
		maxMp = manufacturer.getMaxMp();
		buyer_adena = buyer.getAdena();
		createList = manufacturer.getCreateList();
	}

	@Override
	protected final void writeImpl()
	{
		if(createList == null)
			return;
		writeC(217);
		writeD(obj_id);
		writeD(curMp);
		writeD(maxMp);
		writeD(buyer_adena);
		final int count = createList.size();
		writeD(count);
		for(int i = 0; i < count; ++i)
		{
			final ManufactureItem temp = createList.getList().get(i);
			writeD(temp.getRecipeId());
			writeD(0);
			writeD(temp.getCost());
		}
	}
}
